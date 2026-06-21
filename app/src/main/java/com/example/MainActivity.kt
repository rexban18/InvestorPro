package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.databinding.ActivityMainBinding
import com.example.R

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val baseUrl = "https://investorpro.shop/"
    private var lastUrl = baseUrl
    private lateinit var noInternetView: View

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noInternetView = binding.root.findViewById(R.id.noInternetLayout)

        setupWebView()
        setupWebViewClient()
        setupListeners()
        checkInternetAndLoad()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowContentAccess = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webView, true)
    }

    private fun setupWebViewClient() {
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.GONE
                url?.let { lastUrl = it }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (url.startsWith(baseUrl)) {
                    return false
                }
                // Open external in browser
                android.content.Intent(android.content.Intent.ACTION_VIEW, request.url).also {
                    startActivity(it)
                }
                return true
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                showNoInternet()
            }
        }
    }

    private fun setupListeners() {
        noInternetView.findViewById<View>(R.id.btnRetry).setOnClickListener {
            checkInternetAndLoad()
        }
    }

    private fun checkInternetAndLoad() {
        if (isInternetAvailable()) {
            binding.webView.visibility = View.VISIBLE
            noInternetView.visibility = View.GONE
            binding.webView.loadUrl(lastUrl)
        } else {
            showNoInternet()
        }
    }

    private fun showNoInternet() {
        binding.webView.visibility = View.GONE
        noInternetView.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Exit App?")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes") { _, _ -> super.onBackPressed() }
                .setNegativeButton("No", null)
                .show()
        }
    }
}
