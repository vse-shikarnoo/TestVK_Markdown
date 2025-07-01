package kv.test.markdown.domain.model

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val inlines: List<MarkdownInline>) : MarkdownBlock()
    data class Table(val header: List<List<MarkdownInline>>, val rows: List<List<List<MarkdownInline>>>) : MarkdownBlock()
    data class Image(val alt: String, val url: String) : MarkdownBlock()
    data class ListBlock(val items: List<ListItem>) : MarkdownBlock()
    data class ListItem(val inlines: List<MarkdownInline>, val sublist: ListBlock? = null)
}

sealed class MarkdownInline {
    data class Bold(val text: String) : MarkdownInline()
    data class Italic(val text: String) : MarkdownInline()
    data class Strikethrough(val text: String) : MarkdownInline()
    data class Text(val text: String) : MarkdownInline()
    data class Link(val text: String, val url: String) : MarkdownInline()
} 