package com.mobileide.studio.terminal

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.File

/** A real shell session backed by /system/bin/sh (Android toolbox shell). */
class TerminalSession(private val context: Context) {

    private var process: Process? = null
    private var stdin: OutputStreamWriter? = null
    val output: SnapshotStateList<String> = mutableStateListOf()
    private val handler = Handler(Looper.getMainLooper())

    var workingDirectory: String
        private set

    init {
        val external = context.getExternalFilesDir(null)
        workingDirectory = if (external != null) external.absolutePath else context.filesDir.absolutePath
    }

    fun start() {
        stop()
        try {
            val pb = ProcessBuilder("/system/bin/sh")
            pb.directory(File(workingDirectory))
            pb.redirectErrorStream(true)
            process = pb.start()
            stdin = OutputStreamWriter(process!!.outputStream)
            val reader = BufferedReader(InputStreamReader(process!!.inputStream))
            Thread {
                var line = reader.readLine()
                while (line != null) {
                    val l = line
                    handler.post {
                        output.add(l)
                        if (output.size > 3000) output.removeAt(0)
                    }
                    line = reader.readLine()
                }
                handler.post { output.add("[shell process exited]") }
            }.start()
        } catch (e: Exception) {
            output.add("error starting shell: ${e.message ?: e.toString()}")
        }
    }

    fun send(command: String) {
        val cmd = command.trimEnd()
        stdin?.write(cmd + "\n")
        stdin?.flush()
        output.add("\$workingDirectory \$cmd")
    }

    fun stop() {
        try { stdin?.close() } catch (_: Exception) {}
        process?.destroy()
        process = null
        stdin = null
    }

    fun clear() {
        output.clear()
    }
}
