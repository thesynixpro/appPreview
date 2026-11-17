package com.mobileide.studio.model

import android.content.Context
import android.net.Uri
import java.io.BufferedInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipInputStream

class ProjectManager(private val context: Context) {

    private val projectsDir: File

    init {
        val external = context.getExternalFilesDir(null)
        projectsDir = if (external != null) File(external, "projects") else File(context.filesDir, "projects")
        projectsDir.mkdirs()
    }

    fun listProjects(): List<Project> {
        return projectsDir.listFiles()
            ?.filter { it.isDirectory && File(it, ".project").exists() }
            ?.map { projectToModel(it) }
            .orEmpty()
            .sortedByDescending { it.lastModified }
    }

    private fun projectToModel(dir: File): Project {
        val name = File(dir, ".project").readText().trim()
        val language = if (listSourceFiles(dir).any { it.extension == "kt" }) "Kotlin" else "Java"
        val lastModified = formatDate(dir.lastModified())
        return Project(name = name, path = dir.absolutePath, language = language, lastModified = lastModified)
    }

    fun createProject(name: String, language: String): Project {
        val safeName = name.replace("[^a-zA-Z0-9_.-]".toRegex(), "_")
        val dir = File(projectsDir, safeName)
        dir.mkdirs()
        File(dir, ".project").writeText(safeName)

        val appDir = File(dir, "app")
        val srcMain = File(appDir, "src/main")
        val javaDir = File(srcMain, "java/com/example/$safeName".replace('.', File.separatorChar))
        val resLayout = File(srcMain, "res/layout")
        val resValues = File(srcMain, "res/values")
        javaDir.mkdirs()
        resLayout.mkdirs()
        resValues.mkdirs()

        File(dir, "settings.gradle.kts").writeText("pluginManagement { repositories { gradlePluginPortal() } }\n")
        File(appDir, "build.gradle.kts").writeText(buildGradle(language))
        File(dir, "AndroidManifest.xml").writeText(manifest())
        File(resValues, "strings.xml").writeText(stringsXml(safeName))
        File(resValues, "themes.xml").writeText(themesXml())
        if (language == "Kotlin") {
            File(javaDir, "MainActivity.kt").writeText(mainActivityKt(safeName, language))
        } else {
            File(javaDir, "MainActivity.java").writeText(mainActivityJava(safeName))
        }
        File(resLayout, "activity_main.xml").writeText(activityMainXml())

        return Project(
            name = safeName,
            path = dir.absolutePath,
            language = language,
            lastModified = formatDate(System.currentTimeMillis())
        )
    }

    fun deleteProject(path: String): Boolean {
        return File(path).deleteRecursively()
    }

    /** Total bytes used by all generated projects, for the Home storage card. */
    fun storageUsedBytes(): Long {
        return projectsDir.listFiles().orEmpty().sumOf { dir ->
            dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        }
    }

    /**
     * Import a project from a ZIP chosen through SAF. Entries are extracted into a
     * staging directory first and each path is validated so a crafted archive
     * cannot escape it (zip-slip).
     */
    fun importZip(source: Uri, displayName: String?): Project? {
        val staging = File(projectsDir, ".import_staging")
        if (staging.exists()) staging.deleteRecursively()
        if (!staging.mkdirs()) return null
        try {
            val stream = context.contentResolver.openInputStream(source) ?: return null
            ZipInputStream(BufferedInputStream(stream)).use { zip ->
                val root = staging.canonicalPath + File.separator
                var entry = zip.nextEntry
                while (entry != null) {
                    val target = File(staging, entry.name)
                    if (!target.canonicalPath.startsWith(root)) {
                        throw SecurityException("Unsafe ZIP entry: ${entry.name}")
                    }
                    if (entry.isDirectory) {
                        target.mkdirs()
                    } else {
                        target.parentFile?.mkdirs()
                        target.outputStream().use { out -> zip.copyTo(out) }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }

            val children = staging.listFiles()?.filter { it.name != "__MACOSX" }.orEmpty()
            val single = children.singleOrNull()?.takeIf { it.isDirectory }
            val projectRoot = single ?: staging

            val rawName = displayName?.substringBeforeLast('.')?.takeIf { it.isNotBlank() }
                ?: projectRoot.name
            val safeName = rawName.replace("[^a-zA-Z0-9_.-]".toRegex(), "_").ifBlank { "ImportedProject" }
            val dest = File(projectsDir, safeName)
            if (dest.exists()) dest.deleteRecursively()

            val moved = projectRoot.renameTo(dest) ||
                runCatching { projectRoot.copyRecursively(dest, overwrite = true); true }.getOrDefault(false)
            if (!moved) return null

            if (!File(dest, ".project").exists()) File(dest, ".project").writeText(safeName)
            return Project(
                name = safeName,
                path = dest.absolutePath,
                language = detectLanguage(dest),
                lastModified = formatDate(System.currentTimeMillis())
            )
        } catch (e: Exception) {
            return null
        } finally {
            if (staging.exists()) staging.deleteRecursively()
        }
    }

    private fun detectLanguage(dir: File): String =
        if (listSourceFiles(dir).any { it.extension == "kt" }) "Kotlin" else "Java"


    fun defaultSourceFile(project: Project): File? {
        val dir = File(project.path)
        val kt = File(dir, "app/src/main/java").walkTopDown().firstOrNull { it.extension == "kt" }
        val candidate = kt ?: File(dir, "app/build.gradle.kts")
        return candidate.takeIf { it.exists() }
    }

    fun listSourceFiles(dir: File): List<File> {
        if (!dir.exists()) return emptyList()
        val exts = listOf("kt", "java", "xml", "kts", "gradle")
        return dir.walkTopDown().filter { f -> exts.contains(f.extension) }.toList()
    }

    private fun formatDate(millis: Long): String {
        return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
    }

    private fun buildGradle(language: String): String {
        val kotlin = language == "Kotlin"
        val kotlinPlugin = if (kotlin) "\n    id(\"org.jetbrains.kotlin.android\")" else ""
        val kotlinOptionsBlock = if (kotlin) """
    kotlinOptions {
        jvmTarget = "17"
    }
""" else ""
        return """plugins {
    id("com.android.application")$kotlinPlugin
}

android {
    namespace = "com.example.${"app"}"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
$kotlinOptionsBlock

    buildFeatures {
        compose = $kotlin
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
"""
    }

    private fun manifest(): String {
        return """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.app">

    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:theme="@style/Theme.MyApp">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
"""
    }

    private fun stringsXml(appName: String): String {
        return """<resources>
    <string name="app_name">$appName</string>
    <string name="hello_world">Hello World</string>
</resources>
"""
    }

    private fun themesXml(): String {
        return """<resources>
    <style name="Theme.MyApp" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
"""
    }

    private fun mainActivityKt(appName: String, language: String): String {
        val safe = appName.replace("[^A-Za-z0-9]".toRegex(), "")
        return """package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                Greeting("Hello $safe")
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        content()
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = name, style = MaterialTheme.typography.headlineMedium)
}
"""
    }

    private fun mainActivityJava(appName: String): String {
        val safe = appName.replace("[^A-Za-z0-9]".toRegex(), "")
        return """package com.example.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView view = new TextView(this);
        view.setText("Hello $safe");
        view.setTextSize(24f);
        setContentView(view);
    }
}
"""
    }

    private fun activityMainXml(): String {
        return """<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/textView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/hello_world"
        android:textAppearance="@style/TextAppearance.Material3.TitleLarge"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
"""
    }
}
