package kv.test.markdown.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kv.test.markdown.data.parser.MarkdownParserImpl
import kv.test.markdown.domain.model.MarkdownBlock

class MarkdownViewViewModel : ViewModel() {
    private val _markdown = MutableLiveData<String>()
    val markdown: LiveData<String> get() = _markdown

    private val _blocks = MutableLiveData<List<MarkdownBlock>>()
    val blocks: LiveData<List<MarkdownBlock>> get() = _blocks

    fun setMarkdown(text: String) {
        _markdown.value = text
        _blocks.value = MarkdownParserImpl().parse(text)
    }
} 