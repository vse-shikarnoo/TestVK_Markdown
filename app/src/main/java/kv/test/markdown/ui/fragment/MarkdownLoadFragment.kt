package kv.test.markdown.ui.fragment

import android.app.Activity
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
import kv.test.markdown.ui.viewmodel.MarkdownLoadViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MarkdownLoadFragment : Fragment() {
    private lateinit var buttonPickFile: Button
    private lateinit var buttonDownload: Button
    private lateinit var editTextUrl: EditText

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                loadMarkdownFromUri(uri)
            }
        }
    }

    private val viewModel: MarkdownLoadViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_markdown_load, container, false)
        buttonPickFile = view.findViewById(R.id.buttonPickFile)
        buttonDownload = view.findViewById(R.id.buttonDownload)
        editTextUrl = view.findViewById(R.id.editTextUrl)

        buttonPickFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/*"
            }
            pickFileLauncher.launch(intent)
        }

        buttonDownload.setOnClickListener {
            val url = editTextUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                downloadMarkdownFromUrl(url)
            } else {
                Toast.makeText(requireContext(), "Введите URL", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.markdown.observe(viewLifecycleOwner) { text ->
            // Можно обновлять UI, если потребуется
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.setError(null)
            }
        }
    }

    private fun loadMarkdownFromUri(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val text = inputStream?.bufferedReader().use { it?.readText() } ?: ""
            viewModel.setMarkdown(text)
            openViewFragment(text, uri)
        } catch (e: Exception) {
            viewModel.setError("Ошибка чтения файла")
        }
    }

    private fun downloadMarkdownFromUrl(urlStr: String) {
        thread {
            try {
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val text = reader.readText()
                reader.close()
                // Сохраняем во внутреннее хранилище
                val fileName = "downloaded_${System.currentTimeMillis()}.md"
                val file = File(requireContext().filesDir, fileName)
                file.writeText(text)
                val fileUri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".provider",
                    file
                )
                requireActivity().runOnUiThread {
                    viewModel.setMarkdown(text)
                    openViewFragment(text, fileUri)
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    viewModel.setError("Ошибка загрузки по URL")
                }
            }
        }
    }

    private fun openViewFragment(markdown: String, uri: Uri? = null) {
        val fragment = MarkdownViewFragment.newInstance(markdown, uri)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
} 