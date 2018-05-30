package com.example.dellxps13.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("-", "ALARM RECEIVED")
        makeNotification(context)
    }

    fun makeNotification(context: Context?) {
        val channelId = "com.example.dellxps13.myapplication"
        val description = "Test desc"

        val intent2 = Intent(context, MainActivity::class.java)
        val pendingIntent2 = PendingIntent.getActivity(context, 1, intent2, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        lateinit var builder : Notification.Builder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(context?.resources, R.drawable.ic_launcher))
                    .setContentIntent(pendingIntent2)
        } else {
            builder = Notification.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(context?.resources, R.drawable.ic_launcher))
                    .setContentIntent(pendingIntent2)
        }
        notificationManager.notify(533972, builder.build())
    }

}