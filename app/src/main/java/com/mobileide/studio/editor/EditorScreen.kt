package com.mobileide.studio.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileide.studio.theme.*

@Composable
fun EditorScreen(file: ProjectFile, onSave: () -> Unit = {}) {
    val color = MaterialTheme.colorScheme

    var value by remember { mutableStateOf(TextFieldValue(file.content)) }
    var highlighted by remember { mutableStateOf<AnnotatedString>(SyntaxHighlighter.highlight(file.content)) }
    var history by remember { mutableStateOf(listOf(file.content)) }
    var histIndex by remember { mutableStateOf(0) }
    var composing by remember { mutableStateOf(false) }

    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var showFind by remember { mutableStateOf(false) }

    val codeStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        color = Color(0xFFEAEAEF)
    )

    fun text(): String = value.text

    fun handleChange(newValue: TextFieldValue) {
        val old = text()
        val fresh = newValue.text
        var adjusted = fresh

        if (!composing) {
            // Auto-close brackets when typed at the end with a collapsed selection.
            val collapsedAtEnd = newValue.selection.collapsed && newValue.selection.start == old.length
            when {
                adjusted.endsWith("(") && collapsedAtEnd -> adjusted += ")"
                adjusted.endsWith("{") && collapsedAtEnd -> adjusted += "}"
                adjusted.endsWith("[") && collapsedAtEnd -> adjusted += "]"
                adjusted.endsWith("\"") && collapsedAtEnd -> adjusted += "\""
                adjusted.endsWith("'") && collapsedAtEnd -> adjusted += "'"
            }
            // Auto-indent on newline: replicate leading whitespace of current line.
            if (adjusted.endsWith("\n")) {
                val leading = currentIndent(old)
                adjusted += leading
            }
        }

        if (adjusted != fresh) {
            value = TextFieldValue(adjusted, TextRange(adjusted.length, adjusted.length))
        } else {
            value = newValue
        }

        if (!composing && adjusted != old && adjusted != (history.lastOrNull() ?: "")) {
            val trimmed = adjusted
            history = (history.subList(0, histIndex + 1) + trimmed).takeLast(100)
            histIndex = history.lastIndex
        }
        highlighted = SyntaxHighlighter.highlight(text())
    }

    fun undo() {
        if (histIndex > 0) {
            composing = true
            histIndex--
            value = TextFieldValue(history[histIndex], TextRange(history[histIndex].length))
            composing = false
            highlighted = SyntaxHighlighter.highlight(history[histIndex])
        }
    }

    fun redo() {
        if (histIndex < history.lastIndex) {
            composing = true
            histIndex++
            value = TextFieldValue(history[histIndex], TextRange(history[histIndex].length))
            composing = false
            highlighted = SyntaxHighlighter.highlight(history[histIndex])
        }
    }

    fun insert(symbol: String) {
        val sel = value.selection
        val t = text()
        val newText = t.substring(0, sel.start) + symbol + t.substring(sel.end)
        value = TextFieldValue(newText, TextRange(sel.start + symbol.length))
        if (!composing) {
            history = (history.subList(0, histIndex + 1) + newText).takeLast(100)
            histIndex = history.lastIndex
        }
        highlighted = SyntaxHighlighter.highlight(newText)
    }

    fun save() {
        val f = file
        f.content = text()
        f.save()
        onSave()
    }

    fun findFirst() {
        val i = text().indexOf(findText, ignoreCase = true)
        if (i >= 0) value = TextFieldValue(text(), TextRange(i, i + findText.length))
    }

    fun replaceAll() {
        val t = text().replace(findText, replaceText, ignoreCase = true)
        value = TextFieldValue(t, TextRange(t.length))
        highlighted = SyntaxHighlighter.highlight(t)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.titleMedium,
                color = color.onSurface,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(onClick = { save() }) {
                Text("Save", style = MaterialTheme.typography.labelLarge)
            }
            IconButton(onClick = { undo() }, enabled = histIndex > 0) {
                Icon(imageVector = Undo, contentDescription = "Undo")
            }
            IconButton(onClick = { redo() }, enabled = histIndex < history.lastIndex) {
                Icon(imageVector = Redo, contentDescription = "Redo")
            }
            IconButton(onClick = { showFind = !showFind }) {
                Icon(imageVector = Search, contentDescription = "Find")
            }
        }

        Divider(modifier = Modifier.fillMaxWidth())

        // Find/replace bar
        if (showFind) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    decorationBox = { inner ->
                        if (findText.isEmpty()) {
                            Text("Find", style = MaterialTheme.typography.bodyMedium, color = color.onSurfaceVariant)
                        }
                        inner()
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = color.onSurface),
                    modifier = Modifier.weight(1f).background(color.surfaceContainerHigh, MaterialTheme.shapes.medium).padding(horizontal = 8.dp, vertical = 6.dp),
                    singleLine = true
                )
                BasicTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    decorationBox = { inner ->
                        if (replaceText.isEmpty()) {
                            Text("Replace", style = MaterialTheme.typography.bodyMedium, color = color.onSurfaceVariant)
                        }
                        inner()
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = color.onSurface),
                    modifier = Modifier.weight(1f).background(color.surfaceContainerHigh, MaterialTheme.shapes.medium).padding(horizontal = 8.dp, vertical = 6.dp),
                    singleLine = true
                )
                TextButton(onClick = { findFirst() }) { Text("Find") }
                TextButton(onClick = { replaceAll() }) { Text("Replace All") }
                IconButton(onClick = { showFind = false }) {
                    Icon(imageVector = Close, contentDescription = "Close find")
                }
            }
            Divider(modifier = Modifier.fillMaxWidth())
        }

        // Editor area: line numbers + highlighted code + transparent editable field
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Line numbers gutter
            val lineCount = text().lines().size.let { if (it == 0) 1 else it }
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.End
            ) {
                Text(
                    text = (1..lineCount).joinToString(separator = "\n") { it.toString() },
                    style = codeStyle.copy(color = color.onSurfaceVariant),
                    lineHeight = 20.sp
                )
            }
            Divider(modifier = Modifier.width(1.dp).fillMaxHeight(), color = color.outline)

            Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 8.dp, vertical = 4.dp)) {
                // Syntax-highlighted display layer
                BasicText(
                    text = highlighted,
                    style = codeStyle,
                    modifier = Modifier.fillMaxSize()
                )
                // Transparent editable layer
                BasicTextField(
                    value = value,
                    onValueChange = ::handleChange,
                    textStyle = codeStyle.copy(color = Color.Transparent),
                    modifier = Modifier.fillMaxSize(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    decorationBox = { innerField -> innerField() }
                )
            }
        }

        // Programming symbol keyboard toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(color.surfaceContainer),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val symbols = listOf("{", "}", "[", "]", "(", ")", "<", ">", ";", ":", ",", ".", "@", "#", "=", "+", "-", "*", "/", "!", "&", "|", "?", "_")
            symbols.forEach { symbol ->
                SymbolButton(symbol = symbol, onClick = { insert(symbol) })
            }
        }
    }
}

private fun currentIndent(text: String): String {
    val lastNewline = text.lastIndexOf('\n')
    if (lastNewline < 0) return ""
    val line = text.substring(lastNewline + 1)
    val indent = line.takeWhile { it == ' ' || it == '\t' }
    return indent
}

@Composable
private fun SymbolButton(symbol: String, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.size(34.dp).clickable(onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = symbol, style = MaterialTheme.typography.bodyMedium, color = color.onSurface)
    }
}
