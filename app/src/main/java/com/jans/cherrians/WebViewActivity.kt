package com.jans.cherrians

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*

class WebViewActivity : BaseActivity(), WebAppInterface.WebCallback {
    private var webView: WebView? = null
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_webview)
        getSupportActionBar()!!.hide()

        val url: String = getIntent().getStringExtra("url")
        webView = WebView(this)
        webView!!.loadUrl(url)
        webView!!.addJavascriptInterface(WebAppInterface(this, this), "AndroidInterface")
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        val webSettings = webView!!.settings
        webSettings.databaseEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        webView!!.webViewClient = MyWebViewClient()
        webView!!.setWebChromeClient(MyWebChromeClient())
        webView!!.overScrollMode= View.OVER_SCROLL_NEVER
        webView!!.isVerticalScrollBarEnabled=false
        webView!!.isHorizontalScrollBarEnabled=false

        setContentView(webView)
    }

    override fun backScreen() {
       onBackPressed()
    }

    override fun closeScreen() {
        finish()
    }


    override fun closeApp() {
        finishAffinity()
    }

    override fun killApp() {
        finishAndRemoveTask()
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(
            view: WebView,
            url: String
        ) {
            //Calling a javascript function in html page
            //view.loadUrl("javascript:alert(showVersion('called by Android'))");
        }
    }

    private inner class MyWebChromeClient : WebChromeClient() {
        override fun onJsAlert(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            Log.d("LogTag", message)
            result.confirm()
            return true
        }
    }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView!!.canGoBack()) {
            webView!!.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

