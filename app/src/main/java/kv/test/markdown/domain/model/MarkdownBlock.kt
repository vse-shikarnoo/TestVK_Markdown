package kv.test.markdown.domain.model

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val inlines: List<MarkdownInline>) : MarkdownBlock()
    data class Table(val header: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    data class Image(val alt: String, val url: String) : MarkdownBlock()
}

sealed class MarkdownInline {
    data class Bold(val text: String) : MarkdownInline()
    data class Italic(val text: String) : MarkdownInline()
    data class Strikethrough(val text: String) : MarkdownInline()
    data class Text(val text: String) : MarkdownInline()
} 