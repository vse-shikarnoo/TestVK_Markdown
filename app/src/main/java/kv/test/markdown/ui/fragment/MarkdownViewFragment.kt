package kv.test.markdown.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kv.test.markdown.R
import kv.test.markdown.ui.view.MarkdownView
import kv.test.markdown.ui.viewmodel.MarkdownViewViewModel

class MarkdownViewFragment : Fragment() {
    private val viewModel: MarkdownViewViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_markdown_view, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val markdown = arguments?.getString(ARG_MARKDOWN_TEXT) ?: ""
        viewModel.setMarkdown(markdown)
        val markdownView = view.findViewById<MarkdownView>(R.id.markdownView)
        viewModel.blocks.observe(viewLifecycleOwner) { blocks ->
            markdownView.setBlocks(blocks)
        }
        val buttonEdit = view.findViewById<FloatingActionButton>(R.id.buttonEdit)
        buttonEdit.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,
                    MarkdownEditFragment.newInstance(viewModel.markdown.value ?: "")
                )
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {
        private const val ARG_MARKDOWN_TEXT = "markdown_text"
        fun newInstance(markdown: String): MarkdownViewFragment {
            val fragment = MarkdownViewFragment()
            val args = Bundle()
            args.putString(ARG_MARKDOWN_TEXT, markdown)
            fragment.arguments = args
            return fragment
        }
    }
} 