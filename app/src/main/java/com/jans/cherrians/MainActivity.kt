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
import com.google.firebase.BuildConfig
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*


class MainActivity : BaseActivity(), WebAppInterface.WebCallback  {
    private val TAG_NOTIFY:String="Notification"
    private val TAG:String="MainActivity"
/*    private val RC_APP_UPDATE: Int =1232
    var mAppUpdateManager:AppUpdateManager?=null*/

    var clearHistory = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()!!.hide()
        setContentView(R.layout.activity_main)

        initWebView()

        loadDataFromIntent(intent)

        //checkForAppUpdate()

        registerForNotificationTopic()
    }

    private fun loadDataFromIntent(intent: Intent?) {
        if ( intent!=null && intent.extras != null && intent!!.extras!!.containsKey(Utils.LAUNCH_FROM_NOTIFY)) {
            val title: String= intent.extras?.getString(Utils.NOTIFY_TITLE)?:"${R.string.app_name}"
            val body: String = intent.extras?.getString(Utils.NOTIFY_BODY)?:"${R.string.app_name}"

            if(webView!=null && intent!!.extras!!.containsKey(Utils.NOTIFY_WEB_URL) && !intent!!.extras!!.getString(Utils.NOTIFY_WEB_URL).isNullOrBlank()){
                val webUrl: String? = intent.extras?.getString(Utils.NOTIFY_WEB_URL)
                Log.d(TAG_NOTIFY, "loadDataFromIntent LAUNCH_FROM_NOTIFY Url: $webUrl title : $title body : $body")
                webView.loadUrl(webUrl)
            }else{
                Log.d(TAG_NOTIFY, "loadDataFromIntent LAUNCH_FROM_NOTIFY Dailog $title $body")
                webView.loadUrl(getString(R.string.app_web_url))
                GlobalScope.launch(Dispatchers.Main) {
                    delay(3000)
                    Utils.showDialog(this@MainActivity,title=title,message =body)
                }
            }
        }else if(intent!=null && intent!!.data!=null && !TextUtils.isEmpty(intent!!.data.toString()) && intent!!.data.toString().contains(resources.getString(R.string.app_web_host))){
            Log.d(TAG_NOTIFY, "loadDataFromIntent Deeplink")
            if( webView!=null && webView.url != intent!!.data.toString())
                webView.loadUrl(intent!!.data.toString())
        }

        else{
            Log.d(TAG_NOTIFY, "loadDataFromIntent Default")
            webView.loadUrl(getString(R.string.app_web_url))
            //webView.loadUrl("https://www.cherrians.com/public/app_script_check.html")
            //webView.loadUrl("file:///android_asset/app_script_check.html")
        }
    }

    private fun registerForNotificationTopic() {
        var topic="REL_AND_ALL"
        if(BuildConfig.DEBUG)
            topic= "DEB_AND_ALL"
        Log.d(TAG_NOTIFY, "Subscribing to $topic topic")
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d(TAG_NOTIFY, "Topic : $topic has been registered")
                }
                else{
                    Log.d(TAG_NOTIFY, "Topic : $topic not registered")
                }
            }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        loadDataFromIntent(intent)
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
    private fun initWebView(){
        //webView!!.loadUrl(url)
        webView!!.addJavascriptInterface(WebAppInterface(this, this),"AndroidInterface")
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        val webSettings = webView!!.settings
        webSettings.databaseEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.javaScriptEnabled = true

        webSettings.useWideViewPort=true
        /*webSettings.loadsImagesAutomatically=true
        webSettings.setSupportZoom(false)
        webSettings.setSupportZoom(true)
        webSettings.setBuiltInZoomControls(true)
        webSettings.setDisplayZoomControls(true)
        */
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.isLongClickable=false
        webView.isFocusable=true
        webView!!.webViewClient = MyWebViewClient()
        webView!!.webChromeClient = MyWebChromeClient()
        webView!!.overScrollMode=View.OVER_SCROLL_ALWAYS
        webView!!.isVerticalScrollBarEnabled=false
        webView!!.isHorizontalScrollBarEnabled=false

        /*webViewRefreshLayout.setOnRefreshListener {
            webView.reload()
        }*/
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
            //webViewRefreshLayout.isRefreshing = false

            if(splashLayout.visibility==View.VISIBLE){
                GlobalScope.launch(Dispatchers.Main) {
                    delay(1000)
                    splashLayout.visibility=View.GONE
                }
            }
            if(webLoadProgress.visibility==View.VISIBLE){
                webLoadProgress.visibility=View.GONE
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
            if (keyCode == KeyEvent.KEYCODE_BACK && webView!!.canGoBack()) {
                Log.d(TAG, "onKeyDown going back")
                webView!!.goBack()
                return true
            }
            else if(webView!=null && webView.url != getString(R.string.app_web_url)){
                webLoadProgress.visibility=View.VISIBLE
                Log.d(TAG, "onKeyDown loading default home")
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
