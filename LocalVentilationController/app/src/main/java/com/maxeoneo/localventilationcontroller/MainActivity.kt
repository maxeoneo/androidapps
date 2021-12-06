package com.maxeoneo.localventilationcontroller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebSettings
import android.webkit.WebView
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val webView: WebView = findViewById(R.id.webview)

    val settings: WebSettings = webView.getSettings()
    settings.useWideViewPort = true
    settings.loadWithOverviewMode = true
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true

    val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    val url: String = sharedPref.getString("url", "http://192.168.178.46").toString()
    webView.loadUrl(url);
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    return when (item.itemId) {
      R.id.action_settings -> {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)

        return true;
      };
      else -> super.onOptionsItemSelected(item)
    }
  }
}