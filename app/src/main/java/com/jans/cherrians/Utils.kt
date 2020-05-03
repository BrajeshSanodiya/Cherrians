package com.jans.cherrians

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent


object Utils {

    const val NOTIFICATION_LAYOUT_NONE: Int = 0
    const val NOTIFICATION_LAYOUT_DEFAULT_SMALL: Int = 1
    const val NOTIFICATION_LAYOUT_DEFAULT_BIG: Int = 2
    const val NOTIFICATION_LAYOUT_DEFAULT_MULTILINE: Int = 3


    val app_platform="android"

    val LAUNCH_FROM_NOTIFY="LAUNCH_FROM_NOTIFY"
    val NOTIFY_BODY="NOTIFY_BODY"
    val NOTIFY_TITLE="NOTIFY_TEXT"
    val NOTIFY_WEB_URL="NOTIFY_WEB_URL"

    fun shareApp(context: Context, msg:String?=null, subject:String?=null) {

        val message: String  = context.resources.getString(R.string.app_share_message)
        val messageLink: String = context.resources.getString(R.string.app_share_link)

        var messageSubject: String = context.resources.getString(R.string.app_share_subject)
        if(subject!=null && !TextUtils.isEmpty(subject)){
            messageSubject=subject
        }

        var body = "$message \n$messageLink"
        if(msg!=null && !TextUtils.isEmpty(msg)){
            body=msg
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, body)
        intent.putExtra(Intent.EXTRA_SUBJECT,messageSubject)
        context.startActivity(Intent.createChooser(intent, "Share"))
    }



    fun sendEmail(context: Context,email: String,subject: String?) {
        var mailSubject = subject
        try {
            if (TextUtils.isEmpty(mailSubject)) {
                mailSubject =context.getString(R.string.feedback_mail_subject) + getAppVersion(context)
            }
            /*val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "message/rfc822"*/
            val sendIntent = Intent(Intent.ACTION_SENDTO)
            sendIntent.data = Uri.parse("mailto:")
            sendIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, mailSubject)
            sendIntent.putExtra(Intent.EXTRA_TEXT, getPhoneInfoForMail(context))
            context.startActivity(Intent.createChooser(sendIntent, "Send email"))
        } catch (e: Exception) {
        }
    }

    private fun getPhoneInfoForMail(context: Context): String? {
            var str =
                """
                App Version : ${getAppVersion(context)}
                Sdk : ${Build.VERSION.SDK_INT}
                Platform : $app_platform
                Brand : ${Build.BRAND}
                Model : ${Build.MODEL}
                """.trimIndent()
            str += "\n-------------------------------------\n\n"
           return str
    }

    fun getAppVersion(context: Context): String {
        try {
            val packageInfo =
                context.packageManager.getPackageInfo(context.packageName, 0)
            if (packageInfo != null) return packageInfo.versionName
        } catch (e: Exception) {
        }
        return "1.0"
    }

    fun getAppVersionCode(context: Context): String {
        try {
            val packageInfo =
                context.packageManager.getPackageInfo(context.packageName, 0)
            if (packageInfo != null) return ""+packageInfo.versionCode
        } catch (e: Exception) {
        }
        return "1"
    }

    fun openAppStore(context: Context, packageName:String?=null){
        var appPackageName: String =context.getPackageName()

        if(packageName!=null && !TextUtils.isEmpty(packageName)){
            appPackageName=packageName!!
        }

        try {
           context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    fun openBrowser(context: Context,url:String){
        var myUrl=url
        if (!myUrl.startsWith("https://") && !myUrl.startsWith("http://")){
            myUrl = "http://" + myUrl
        }
        val openUrlIntent  = Intent(Intent.ACTION_VIEW, Uri.parse(myUrl))
        // Verify that the intent will resolve to an activity
        if (openUrlIntent .resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(openUrlIntent )
        }
    }

    fun openBrowserChooser(context: Context,url:String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        // Note the Chooser below. If no applications match,
        // Android displays a system message.So here there is no need for try-catch.
        context.startActivity(Intent.createChooser(intent, "Browse with"))
    }

    fun openChromeBrowser(context: Context,url:String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage("com.android.chrome")
        try {
            context.startActivity(intent);
        } catch (ex:ActivityNotFoundException) {
            intent.setPackage(null);
            context.startActivity(Intent.createChooser(intent, "Browse with"))
        }
    }

    fun openChormeTab(context: Context, url: String){
        var builder: CustomTabsIntent.Builder  =  CustomTabsIntent.Builder();
        var customTabsIntent:CustomTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }

    fun checkInternetConnection(context: Context): Boolean {
        val con_manager =context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return (con_manager.activeNetworkInfo != null && con_manager.activeNetworkInfo.isAvailable
                && con_manager.activeNetworkInfo.isConnected)
    }


    fun showDialog(context: Context,title:String,message:String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage(Html.fromHtml(message.trimIndent()))
        val textView = TextView(context)
        textView.setText(Html.fromHtml(title))
        textView.setBackgroundColor(Color.DKGRAY)
        textView.setPadding(20, 20, 20, 20)
        textView.gravity = Gravity.CENTER
        textView.setTextColor(Color.WHITE)
        textView.textSize = 20f
        builder.setCustomTitle(textView)
            .setPositiveButton(android.R.string.ok,
                DialogInterface.OnClickListener { dialog, whichButton ->
                    //finishAffinity();
                })
        builder.show()
    }
}