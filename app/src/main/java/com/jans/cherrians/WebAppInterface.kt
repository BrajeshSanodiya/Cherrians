package com.jans.cherrians

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Build
import android.webkit.JavascriptInterface
import android.widget.Toast
import javax.security.auth.Subject

class WebAppInterface internal constructor(var mContext: Context, webCallback: WebCallback) {
    internal interface WebCallback {
        fun backScreen()
        fun closeScreen()
        fun closeApp()
        fun killApp()
    }

    var pref: SharedPreferences//// 0 - for private mode
    private val webCallback: WebCallback
    var editor: Editor

    @JavascriptInterface
    fun showToast(toast: String?) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun getDeviceVersion(): Int {
        return Build.VERSION.SDK_INT
    }

    @JavascriptInterface
    fun getAppVersion(): String {
        return Utils.getAppVersion(mContext)
    }
    @JavascriptInterface
    fun getAppPlatform(): String {
        return Utils.app_platform
    }

    @JavascriptInterface
    fun setLocalData(key: String?, value: String?) {
        editor.putString(key, value)
        editor.commit()
    }

    @JavascriptInterface
    fun getLocalData(key: String?): String? {
        return pref.getString(key, "")
    }

    @JavascriptInterface
    fun backScreen() {
        webCallback.backScreen()
    }

    @JavascriptInterface
    fun openFullWeb(url: String?) {
        val intent = Intent(mContext, WebViewActivity::class.java)
        intent.putExtra("url", url)
        mContext.startActivity(intent)
    }

    @JavascriptInterface
    fun closeScreen() {
        webCallback.closeScreen()
    }

    @JavascriptInterface
    fun closeApp() {
        webCallback.closeApp()
    }

    @JavascriptInterface
    fun killApp() {
        webCallback.killApp()
    }

    @JavascriptInterface
    fun shareApp() {
        Utils.shareApp(mContext)
    }

    @JavascriptInterface
    fun shareContent(msg: String) {
        Utils.shareApp(mContext, msg)
    }

    @JavascriptInterface
    fun shareContentWithTitle(msg: String, title: String) {
        Utils.shareApp(mContext, msg, title)
    }

    @JavascriptInterface
    fun sendEmail(email:String) {
        Utils.sendEmail(context = mContext,email=email,subject = null)
    }

    @JavascriptInterface
    fun sendEmailWithSubject(email:String,mailSub: String) {
        Utils.sendEmail(context = mContext,email=email,subject=mailSub)
    }

    @get:JavascriptInterface
    val appDeviceInfo: String
        get() = """{
            "app_version": "${Utils.getAppVersion(mContext)}",
            "app_version_code": "${Utils.getAppVersionCode(mContext)}",
            "app_platform": "${Utils.app_platform}",
            "device_version": "${Build.VERSION.SDK_INT}",
            "device_brand": "${Build.BRAND}",
            "device_model": "${Build.MODEL}"
            }"""


    @JavascriptInterface
    fun openAppStore() {
        Utils.openAppStore(mContext)
    }

    @JavascriptInterface
    fun openAppStore(packageName: String) {
        Utils.openAppStore(mContext, packageName)
    }

    @JavascriptInterface
    fun openBrowser(url: String) {
        Utils.openBrowser(mContext, url)
    }

    @JavascriptInterface
    fun openBrowserChooser(url: String) {
        Utils.openBrowserChooser(mContext, url)
    }

    @JavascriptInterface
    fun openChromeBrowser(url: String) {
        Utils.openChromeBrowser(mContext, url)
    }

    @JavascriptInterface
    fun openChormeTab(url: String) {
        Utils.openChormeTab(mContext, url)
    }

    @JavascriptInterface
    fun isNetworkConnected():Boolean {
        return Utils.checkInternetConnection(mContext)
    }


    init {
        pref = mContext.getSharedPreferences("MyPref", 0)
        this.webCallback = webCallback
        editor = pref.edit()
    }
}
