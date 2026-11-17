package com.mobileide.studio.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SmartScreen
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileide.studio.theme.*

@Composable
fun SettingsScreen() {
    val color = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = com.mobileide.studio.R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp),
            color = color.onSurface
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp)
        ) {
            item { SettingsSection(title = "Appearance") }
            item { SettingsRow(title = stringResource(id = com.mobileide.studio.R.string.settings_theme), subtitle = "Dark", leading = Icons.Default.DarkMode, onClick = {}) }
            item { SettingsRow(title = "Editor font size", subtitle = "14 sp", leading = Icons.Default.TextFormat, onClick = {}) }
            item { SettingsRow(title = stringResource(id = com.mobileide.studio.R.string.settings_auto_save), subtitle = "On", leading = Icons.Default.Save, onClick = {}) }

            item { SettingsSection(title = stringResource(id = com.mobileide.studio.R.string.settings_preview)) }
            item { SettingsRow(title = "Device profile", subtitle = "Pixel 7", leading = Icons.Default.SmartScreen, onClick = {}) }
            item { SettingsRow(title = "Orientation", subtitle = "Portrait", leading = Icons.Default.ScreenRotation, onClick = {}) }

            item { SettingsSection(title = stringResource(id = com.mobileide.studio.R.string.settings_build)) }
            item { SettingsRow(title = "SDK & JDK", subtitle = "Configure build tools", leading = Icons.Default.Build, onClick = {}) }
            item { SettingsRow(title = "Gradle tasks", subtitle = "assembleDebug, clean, test", leading = Icons.Default.Terminal, onClick = {}) }

            item { SettingsSection(title = stringResource(id = com.mobileide.studio.R.string.settings_about)) }
            item { SettingsRow(title = stringResource(id = com.mobileide.studio.R.string.settings_about), subtitle = stringResource(id = com.mobileide.studio.R.string.settings_about_summary), leading = Icons.Default.Info, onClick = {}) }
        }
    }
}

@Composable
private fun SettingsSection(title: String) {
    val color = MaterialTheme.colorScheme
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = color.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(title: String, subtitle: String, leading: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { Icon(imageVector = leading, contentDescription = null, tint = color.primary) },
        headlineContent = { Text(text = title, style = MaterialTheme.typography.bodyMedium, color = color.onSurface) },
        supportingContent = { Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = color.onSurfaceVariant) },
        trailingContent = { Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = color.onSurfaceVariant) }
    )
}
