package com.jans.cherrians

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.core.os.postDelayed
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : BaseActivity(), WebAppInterface.WebCallback  {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()!!.hide()
        setContentView(R.layout.activity_main)

        loadWebView("https://www.cherrians.com/?ref=app")
        //loadWebView("https://www.cherrians.com/public/app_script_check.html")
        //loadWebView("file:///android_asset/app_script_check.html")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView(url:String){
        webView!!.loadUrl(url)
        webView!!.addJavascriptInterface(WebAppInterface(this, this),"AndroidInterface")
        WebView.setWebContentsDebuggingEnabled(true)

        val webSettings = webView!!.settings
        webSettings.databaseEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView!!.webViewClient = MyWebViewClient()
        webView!!.webChromeClient = MyWebChromeClient()
        webView!!.overScrollMode=View.OVER_SCROLL_NEVER
        webView!!.isVerticalScrollBarEnabled=false
        webView!!.isHorizontalScrollBarEnabled=false

        webViewRefreshLayout.setOnRefreshListener {
            webView.reload()
        }
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

    fun internetRetry(view:View) {
        if(!Utils.checkInternetConnection(this@MainActivity)){
            showMessage(false)
        }else{
            webNetworkBtn.isEnabled=false
            webNetworkProgress.visibility=View.VISIBLE
            webView.reload()

        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(
            view: WebView,
            url: String
        ) {
            webViewRefreshLayout.isRefreshing = false
            GlobalScope.launch(Dispatchers.Main) {
                delay(1000)
                splashLayout.visibility=View.GONE
            }
        }
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            if(webNetworkLayout.visibility==View.VISIBLE)
                GlobalScope.launch(Dispatchers.Main) {
                    delay(1000)
                    webNetworkProgress.visibility=View.GONE
                    webNetworkLayout.visibility=View.GONE
                }
        }
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if(!Utils.checkInternetConnection(this@MainActivity)){
                showMessage(false)
            }else{
                view!!.loadUrl(url)
            }
            return true
        }
        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            webNetworkLayout.visibility=View.VISIBLE
            webNetworkBtn.isEnabled=true
            // view!!.loadUrl("file:///android_asset/internet_retry.html")
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

    private var backPressedOnce = false
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(splashLayout.visibility==View.GONE){
            if (keyCode == KeyEvent.KEYCODE_BACK && webView!!.canGoBack()) {
                webView!!.goBack()
                return true
            }
            else if(backPressedOnce) {
                return super.onKeyDown(keyCode, event)
            }else{
                backPressedOnce = true
                Toast.makeText(this, "Press BACK again to exit",Toast.LENGTH_SHORT).show()
                Handler().postDelayed(2000) { backPressedOnce = false }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

}
