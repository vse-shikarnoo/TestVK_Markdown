package kv.test.markdown.data.parser

import kv.test.markdown.domain.model.*
import kv.test.markdown.domain.parser.MarkdownParser

class MarkdownParserImpl : MarkdownParser {
    override fun parse(markdown: String): List<MarkdownBlock> {
        val lines = markdown.lines()
        val blocks = mutableListOf<MarkdownBlock>()
        var paragraphBuffer = mutableListOf<String>()
        var i = 0
        fun flushParagraph() {
            if (paragraphBuffer.isNotEmpty()) {
                blocks.add(
                    MarkdownBlock.Paragraph(
                        parseInlines(paragraphBuffer.joinToString("\n"))
                    )
                )
                paragraphBuffer.clear()
            }
        }
        while (i < lines.size) {
            val line = lines[i]
            val headerMatch = Regex("^(#{1,6})\\s+(.*)").find(line)
            val imageMatch = Regex("!\\[(.*?)\\]\\((.*?)\\)").find(line)
            val isTableRow = line.trim().startsWith("|") && line.trim().endsWith("|")
            // --- Таблица ---
            if (isTableRow && i + 1 < lines.size && Regex("""^\s*\|?\s*:?-+.*\|""").containsMatchIn(lines[i + 1])) {
                flushParagraph()
                val header = line.trim().trim('|').split("|").map { it.trim() }
                i++
                // skip separator
                i++
                val rows = mutableListOf<List<String>>()
                while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                    val row = lines[i].trim().trim('|').split("|").map { it.trim() }
                    rows.add(row)
                    i++
                }
                blocks.add(MarkdownBlock.Table(header, rows))
                continue
            }
            // --- Изображение ---
            if (imageMatch != null) {
                flushParagraph()
                val alt = imageMatch.groupValues[1]
                val url = imageMatch.groupValues[2]
                blocks.add(MarkdownBlock.Image(alt, url))
                i++
                continue
            }
            // --- Заголовок ---
            if (headerMatch != null) {
                flushParagraph()
                val level = headerMatch.groupValues[1].length
                val text = headerMatch.groupValues[2]
                blocks.add(MarkdownBlock.Header(level, text))
                i++
                continue
            }
            // --- Пустая строка ---
            if (line.trim().isEmpty()) {
                flushParagraph()
                i++
                continue
            }
            // --- Обычный текст ---
            paragraphBuffer.add(line)
            i++
        }
        flushParagraph()
        return blocks
    }

    private fun parseInlines(text: String): List<MarkdownInline> {
        val result = mutableListOf<MarkdownInline>()
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        result.add(MarkdownInline.Bold(text.substring(i + 2, end)))
                        i = end + 2
                    } else {
                        result.add(MarkdownInline.Text(text.substring(i)))
                        break
                    }
                }
                text.startsWith("*", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        result.add(MarkdownInline.Italic(text.substring(i + 1, end)))
                        i = end + 1
                    } else {
                        result.add(MarkdownInline.Text(text.substring(i)))
                        break
                    }
                }
                text.startsWith("~~", i) -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end != -1) {
                        result.add(MarkdownInline.Strikethrough(text.substring(i + 2, end)))
                        i = end + 2
                    } else {
                        result.add(MarkdownInline.Text(text.substring(i)))
                        break
                    }
                }
                else -> {
                    val next = listOf(
                        text.indexOf("**", i).takeIf { it >= 0 } ?: text.length,
                        text.indexOf("*", i).takeIf { it >= 0 } ?: text.length,
                        text.indexOf("~~", i).takeIf { it >= 0 } ?: text.length
                    ).minOrNull() ?: text.length
                    result.add(MarkdownInline.Text(text.substring(i, next)))
                    i = next
                }
            }
        }
        return result
    }
} 