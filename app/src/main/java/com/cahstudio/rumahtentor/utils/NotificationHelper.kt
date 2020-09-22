package com.cahstudio.rumahtentor.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.cahstudio.rumahtentor.R

class NotificationHelper(base: Context?) : ContextWrapper(base) {
    private var notificationManager: NotificationManager? = null

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        manager!!.createNotificationChannel(channel)
    }

    val manager: NotificationManager?
        get() {
            if (notificationManager == null) {
                notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return notificationManager
        }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getNotification(
        title: String?,
        content: String?,
        sound: Uri?,
        data: Map<String?, String?>?
    ): NotificationCompat.Builder {
        val intent = Intent()
        intent.putExtra("user_id", data?.get("from_user_id"))
        intent.putExtra("name", data?.get("from_name"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val r = RingtoneManager.getRingtone(applicationContext, sound)
        r.play()
        val pendingIntent =
            PendingIntent.getActivity(this, -1, intent, PendingIntent.FLAG_ONE_SHOT)
        return NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setSound(sound)
            .setPriority(NotificationCompat.PRIORITY_MAX)
    }

    companion object {
        private const val CHANNEL_ID = "com.cahstudio.rumahtentor"
        private const val CHANNEL_NAME = "RumahTentor"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }
}