package kv.test.markdown.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kv.test.markdown.R
import kv.test.markdown.ui.viewmodel.MarkdownEditViewModel

class MarkdownEditFragment : Fragment() {
    private lateinit var editText: EditText
    private lateinit var buttonSave: Button
    private val viewModel: MarkdownEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_markdown_edit, container, false)
        editText = view.findViewById(R.id.editTextMarkdown)
        buttonSave = view.findViewById(R.id.buttonSave)
        val markdown = arguments?.getString(ARG_MARKDOWN_TEXT) ?: ""
        editText.setText(markdown)
        buttonSave.setOnClickListener {
            viewModel.setMarkdown(editText.text.toString())
            parentFragmentManager.popBackStack()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,
                    MarkdownViewFragment.newInstance(editText.text.toString())
                )
                .commit()
        }
        // TODO: обработка сохранения
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val markdown = arguments?.getString(ARG_MARKDOWN_TEXT) ?: ""
        viewModel.setMarkdown(markdown)
        viewModel.markdown.observe(viewLifecycleOwner) { text ->
            if (editText.text.toString() != text) {
                editText.setText(text)
            }
        }
        buttonSave.setOnClickListener {
            viewModel.setMarkdown(editText.text.toString())
            parentFragmentManager.popBackStack()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,
                    MarkdownViewFragment.newInstance(editText.text.toString())
                )
                .commit()
        }
    }

    companion object {
        private const val ARG_MARKDOWN_TEXT = "markdown_text"
        fun newInstance(markdown: String): MarkdownEditFragment {
            val fragment = MarkdownEditFragment()
            val args = Bundle()
            args.putString(ARG_MARKDOWN_TEXT, markdown)
            fragment.arguments = args
            return fragment
        }
    }
} 