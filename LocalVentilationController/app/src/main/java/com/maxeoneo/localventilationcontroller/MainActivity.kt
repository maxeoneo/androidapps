package com.maxeoneo.localventilation

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView: WebView = findViewById(R.id.webview)
        val settings: WebSettings = webView.settings
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val remoteUrl: String = sharedPref.getString("url", "http://192.168.178.46").toString()
        val localUrl = "file:///android_asset/setup-guide.html"

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // If the remote URL loads successfully, do nothing (content is already replaced)
            }

            override fun onReceivedError(
                view: WebView,
                request: android.webkit.WebResourceRequest,
                error: android.webkit.WebResourceError
            ) {
                // On error, show the local page
                view.loadUrl(localUrl)
            }
        }

        // Load local page first
        webView.loadUrl(localUrl)
        // Then try to load remote URL
        webView.loadUrl(remoteUrl)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
