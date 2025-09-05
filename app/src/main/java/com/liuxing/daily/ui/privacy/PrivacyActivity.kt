package com.liuxing.daily.ui.privacy

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityPrivacyBinding
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.ThemeUtil
import java.net.URL

class PrivacyActivity : AppCompatActivity() {

    private val binding: ActivityPrivacyBinding by lazy {
        ActivityPrivacyBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        setContentView(binding.main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.title = getString(R.string.user_agreement_and_privacy_Policy)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                return if (url.startsWith("mailto:")) {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO)
                        intent.data = url.toUri()
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        CopyUtil.copyTextToClipboard(this@PrivacyActivity, url.split(":")[1])
                    }
                    true
                } else {
                    false
                }
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
                onBackPressedCallback.isEnabled = binding.webView.canGoBack()
            }
        }

        Thread {
            try {
                val url =
                    URL("https://gitee.com/LiuXing0327/app-privacy/raw/master/daily/terms_and_privacy.html")
                val htmlContent = url.readText()

                runOnUiThread {
                    binding.webView.loadDataWithBaseURL(
                        "https://gitee.com/",
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (onBackPressedCallback.isEnabled) {
            binding.webView.goBack()
        } else {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.webView.goBack()
            }

        }
}