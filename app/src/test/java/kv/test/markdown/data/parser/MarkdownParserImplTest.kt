package kv.test.markdown.data.parser

import kv.test.markdown.domain.model.MarkdownBlock
import kv.test.markdown.domain.model.MarkdownInline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserImplTest {
    private val parser = MarkdownParserImpl()

    @Test
    fun `парсит заголовки всех уровней`() {
        val md = """
# Заголовок 1
## Заголовок 2
### Заголовок 3
#### Заголовок 4
##### Заголовок 5
###### Заголовок 6
""".trim()
        val blocks = parser.parse(md)
        assertEquals(6, blocks.size)
        for (i in 1..6) {
            val block = blocks[i - 1] as MarkdownBlock.Header
            assertEquals(i, block.level)
            assertTrue(block.text.startsWith("Заголовок"))
        }
    }

    @Test
    fun `парсит жирный, курсив и зачёркнутый текст`() {
        val md = "Это **жирный** и *курсив* и ~~зачёркнутый~~ текст."
        val blocks = parser.parse(md)
        assertEquals(1, blocks.size)
        val para = blocks[0] as MarkdownBlock.Paragraph
        val inlines = para.inlines
        assertEquals(7, inlines.size)
        assertEquals(MarkdownInline.Text("Это "), inlines[0])
        assertEquals(MarkdownInline.Bold("жирный"), inlines[1])
        assertEquals(MarkdownInline.Text(" и "), inlines[2])
        assertEquals(MarkdownInline.Italic("курсив"), inlines[3])
        assertEquals(MarkdownInline.Text(" и "), inlines[4])
        assertEquals(MarkdownInline.Strikethrough("зачёркнутый"), inlines[5])
        assertEquals(MarkdownInline.Text(" текст."), inlines[6])
    }

    @Test
    fun `парсит таблицу`() {
        val md = """
| Заголовок | Столбец2 |
|-----------|----------|
| Ячейка1   | Ячейка2  |
| Ячейка3   | Ячейка4  |
""".trim()
        val blocks = parser.parse(md)
        assertEquals(1, blocks.size)
        val table = blocks[0] as MarkdownBlock.Table
        assertEquals(listOf("Заголовок", "Столбец2"), table.header)
        assertEquals(2, table.rows.size)
        assertEquals(listOf("Ячейка1", "Ячейка2"), table.rows[0])
        assertEquals(listOf("Ячейка3", "Ячейка4"), table.rows[1])
    }

    @Test
    fun `парсит изображение`() {
        val md = "![alt text](http://example.com/img.png)"
        val blocks = parser.parse(md)
        assertEquals(1, blocks.size)
        val img = blocks[0] as MarkdownBlock.Image
        assertEquals("alt text", img.alt)
        assertEquals("http://example.com/img.png", img.url)
    }

    @Test
    fun `парсит обычный параграф`() {
        val md = "Просто текст без форматирования."
        val blocks = parser.parse(md)
        assertEquals(1, blocks.size)
        val para = blocks[0] as MarkdownBlock.Paragraph
        assertEquals(1, para.inlines.size)
        assertEquals(MarkdownInline.Text("Просто текст без форматирования."), para.inlines[0])
    }
} 