package com.cahstudio.rumahtentor.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.cahstudio.rumahtentor.R
import java.util.*

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val day = p1?.getIntExtra("day",-1)
        val calendar = Calendar.getInstance()
        if (day != -1){
            if (day == calendar.get(Calendar.DAY_OF_WEEK)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    p0?.let { pusNotificationWithChannel(it) }
                } else {
                    p0?.let { pushNotification(it) }
                }
            }
        }

    }

    private fun pushNotification(context: Context) {
        val title = "Saatnya untuk belajar dan mengajar"
        val content = "Jangan lupa untuk selalu semangat"

        val intent = Intent()
        intent.putExtra("reminder", true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(context, -1, intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSount = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(content)
            .setSound(defaultSount)
            .setAutoCancel(true)
            .setPriority(Notification.PRIORITY_HIGH)
        val helper = NotificationHelper(context)
        helper.manager?.notify(Random().nextInt(), builder.build())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pusNotificationWithChannel(context: Context) {
        val title = "Saatnya untuk belajar dan mengajar"
        val content = "Jangan lupa untuk selalu semangat"

        val builder: NotificationCompat.Builder

        val defaultSount = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val helper = NotificationHelper(context)
        helper.createChannel()
        builder = helper.getNotification(title, content, defaultSount, null)
        helper.manager?.notify(Random().nextInt(), builder.build())
    }
}