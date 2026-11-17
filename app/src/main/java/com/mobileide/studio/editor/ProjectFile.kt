package com.mobileide.studio.editor

import java.io.File

data class ProjectFile(
    val name: String,
    val path: String,
    var content: String = ""
) {
    fun save() = File(path).writeText(content)

    companion object {
        fun load(path: String): ProjectFile {
            val file = File(path)
            return ProjectFile(name = file.name, path = path, content = file.readText())
        }
    }
}
