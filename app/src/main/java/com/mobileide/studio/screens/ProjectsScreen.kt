package com.mobileide.studio.screens

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileide.studio.editor.ProjectFile
import com.mobileide.studio.model.Project
import com.mobileide.studio.model.ProjectManager
import com.mobileide.studio.theme.*

@Composable
fun ProjectsScreen(projectManager: ProjectManager, onOpen: (ProjectFile) -> Unit) {
    val color = MaterialTheme.colorScheme

    var search by remember { mutableStateOf("") }
    var deleteConfirm: String? by remember { mutableStateOf(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = com.mobileide.studio.R.string.projects_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp),
            color = color.onSurface
        )

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text(stringResource(id = com.mobileide.studio.R.string.projects_search)) },
            modifier = Modifier.padding(horizontal = 16.dp),
            singleLine = true
        )

        val projects = projectManager.listProjects().filter { it.name.contains(search, ignoreCase = true) }

        if (projects.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = FolderOff,
                    contentDescription = null,
                    tint = color.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = com.mobileide.studio.R.string.projects_empty),
                    style = MaterialTheme.typography.titleMedium,
                    color = color.onSurfaceVariant
                )
                Text(
                    text = stringResource(id = com.mobileide.studio.R.string.projects_empty_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = color.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp)
            ) {
                items(projects, key = { it.name }) { project ->
                    ProjectCard(
                        project = project,
                        projectManager = projectManager,
                        onOpen = { f -> onOpen(f) },
                        onDelete = { deleteConfirm = project.path }
                    )
                }
            }
        }
    }

    deleteConfirm?.let { path ->
        ConfirmDeleteDialog(
            onDismiss = { deleteConfirm = null },
            onDelete = {
                projectManager.deleteProject(path)
                deleteConfirm = null
            }
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(onDismiss: () -> Unit, onDelete: () -> Unit) {
    val color = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = com.mobileide.studio.R.string.confirm_delete_title), style = MaterialTheme.typography.titleLarge, color = color.onSurface) },
        text = { Text(stringResource(id = com.mobileide.studio.R.string.confirm_delete_msg), style = MaterialTheme.typography.bodyMedium, color = color.onSurfaceVariant) },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text(stringResource(id = com.mobileide.studio.R.string.btn_confirm), style = MaterialTheme.typography.labelLarge, color = color.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = com.mobileide.studio.R.string.btn_cancel), style = MaterialTheme.typography.labelLarge, color = color.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun ProjectCard(project: Project, projectManager: ProjectManager, onOpen: (ProjectFile) -> Unit, onDelete: () -> Unit) {
    val color = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = {
            projectManager.defaultSourceFile(project)?.let { f -> onOpen(ProjectFile.load(f.absolutePath)) }
        }),
        colors = CardDefaults.cardColors(containerColor = color.surfaceContainerHigh),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = project.name, style = MaterialTheme.typography.titleMedium, color = color.onSurface)
                Text(text = "${project.language}  •  ${project.lastModified}", style = MaterialTheme.typography.bodySmall, color = color.onSurfaceVariant)
            }
            Row {
                Badge(
                    containerColor = project.buildStatus.color,
                    contentColor = Color.White,
                    content = { Text(project.buildStatus.label, style = MaterialTheme.typography.labelSmall) }
                )
                IconButton(onClick = { onDelete() }) {
                    Icon(imageVector = Delete, contentDescription = "Delete", tint = color.onSurfaceVariant)
                }
            }
        }
    }
}
