package kv.test.markdown.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MarkdownLoadViewModel : ViewModel() {
    private val _markdown = MutableLiveData<String>()
    val markdown: LiveData<String> get() = _markdown

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun setMarkdown(text: String) {
        _markdown.value = text
    }

    fun setError(message: String?) {
        _error.value = message
    }
} 