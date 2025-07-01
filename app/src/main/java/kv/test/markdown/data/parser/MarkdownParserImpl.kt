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
            val headerMatch = Regex("""^(#{1,6})\s+(.*)""").find(line)
            val imageMatch = Regex("""!\[(.*?)\]\((.*?)\)""").find(line)
            val isTableRow = line.trim().startsWith("|") && line.trim().endsWith("|")
            val listMatch = Regex("""^\s*([-*+])\s+(.*)""").find(line)
            // --- Таблица ---
            if (isTableRow && i + 1 < lines.size && Regex("""^\s*\|?\s*:?-+.*\|""").containsMatchIn(lines[i + 1])) {
                flushParagraph()
                val header = line.trim().trim('|').split("|").map { parseInlines(it.trim()) }
                i++
                // skip separator
                i++
                val rows = mutableListOf<List<List<MarkdownInline>>>()
                while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                    val row = lines[i].trim().trim('|').split("|").map { parseInlines(it.trim()) }
                    rows.add(row)
                    i++
                }
                blocks.add(MarkdownBlock.Table(header, rows))
                continue
            }
            // --- Список ---
            if (listMatch != null) {
                flushParagraph()
                val (listBlock, nextIndex) = parseList(lines, i, getIndent(lines[i]))
                blocks.add(listBlock)
                i = nextIndex
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
                text.startsWith("[", i) -> {
                    val endText = text.indexOf("]", i + 1)
                    if (endText != -1 && endText + 1 < text.length && text[endText + 1] == '(') {
                        val endUrl = text.indexOf(")", endText + 2)
                        if (endUrl != -1) {
                            val linkText = text.substring(i + 1, endText)
                            val linkUrl = text.substring(endText + 2, endUrl)
                            result.add(MarkdownInline.Link(linkText, linkUrl))
                            i = endUrl + 1
                            continue
                        }
                    }
                    result.add(MarkdownInline.Text("["))
                    i++
                }
                else -> {
                    val next = listOf(
                        text.indexOf("**", i).takeIf { it >= 0 } ?: text.length,
                        text.indexOf("*", i).takeIf { it >= 0 } ?: text.length,
                        text.indexOf("~~", i).takeIf { it >= 0 } ?: text.length,
                        text.indexOf("[", i).takeIf { it >= 0 } ?: text.length
                    ).minOrNull() ?: text.length
                    result.add(MarkdownInline.Text(text.substring(i, next)))
                    i = next
                }
            }
        }
        return result
    }

    private fun getIndent(line: String): Int {
        return line.indexOfFirst { !it.isWhitespace() }.takeIf { it >= 0 } ?: 0
    }

    private fun parseList(lines: List<String>, start: Int, baseIndent: Int): Pair<MarkdownBlock.ListBlock, Int> {
        val items = mutableListOf<MarkdownBlock.ListItem>()
        var i = start
        while (i < lines.size) {
            val line = lines[i]
            val indent = getIndent(line)
            val m = Regex("""^\s*([-*+])\s+(.*)""").find(line)
            if (m != null && indent == baseIndent) {
                val content = m.groupValues[2]
                // Проверяем, есть ли вложенный список
                var sublist: MarkdownBlock.ListBlock? = null
                val next = i + 1
                if (next < lines.size) {
                    val nextIndent = getIndent(lines[next])
                    val nextMatch = Regex("""^\s*([-*+])\s+(.*)""").find(lines[next])
                    if (nextMatch != null && nextIndent > baseIndent) {
                        val (sub, newIndex) = parseList(lines, next, nextIndent)
                        sublist = sub
                        i = newIndex - 1
                    }
                }
                items.add(MarkdownBlock.ListItem(parseInlines(content), sublist))
                i++
            } else if (m != null && indent > baseIndent) {
                // вложенный список, но уже обработан выше
                i++
            } else {
                break
            }
        }
        return MarkdownBlock.ListBlock(items) to i
    }
} 