package kv.test.markdown.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kv.test.markdown.R
import kv.test.markdown.ui.fragment.MarkdownLoadFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MarkdownLoadFragment())
                .commit()
        }
    }
}