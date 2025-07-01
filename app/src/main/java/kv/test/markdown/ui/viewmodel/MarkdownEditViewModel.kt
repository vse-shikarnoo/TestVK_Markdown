package kv.test.markdown.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MarkdownEditViewModel : ViewModel() {
    private val _markdown = MutableLiveData<String>()
    val markdown: LiveData<String> get() = _markdown

    fun setMarkdown(text: String) {
        _markdown.value = text
    }
} 