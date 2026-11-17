package com.mobileide.studio.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.SmartScreen
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileide.studio.R
import com.mobileide.studio.model.Project
import com.mobileide.studio.model.ProjectManager

@Composable
fun HomeScreen(
    projectManager: ProjectManager,
    onCreateProject: (String, String) -> Unit,
    onImport: () -> Unit,
    onOpenProject: (Project) -> Unit,
    onOpenPreview: () -> Unit,
    onOpenTerminal: () -> Unit
) {
    val color = MaterialTheme.colorScheme
    var showCreate by remember { mutableStateOf(false) }
    val projects = remember(projectManager) { projectManager.listProjects() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = color.surfaceContainerHigh),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(id = R.string.home_welcome),
                        style = MaterialTheme.typography.headlineLarge,
                        color = color.onSurface
                    )
                    Text(
                        text = stringResource(id = R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = color.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton("Create", Icons.Default.Add) { showCreate = true }
                QuickActionButton("Import", Icons.Default.FolderOpen) { onImport() }
                QuickActionButton("Preview", Icons.Default.SmartScreen) { onOpenPreview() }
                QuickActionButton("Terminal", Icons.Default.Terminal) { onOpenTerminal() }
            }
        }

        item { SectionTitle(stringResource(id = R.string.home_recent_projects)) }

        if (projects.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOff,
                        contentDescription = null,
                        tint = color.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.home_no_projects),
                        style = MaterialTheme.typography.bodyMedium,
                        color = color.onSurfaceVariant
                    )
                }
            }
        } else {
            items(projects, key = { it.path }) { project ->
                HomeProjectCard(project = project) { onOpenProject(project) }
            }
        }

        item { SectionTitle(stringResource(id = R.string.home_last_build)) }
        item {
            BuildStatusCard(
                label = stringResource(id = R.string.home_build_never),
                status = BuildStatus.None
            )
        }

        item { SectionTitle(stringResource(id = R.string.home_recent_errors)) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = color.surfaceContainerHigh),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(color = ErrorSeverity.Info.color, radius = size.minDimension / 2f)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.home_no_errors),
                        style = MaterialTheme.typography.bodyMedium,
                        color = color.onSurface
                    )
                }
            }
        }

        item { SectionTitle(stringResource(id = R.string.home_storage)) }
        item {
            val used = remember(projectManager) { projectManager.storageUsedBytes() }
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = color.surfaceContainerHigh),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.home_storage),
                            style = MaterialTheme.typography.titleMedium,
                            color = color.onSurface
                        )
                        Text(
                            text = "${projects.size} project(s) on device",
                            style = MaterialTheme.typography.bodySmall,
                            color = color.onSurfaceVariant
                        )
                    }
                    Text(
                        text = formatSize(used),
                        style = MaterialTheme.typography.bodyMedium,
                        color = color.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showCreate) {
        CreateProjectDialog(
            onDismiss = { showCreate = false },
            onCreate = { name, language ->
                showCreate = false
                onCreateProject(name, language)
            }
        )
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}

@Composable
private fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    val color = MaterialTheme.colorScheme
    var name by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("Kotlin") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Project", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pick a name and a language for the generated template.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = language == "Kotlin",
                        onClick = { language = "Kotlin" },
                        label = { Text("Kotlin") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color.primary)
                    )
                    FilterChip(
                        selected = language == "Java",
                        onClick = { language = "Java" },
                        label = { Text("Java") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color.primary)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onCreate(name.trim(), language) }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun QuickActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.width(76.dp).clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color.primary,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun HomeProjectCard(project: Project, onOpen: () -> Unit) {
    val color = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = color.surfaceContainerHigh),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = project.name, style = MaterialTheme.typography.titleMedium, color = color.onSurface)
                Text(
                    text = "${project.language}  •  ${project.lastModified}",
                    style = MaterialTheme.typography.bodySmall,
                    color = color.onSurfaceVariant
                )
            }
            Badge(
                containerColor = project.buildStatus.color,
                contentColor = Color.White
            ) {
                Text(project.buildStatus.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

enum class BuildStatus(val label: String, val color: Color) {
    None("—", Color(0xFF6A6A70)),
    Success("OK", Color(0xFF4CAF50)),
    Failure("Fail", Color(0xFFCF6679)),
    Running("Building", Color(0xFF3AA9FF))
}

enum class ErrorSeverity(val color: Color) {
    Info(Color(0xFF3AA9FF)),
    Warning(Color(0xFFFFB300)),
    Error(Color(0xFFCF6679))
}

@Composable
private fun BuildStatusCard(label: String, status: BuildStatus) {
    val color = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = color.surfaceContainerHigh),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = status.color, radius = size.minDimension / 2f)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(id = R.string.home_last_build),
                    style = MaterialTheme.typography.bodySmall,
                    color = color.onSurfaceVariant
                )
                Text(text = label, style = MaterialTheme.typography.bodyMedium, color = color.onSurface)
            }
        }
    }
}
