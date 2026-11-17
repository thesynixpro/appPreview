package com.mobileide.studio.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

object SyntaxHighlighter {
    private val commentStyle = SpanStyle(color = Color(0xFF8A8A92))
    private val stringStyle = SpanStyle(color = Color(0xFF4CAF50))
    private val keywordStyle = SpanStyle(color = Color(0xFFBB86FC), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    private val annotationStyle = SpanStyle(color = Color(0xFF3AA9FF))
    private val numberStyle = SpanStyle(color = Color(0xFFFFB300))

    private val commentRegex = Regex("//[^\\n]*|/\\*[\\s\\S]*?\\*/")
    private val stringRegex = Regex("\"(?:\\\\.|[^\"\\\\])*\"")
    private val charRegex = Regex("'(?:\\\\.|[^'\\\\])*'")
    private val annotationRegex = Regex("@\\w+")
    private val keywordRegex = Regex(
        "\\b(var|val|fun|class|object|interface|sealed|enum|if|else|when|for|while|do|return|throw|try|catch|finally|" +
            "abstract|override|open|final|private|public|internal|protected|companion|init|constructor|by|in|is|as|super|" +
            "this|null|true|false|suspend|typealias|import|package|const|lateinit|vararg|reified|inline|external|" +
            "native|synchronized|transient|volatile|strictfp|native|assert|goto|const)\\b"
    )
    private val numberRegex = Regex("\\b\\d+\\.?\\d*\\b")

    private data class Span(val start: Int, val end: Int, val style: SpanStyle)

    fun highlight(text: String): AnnotatedString {
        val spans = mutableListOf<Span>()
        fun add(regex: Regex, style: SpanStyle) {
            regex.findAll(text).forEach { m ->
                val s = m.range.first
                val e = m.range.last + 1
                if (!spans.any { it.start < e && s < it.end }) {
                    spans.add(Span(s, e, style))
                }
            }
        }
        add(commentRegex, commentStyle)
        add(stringRegex, stringStyle)
        add(charRegex, stringStyle)
        add(annotationRegex, annotationStyle)
        add(keywordRegex, keywordStyle)
        add(numberRegex, numberStyle)
        spans.sortBy { it.start }

        return buildAnnotatedString {
            var last = 0
            for (span in spans) {
                if (span.start > last) {
                    append(text.substring(last, span.start.coerceAtMost(text.length)))
                }
                val end = span.end.coerceAtMost(text.length)
                if (span.start < end) {
                    withStyle(span.style) {
                        append(text.substring(span.start, end))
                    }
                }
                last = end
            }
            if (last < text.length) {
                append(text.substring(last))
            }
        }
    }
}
