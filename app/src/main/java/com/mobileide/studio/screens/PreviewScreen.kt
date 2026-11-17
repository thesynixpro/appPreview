package com.mobileide.studio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SmartScreen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileide.studio.theme.*

@Composable
fun PreviewScreen() {
    val color = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {
        // Top toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = {},
                label = { Text("My Project", style = MaterialTheme.typography.labelSmall, color = color.onSurface) }
            )
            AssistChip(
                onClick = {},
                label = { Text("activity_main.xml", style = MaterialTheme.typography.labelSmall, color = color.onSurface) }
            )
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh preview", tint = color.primary)
            }
            Spacer(modifier = Modifier.weight(1f))
            AssistChip(onClick = {}, label = { Text("Pixel 7", style = MaterialTheme.typography.labelSmall, color = color.onSurface) })
            AssistChip(onClick = {}, label = { Text("Portrait", style = MaterialTheme.typography.labelSmall, color = color.onSurface) })
            AssistChip(onClick = {}, label = { Text("Light", style = MaterialTheme.typography.labelSmall, color = color.onSurface) })
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Default.Fullscreen, contentDescription = "Full screen preview", tint = color.primary)
            }
        }

        Divider(modifier = Modifier.fillMaxWidth())

        // Device frame + preview
        PreviewDeviceFrame(
            content = {
                PreviewPlaceholder(
                    message = stringResource(id = com.mobileide.studio.R.string.preview_ready),
                    detail = "Open or create a project to render a live UI preview."
                )
            }
        )
    }
}

@Composable
private fun PreviewDeviceFrame(content: @Composable () -> Unit) {
    val color = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(360.dp, 720.dp)
                .background(Color(0xFF1E1E22), MaterialTheme.shapes.large)
                .padding(16.dp)
        ) {
            // Status bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.SignalCellularAlt, contentDescription = null, tint = color.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "10:42", style = MaterialTheme.typography.labelSmall, color = color.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Default.BatteryFull, contentDescription = null, tint = color.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }

            // Preview content area
            Box(
                modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }

            // Navigation bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.BottomCenter)
                    .background(color.surfaceContainerHigh)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NavBarIcon(icon = ArrowBack, label = "Back")
                NavBarIcon(icon = Home, label = "Home")
                NavBarIcon(icon = Apps, label = "Recents")
            }
        }
    }
}

@Composable
private fun NavBarIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    val color = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.size(48.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color.primary, modifier = Modifier.size(22.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color.onSurfaceVariant)
    }
}

@Composable
private fun PreviewPlaceholder(message: String, detail: String) {
    val color = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = Icons.Default.SmartScreen, contentDescription = null, tint = color.onSurfaceVariant, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.size(16.dp))
        Text(text = message, style = MaterialTheme.typography.titleMedium, color = color.onSurface)
        Text(text = detail, style = MaterialTheme.typography.bodySmall, color = color.onSurfaceVariant)
    }
}
