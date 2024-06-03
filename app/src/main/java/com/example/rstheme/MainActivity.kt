package com.example.rstheme

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var loadUrlButton: Button
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var fileChooserParams: WebChromeClient.FileChooserParams? = null

    private val url1 = "https://rstheme.com"
    private val url2 = "https://themeforest.net/user/rs-theme/portfolio"
    private var currentUrl = url1

    companion object {
        private const val FILE_UPLOAD_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the SwipeRefreshLayout, WebView, ProgressBar, and Button
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        loadUrlButton = findViewById(R.id.loadUrlButton)

        // Enable JavaScript
        webView.settings.javaScriptEnabled = true

        // Set a WebViewClient to handle links within the WebView
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefreshLayout.isRefreshing = false
                progressBar.visibility = WebView.GONE
            }
        }

        // Set a WebChromeClient to handle progress and title changes
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.visibility = WebView.VISIBLE
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = WebView.GONE
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                supportActionBar?.title = title
            }

            // For Android 5.0+
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                this@MainActivity.fileChooserParams = fileChooserParams
                try {
                    startActivityForResult(fileChooserParams.createIntent(), FILE_UPLOAD_REQUEST_CODE)
                } catch (e: ActivityNotFoundException) {
                    filePathCallback.onReceiveValue(null)
                }
                return true
            }
        }

        // Set up the SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
        }

        // Load the initial URL
        webView.loadUrl(currentUrl)

        // Set up the button click listener to switch between URLs
        loadUrlButton.setOnClickListener {
            currentUrl = if (currentUrl == url1) url2 else url1
            webView.loadUrl(currentUrl)
        }
    }

    // Handle back button press to navigate back in the WebView
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    // Handle the result of the file upload activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_UPLOAD_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result = if (data != null) {
                    WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                } else {
                    null
                }
                filePathCallback?.onReceiveValue(result)
            } else {
                filePathCallback?.onReceiveValue(null)
            }
            filePathCallback = null
            fileChooserParams = null
        }
    }
}
