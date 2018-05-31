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
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras
        val notify0days = extras.getBoolean("notify0days", false)
        val notify2days = extras.getBoolean("notify2days", false)

        val sdf = SimpleDateFormat("dd")
        val currentDay = sdf.format(Date()).toInt()

        if (notify0days) {
            checkDeadLines(context, currentDay)
        }
        if (notify2days) {
            checkDeadLines(context, currentDay - 2)
        }

    }

    private fun checkDeadLines(context: Context, currentDay : Int) {
        GetInfoTask(object : GetInfoTask.AsyncResponse {
            override fun processFinish(output: String) {
                val jsonObj = JSONObject(output.substring(output.indexOf("{"), output.lastIndexOf("}") + 1))
                val clients = jsonObj.getJSONArray("clients")

                for (i in 0 until clients.length()) {
                    val rec = clients.getJSONObject(i)
                    if (rec.getInt("day") == currentDay) {
                        makeNotification(context, "Klient: " + rec.getString("symbol") + " Data: " + rec.getString("date"))
                    }
                }
            }
        }).execute()
    }

    fun makeNotification(context: Context, textContent : String) {
        val channelId = "com.example.dellxps13.myapplication"
        val description = "Przypomnienie o terminie"

        val intent2 = Intent(context, MainActivity::class.java)
        val pendingIntent2 = PendingIntent.getActivity(context, 1, intent2, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        lateinit var builder : Notification.Builder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setContentTitle(description)
                    .setContentText(textContent)
                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher))
                    .setContentIntent(pendingIntent2)
        } else {
            builder = Notification.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setContentTitle(description)
                    .setContentText(textContent)
                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher))
                    .setContentIntent(pendingIntent2)
        }
        notificationManager.notify(533972, builder.build())
    }

}