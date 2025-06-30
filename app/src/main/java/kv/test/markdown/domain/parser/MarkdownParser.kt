package kv.test.markdown.domain.parser

import kv.test.markdown.domain.model.MarkdownBlock

interface MarkdownParser {
    fun parse(markdown: String): List<MarkdownBlock>
} 