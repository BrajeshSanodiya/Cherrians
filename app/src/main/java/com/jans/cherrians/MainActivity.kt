package com.jans.cherrians

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
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
/*    private val RC_APP_UPDATE: Int =1232
    var mAppUpdateManager:AppUpdateManager?=null*/

    var clearHistory = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        println("MainActivity : onCreate")
        super.onCreate(savedInstanceState)

        println("MainActivity : onCreate2")
        getSupportActionBar()!!.hide()
        setContentView(R.layout.activity_main)

        var loadUrl:String=this.resources.getString(R.string.app_web_url)
        val data: Uri? = intent.data
        if(data!=null && !TextUtils.isEmpty(data.toString()) && data.toString().contains(resources.getString(R.string.app_web_host))){
            loadUrl= data.toString()
        }
        //Snackbar.make(rootLayout,loadUrl,Snackbar.LENGTH_LONG).show()
        loadWebView(loadUrl)
        //loadWebView("https://www.cherrians.com/public/app_script_check.html")
        //loadWebView("file:///android_asset/app_script_check.html")

        //checkForAppUpdate()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if(intent!=null){
            val data: Uri? = intent!!.data
            if(webView!=null && data!=null && !TextUtils.isEmpty(data.toString()) && data.toString().contains(resources.getString(R.string.app_web_host))){
                if(webView.url != data.toString())
                    webView.loadUrl(data.toString())
            }
        }
    }

/*
    var installStateUpdatedListener: InstallStateUpdatedListener =
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(state: InstallState) {
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate()
                } else if (state.installStatus() == InstallStatus.INSTALLED) {
                    if (mAppUpdateManager != null) {
                        mAppUpdateManager!!.unregisterListener(this)
                    }
                } else {
                    Log.i(
                        "MainActivity",
                        "InstallStateUpdatedListener: state: " + state.installStatus()
                    )
                }
            }
        }

    private fun popupSnackbarForCompleteUpdate() {
        val snackbar = Snackbar.make(
            findViewById(R.id.rootLayout),
            "New app is ready!",
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction(
            "Install"
        ) { view: View? ->
            if (mAppUpdateManager != null) {
                mAppUpdateManager!!.completeUpdate()
            }
        }
        snackbar.setActionTextColor(resources.getColor(R.color.colorAccent))
        snackbar.show()
    }

    private fun checkForAppUpdate() {
        mAppUpdateManager = AppUpdateManagerFactory.create(this);
        mAppUpdateManager!!.registerListener(installStateUpdatedListener);
        mAppUpdateManager!!
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE  && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    mAppUpdateManager!!.startUpdateFlowForResult(appUpdateInfo,IMMEDIATE,this,RC_APP_UPDATE)
                }
               else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    mAppUpdateManager!!.startUpdateFlowForResult(appUpdateInfo,FLEXIBLE,this,RC_APP_UPDATE)
                }
                else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED){
                    popupSnackbarForCompleteUpdate();
                }
            }
    }
*/

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
            if (clearHistory)
            {
                clearHistory = false;
                webView.clearHistory();
            }
            webViewRefreshLayout.isRefreshing = false
            GlobalScope.launch(Dispatchers.Main) {
                delay(1000)
                splashLayout.visibility=View.GONE
            }
            super.onPageFinished(view, url);
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
            /*Snackbar.make(rootLayout,webView.url+"\n"+resources.getString(R.string.app_web_url),Snackbar.LENGTH_LONG).show()
              Toast.makeText(this,"hi ${webView.url == getString(R.string.app_web_url)}",Toast.LENGTH_LONG).show()*/
            if (keyCode == KeyEvent.KEYCODE_BACK && webView!!.canGoBack()) {
                webView!!.goBack()
                return true
            }
            else if(webView!=null && webView.url != getString(R.string.app_web_url)){
                clearHistory = true
                webView.loadUrl(resources.getString(R.string.app_web_url))
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


   /* override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != Activity.RESULT_OK) {
                Log.e("MainActivity", "onActivityResult: app download failed")
            }
        }
    }*/

}
