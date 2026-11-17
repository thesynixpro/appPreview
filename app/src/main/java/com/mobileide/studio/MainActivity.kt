@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.mobileide.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.mobileide.studio.editor.EditorScreen
import com.mobileide.studio.editor.ProjectFile
import com.mobileide.studio.model.ProjectManager
import com.mobileide.studio.screens.HomeScreen
import com.mobileide.studio.screens.PreviewScreen
import com.mobileide.studio.screens.ProjectsScreen
import com.mobileide.studio.screens.SettingsScreen
import com.mobileide.studio.screens.TerminalScreen
import com.mobileide.studio.theme.MobileIDETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileIDETheme {
                MobileIDEShell()
            }
        }
    }
}

enum class Screen(
    val label: String,
    val icon: ImageVector
) {
    Home("Home", Icons.Default.Home),
    Projects("Projects", Icons.Default.Folder),
    Preview("Preview", Icons.Default.Tv),
    Terminal("Terminal", Icons.Default.Terminal),
    Settings("Settings", Icons.Default.Settings),
}

@Composable
fun MobileIDEShell() {
    val context = LocalContext.current
    val projectManager = remember(context) { ProjectManager(context) }

    var openFile by remember { mutableStateOf<ProjectFile?>(null) }
    var selectedScreen by remember { mutableStateOf(Screen.Home) }

    // Real project import: pick a ZIP through SAF and extract it into the projects dir.
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val imported = projectManager.importZip(uri, uri.lastPathSegment)
            if (imported != null) {
                projectManager.defaultSourceFile(imported)?.let { file ->
                    openFile = ProjectFile.load(file.absolutePath)
                }
            }
        }
    }

    val color = MaterialTheme.colorScheme
    val currentFile = openFile

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentFile?.name ?: selectedScreen.label,
                        style = MaterialTheme.typography.headlineMedium,
                        color = color.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = color.surfaceContainer,
                    titleContentColor = color.onSurface
                ),
                actions = {
                    if (currentFile != null) {
                        IconButton(onClick = { openFile = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close editor",
                                tint = color.onSurface
                            )
                        }
                    } else if (selectedScreen == Screen.Preview) {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh preview",
                                tint = color.onSurface
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (currentFile == null) {
                NavigationBar(
                    containerColor = color.surfaceContainer,
                    contentColor = color.onSurface
                ) {
                    Screen.entries.forEach { screen ->
                        NavigationBarItem(
                            selected = selectedScreen == screen,
                            onClick = { selectedScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label,
                                    tint = if (selectedScreen == screen) color.primary else color.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = screen.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedScreen == screen) color.primary else color.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = color.background
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            if (currentFile != null) {
                EditorScreen(file = currentFile, onSave = { })
            } else {
                when (selectedScreen) {
                    Screen.Home -> HomeScreen(
                        projectManager = projectManager,
                        onCreateProject = { name, language ->
                            val created = projectManager.createProject(name, language)
                            projectManager.defaultSourceFile(created)?.let { file ->
                                openFile = ProjectFile.load(file.absolutePath)
                            }
                        },
                        onImport = {
                            importLauncher.launch(
                                arrayOf(
                                    "application/zip",
                                    "application/x-zip-compressed",
                                    "application/octet-stream"
                                )
                            )
                        },
                        onOpenProject = { project ->
                            projectManager.defaultSourceFile(project)?.let { file ->
                                openFile = ProjectFile.load(file.absolutePath)
                            }
                        },
                        onOpenPreview = { selectedScreen = Screen.Preview },
                        onOpenTerminal = { selectedScreen = Screen.Terminal }
                    )
                    Screen.Projects -> ProjectsScreen(
                        projectManager = projectManager,
                        onOpen = { file -> openFile = file }
                    )
                    Screen.Preview -> PreviewScreen()
                    Screen.Terminal -> TerminalScreen()
                    Screen.Settings -> SettingsScreen()
                }
            }
        }
    }
}
