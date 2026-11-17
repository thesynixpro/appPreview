package com.mobileide.studio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mobileide.studio.terminal.TerminalSession
import com.mobileide.studio.theme.*

@Composable
fun TerminalScreen() {
    val context = LocalContext.current
    val color = MaterialTheme.colorScheme

    val session = remember { TerminalSession(context) }
    DisposableEffect(Unit) {
        session.start()
        onDispose { session.stop() }
    }

    var command by remember { mutableStateOf("") }
    var history by remember { mutableStateListOf<String>() }

    fun runCommand() {
        val cmd = command.trim()
        if (cmd.isEmpty()) return
        history.add(cmd)
        session.send(cmd)
        command = ""
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = com.mobileide.studio.R.string.terminal_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp),
            color = color.onSurface
        )

        // Working directory chip
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = " ", style = MaterialTheme.typography.bodySmall, color = color.onSurfaceVariant, modifier = Modifier.size(4.dp))
            Text(text = session.workingDirectory, style = MaterialTheme.typography.bodySmall, color = color.onSurfaceVariant)
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { session.clear() }) {
                Text("Clear", style = MaterialTheme.typography.labelLarge, color = color.primary)
            }
        }

        Divider(modifier = Modifier.fillMaxWidth())

        // Terminal output
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(color.surfaceContainerHigh, MaterialTheme.shapes.medium)
        ) {
            TerminalOutput(lines = session.output)
        }

        // Command input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = command,
                onValueChange = { command = it },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = color.onSurface, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(color.surface, MaterialTheme.shapes.medium)
                    .padding(8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Go
                ),
                decorationBox = { innerField ->
                    Box {
                        if (command.isEmpty()) {
                            Text(
                                "Type a command…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = color.onSurfaceVariant
                            )
                        }
                        innerField()
                    }
                }
            )
            IconButton(onClick = { runCommand() }) {
                Icon(imageVector = ArrowForward, contentDescription = "Run", tint = color.primary)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TerminalButton(label = stringResource(id = com.mobileide.studio.R.string.terminal_run), onClick = { runCommand() })
            TerminalButton(label = stringResource(id = com.mobileide.studio.R.string.terminal_stop), onClick = { session.stop() }, color = color.error)
            TerminalButton(label = stringResource(id = com.mobileide.studio.R.string.terminal_clear), onClick = { session.clear() })
        }
    }
}

@Composable
private fun TerminalOutput(lines: List<String>) {
    val color = MaterialTheme.colorScheme
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            count = lines.size,
            key = { index -> index }
        ) { index ->
            val line = lines[index]
            Text(
                text = line,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                color = if (line.startsWith("error", ignoreCase = true) || line.contains("command not found", ignoreCase = true)) color.error else Color(0xFFEAEAEF),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TerminalButton(label: String, onClick: () -> Unit, color: Color = MaterialTheme.colorScheme.primary) {
    val colorScheme = MaterialTheme.colorScheme
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = colorScheme.onPrimary)
    }
}
