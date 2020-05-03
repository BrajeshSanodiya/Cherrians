package com.jans.cherrians

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.text.Html
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jans.imageload.DefaultImageLoader
import com.jans.imageload.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Scope


class MyFirebaseMessagingService : FirebaseMessagingService() {
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            if (/* Check if data needs to be processed by long running job */ remoteMessage.data.containsKey("img") && remoteMessage.data.get("img")!!.isNotEmpty()) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob(remoteMessage.data)
            } else {
                handleNow(remoteMessage.data)
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private fun scheduleJob(data: MutableMap<String, String>) {

        GlobalScope.launch(Dispatchers.IO){
            var messageTitle:String=getString(R.string.app_name)
            if(data.containsKey("title"))
                messageTitle=data.get("title").toString()
            var messageDesc:String=getString(R.string.app_name)
            if(data.containsKey("desc"))
                messageDesc=data.get("desc").toString()
            var messageWebUrl:String?=null
            if(data.containsKey("webUrl"))
                messageWebUrl=data.get("webUrl")
            var messageImg: String? =data.get("img")
            var messageLayoutType:Int=Utils.NOTIFICATION_LAYOUT_DEFAULT_SMALL
            if(data.containsKey("layoutType"))
                messageLayoutType=data.get("layoutType")!!.toInt()

            if(messageLayoutType!=Utils.NOTIFICATION_LAYOUT_NONE)

                messageImg?.let {
                    val result =
                        DefaultImageLoader.download(
                            this@MyFirebaseMessagingService,
                            messageImg
                        )
                    if (result is Result.Success) {
                        GlobalScope.launch(Dispatchers.Main) {
                            showNotification(context = this@MyFirebaseMessagingService,title = messageTitle,desc = messageDesc,longBitmap = result.data,webUrl = messageWebUrl,notificationType = messageLayoutType)
                        }
                    }
                }

        }

    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow(data: MutableMap<String, String>) {
        Log.d(TAG, "Short lived task is done.")
        var messageTitle:String=getString(R.string.app_name)
        if(data.containsKey("title"))
            messageTitle=data.get("title").toString()
        var messageDesc:String=getString(R.string.app_name)
        if(data.containsKey("desc"))
            messageDesc=data.get("desc").toString()
        var messageWebUrl:String?=null
        if(data.containsKey("webUrl"))
            messageWebUrl=data.get("webUrl")
        var messageImg:String?=""
        if(data.containsKey("img"))
            messageImg=data.get("img")
        var messageLayoutType:Int=Utils.NOTIFICATION_LAYOUT_DEFAULT_SMALL
        if(data.containsKey("layoutType"))
            messageLayoutType=data.get("layoutType")!!.toInt()

        if(messageLayoutType!=Utils.NOTIFICATION_LAYOUT_NONE)

        showNotification(context = this,title = messageTitle,desc = messageDesc,longBitmap = null,webUrl = messageWebUrl,notificationType = messageLayoutType)

    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun showNotification(context:Context,title:String, desc: String,
                                 longBitmap: Bitmap?, webUrl:String?, notificationType:Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Utils.LAUNCH_FROM_NOTIFY , Utils.LAUNCH_FROM_NOTIFY )
        intent.putExtra(Utils.NOTIFY_BODY,desc)
        intent.putExtra(Utils.NOTIFY_TITLE,title)
      webUrl?.let {
          Log.d(TAG, "webUrl ${webUrl}")
          intent.putExtra(Utils.NOTIFY_WEB_URL , webUrl)
      }


        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)
        val channelId = getString(R.string.default_notification_channel_id)
        val channelName=getString(R.string.default_notification_channel_name)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        //val largeIcon = BitmapFactory.decodeResource(this.resources, R.drawable.splash_logo)


        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)

            .setContentTitle(Html.fromHtml(title))
            .setContentText(Html.fromHtml(desc))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)

        longBitmap?.let {
            notificationBuilder.setLargeIcon(longBitmap)
            if(notificationType==Utils.NOTIFICATION_LAYOUT_DEFAULT_BIG){
                notificationBuilder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(longBitmap)
                        .bigLargeIcon(null)
                )
            }
        }
        if(notificationType==Utils.NOTIFICATION_LAYOUT_DEFAULT_MULTILINE){
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(Html.fromHtml(desc))
            )
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,channelName,NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationID()/* ID of notification */, notificationBuilder.build())
    }

    fun notificationID(): Int {
        val now = Date()
        return SimpleDateFormat("ddHHmmss", Locale.US).format(now).toInt()
    }

    companion object {
        private const val TAG = "Notification"
    }
}
