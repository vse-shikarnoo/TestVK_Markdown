package kv.test.markdown.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kv.test.markdown.R
import kv.test.markdown.ui.viewmodel.MarkdownEditViewModel

class MarkdownEditFragment : Fragment() {
    private lateinit var editText: EditText
    private lateinit var buttonSave: Button
    private val viewModel: MarkdownEditViewModel by viewModels()
    private val saveAsLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri ->
        if (uri != null) {
            try {
                requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(editText.text.toString().toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(requireContext(), "Файл успешно сохранён", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка сохранения файла", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
            val fileUri = arguments?.getParcelable<Uri>(ARG_FILE_URI)
            if (fileUri != null) {
                try {
                    requireContext().contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                        outputStream.write(editText.text.toString().toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(requireContext(), "Файл успешно сохранён", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Ошибка сохранения файла", Toast.LENGTH_SHORT).show()
                }
            }
            parentFragmentManager.popBackStack()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,
                    MarkdownViewFragment.newInstance(editText.text.toString(), fileUri)
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
            val fileUri = arguments?.getParcelable<Uri>(ARG_FILE_URI)
            if (fileUri != null) {
                try {
                    requireContext().contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                        outputStream.write(editText.text.toString().toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(requireContext(), "Файл успешно сохранён", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Ошибка сохранения файла", Toast.LENGTH_SHORT).show()
                }
            }
            parentFragmentManager.popBackStack()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,
                    MarkdownViewFragment.newInstance(editText.text.toString(), fileUri)
                )
                .commit()
        }
        val buttonSaveAs = view.findViewById<Button>(R.id.buttonSaveAs)
        buttonSaveAs.setOnClickListener {
            saveAsLauncher.launch("markdown_${System.currentTimeMillis()}.md")
        }
    }

    companion object {
        private const val ARG_MARKDOWN_TEXT = "markdown_text"
        private const val ARG_FILE_URI = "file_uri"
        fun newInstance(markdown: String, fileUri: Uri? = null): MarkdownEditFragment {
            val fragment = MarkdownEditFragment()
            val args = Bundle()
            args.putString(ARG_MARKDOWN_TEXT, markdown)
            if (fileUri != null) args.putParcelable(ARG_FILE_URI, fileUri)
            fragment.arguments = args
            return fragment
        }
    }
} 