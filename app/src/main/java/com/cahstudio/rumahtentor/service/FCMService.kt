package com.blanjaque.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.cahstudio.rumahtentor.R
import com.cahstudio.rumahtentor.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*


class FCMService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendNotificationWithChannel(remoteMessage)
            } else {
                sendNotification(remoteMessage)
            }
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val mRef = FirebaseDatabase.getInstance().reference
            mRef.child("token").child(user.uid).setValue(s)
        }
    }

    protected fun sendNotification(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        var title = ""
        var content: String? = ""
        if (data["status"] == "message") {
            title = "Pesan dari " + data["from_name"]
            var message: String? = ""
            message = if (data["message"]!!.length > 20) {
                data["message"]!!.substring(0, 17) + "..."
            } else {
                data["message"]
            }
            content = message
        }
        val intent = Intent()
        intent.putExtra("user_id", data["from_user_id"])
        intent.putExtra("name", data["from_name"])
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent =
            PendingIntent.getActivity(this, -1, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSount =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder =
            NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(defaultSount)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Random().nextInt(), builder.build())
        val r = RingtoneManager.getRingtone(applicationContext, defaultSount)
        r.play()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected fun sendNotificationWithChannel(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        var title = ""
        var content: String? = ""
        if (data["status"] == "message") {
            title = "Pesan dari " + data["from_name"]
            var message: String? = ""
            message = if (data["message"]!!.length > 20) {
                data["message"]!!.substring(0, 17) + "..."
            } else {
                data["message"]
            }
            content = message
        }
        val helper: NotificationHelper
        val builder: NotificationCompat.Builder
        val defaultSount =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        helper = NotificationHelper(this)
        builder = helper.getNotification(title, content, defaultSount, data)
        helper.manager!!.notify(Random().nextInt(), builder.build())
    }
}