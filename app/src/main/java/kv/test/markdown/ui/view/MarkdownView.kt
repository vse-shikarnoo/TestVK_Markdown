package kv.test.markdown.ui.view

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.StrikethroughSpan
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import kv.test.markdown.domain.model.*

class MarkdownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
    }

    fun setBlocks(blocks: List<MarkdownBlock>) {
        removeAllViews()
        for ((index, block) in blocks.withIndex()) {
            val view = when (block) {
                is MarkdownBlock.Header -> createHeader(block)
                is MarkdownBlock.Paragraph -> createParagraph(block)
                is MarkdownBlock.Table -> createTable(block)
                is MarkdownBlock.Image -> createImage(block)
                is MarkdownBlock.ListBlock -> createList(block)
            }
            // Добавляем вертикальный отступ между блоками
            val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            params.topMargin = if (index == 0) 0 else 16
            view.layoutParams = params
            addView(view)
        }
    }

    private fun createHeader(block: MarkdownBlock.Header): View {
        val tv = TextView(context)
        tv.text = block.text
        tv.setTypeface(null, Typeface.BOLD)
        tv.setPadding(0, 24, 0, 8)
        tv.textSize = when (block.level) {
            1 -> 26f
            2 -> 22f
            3 -> 20f
            4 -> 18f
            5 -> 16f
            else -> 14f
        }
        return tv
    }

    private fun createParagraph(block: MarkdownBlock.Paragraph): View {
        val tv = TextView(context)
        tv.text = buildSpannable(block.inlines)
        tv.setPadding(0, 4, 0, 4)
        return tv
    }

    private fun buildSpannable(inlines: List<MarkdownInline>): CharSequence {
        val builder = SpannableStringBuilder()
        var cursor = 0
        for (inline in inlines) {
            val start = builder.length
            when (inline) {
                is MarkdownInline.Bold -> {
                    builder.append(inline.text)
                    builder.setSpan(StyleSpan(Typeface.BOLD), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                is MarkdownInline.Italic -> {
                    builder.append(inline.text)
                    builder.setSpan(StyleSpan(Typeface.ITALIC), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                is MarkdownInline.Strikethrough -> {
                    builder.append(inline.text)
                    builder.setSpan(StrikethroughSpan(), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                is MarkdownInline.Link -> {
                    val spanStart = builder.length
                    builder.append(inline.text)
                    builder.setSpan(object : android.text.style.ClickableSpan() {
                        override fun onClick(widget: View) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(inline.url))
                                widget.context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    }, spanStart, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                is MarkdownInline.Text -> builder.append(inline.text)
            }
            cursor = builder.length
        }
        return builder
    }

    private fun createTable(block: MarkdownBlock.Table): View {
        val table = TableLayout(context)
        table.setPadding(0, 8, 0, 8)
        // Header
        val headerRow = TableRow(context)
        for (cell in block.header) {
            val tv = TextView(context)
            tv.text = cell
            tv.setTypeface(null, Typeface.BOLD)
            tv.setPadding(8, 4, 8, 4)
            headerRow.addView(tv)
        }
        table.addView(headerRow)
        // Rows
        for (row in block.rows) {
            val tableRow = TableRow(context)
            for (cell in row) {
                val tv = TextView(context)
                tv.text = cell
                tv.setPadding(8, 4, 8, 4)
                tableRow.addView(tv)
            }
            table.addView(tableRow)
        }
        return table
    }

    private fun createImage(block: MarkdownBlock.Image): View {
        val imageView = ImageView(context)
        imageView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        // Загрузка изображения стандартными средствами
        Thread {
            try {
                val url = java.net.URL(block.url)
                val bmp = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream())
                post {
                    imageView.setImageBitmap(bmp)
                }
            } catch (e: Exception) {
                // Ошибка загрузки
            }
        }.start()
        imageView.contentDescription = block.alt
        imageView.adjustViewBounds = true
        imageView.setPadding(0, 8, 0, 8)
        return imageView
    }

    private fun createList(block: MarkdownBlock.ListBlock): View {
        val layout = LinearLayout(context)
        layout.orientation = VERTICAL
        for (item in block.items) {
            val tv = TextView(context)
            tv.text = buildSpannable(item)
            tv.setPadding(32, 0, 0, 0)
            tv.text = "• " + tv.text
            tv.setLineSpacing(0f, 1.1f)
            tv.setOnClickListener(null)
            tv.movementMethod = android.text.method.LinkMovementMethod.getInstance()
            layout.addView(tv)
        }
        return layout
    }
} 