package com.example.dellxps13.myapplication

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.AppCompatButton
import android.widget.Toast
import com.example.dellxps13.myapplication.LoginTask.AsyncResponse
import org.json.JSONObject
import android.net.ConnectivityManager
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.v4.app.NotificationCompat
import android.support.v7.widget.CardView
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONException
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import android.view.ViewGroup
import android.view.LayoutInflater




class MainActivity : AppCompatActivity() {

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    lateinit var alarmManager: AlarmManager
    lateinit var pendingIntent: PendingIntent
    lateinit var broadcastReceiver: BroadcastReceiver
    lateinit var calendar: Calendar

    val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val loginButton = findViewById<AppCompatButton>(R.id.loginButton)

        calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 20)
        calendar.set(Calendar.MINUTE, 22)
        registerAlarmBroadcast()
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,  calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent)

        loginButton.setOnClickListener {
            var login = findViewById<TextInputEditText>(R.id.login).text.toString()
            var password = findViewById<TextInputEditText>(R.id.password).text.toString()

            if(isInternetAvailable()) {
                LoginTask(object : AsyncResponse {
                    override fun processFinish(output: String) {
                        val jsonObj = JSONObject(output.substring(output.indexOf("{"), output.lastIndexOf("}") + 1))
                        if (jsonObj.getBoolean("success")) {
                            setContentView(R.layout.dashboard)
                        } else {
                            Toast.makeText(context, "Niepoprawny login lub hasło", Toast.LENGTH_LONG).show()
                        }
                    }
                }).execute(login, password)

                GetInfoTask(object : GetInfoTask.AsyncResponse {
                    override fun processFinish(output: String) {
                        val jsonObj = JSONObject(output.substring(output.indexOf("{"), output.lastIndexOf("}") + 1))
                        val clients = jsonObj.getJSONArray("clients")
                        val scroll = findViewById<LinearLayout>(R.id.scroll)

                        val sortedJsonArray = JSONArray()

                        val jsonValues = ArrayList<JSONObject>()
                        for (i in 0 until clients.length()) {
                            jsonValues.add(clients.getJSONObject(i))
                        }
                        Collections.sort(jsonValues, object : Comparator<JSONObject> {
                            //You can change "Name" with "ID" if you want to sort by ID
                            private val KEY_NAME = "day"

                            override fun compare(a: JSONObject, b: JSONObject): Int {
                                var valA = 0
                                var valB = 0

                                try {
                                    valA = a.get(KEY_NAME) as Int
                                    valB = b.get(KEY_NAME) as Int
                                } catch (e: JSONException) {
                                    //do something
                                }

                                return valA.compareTo(valB)
                                //if you want to change the sort order, simply use the following:
                                //return -valA.compareTo(valB);
                            }
                        })

                        for (i in 0 until clients.length()) {
                            sortedJsonArray.put(jsonValues[i])
                        }

                        val sdf = SimpleDateFormat("dd")
                        val currentDay = sdf.format(Date()).toInt()

                        for (i in 0 until sortedJsonArray.length()) {
                            val rec = sortedJsonArray.getJSONObject(i)
                            if(rec.getInt("day") >= currentDay) {
                                scroll.addView(makeCard(rec))
                            }
                        }
                    }
                }).execute()
            } else {
                Toast.makeText(context, "Brak połączenia z internetem", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun makeCard(client : JSONObject) : View{
        val vi = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val card = vi.inflate(R.layout.client_card, null)
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                180
        )
        params.setMargins(20, 25, 20, 25)
        card.layoutParams = params

        // fill in any details dynamically here
        val symbolTextView = card.findViewById(R.id.symbol) as TextView
        val descriptionTextView = card.findViewById<TextView>(R.id.description)
        val opiekunTextView = card.findViewById<TextView>(R.id.opiekun)

        symbolTextView.text =  client.getString("symbol")
        descriptionTextView.text = client.getString("date")
        opiekunTextView.text = client.getString("opiekun")

        return card
    }

    fun makeNotification(view : View) {
        val channelId = "com.example.dellxps13.myapplication"
        val description = "Test desc"

        val intent = Intent(this,MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId,description,NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this,channelId)
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.ic_launcher))
                    .setContentIntent(pendingIntent)
        }else{
            builder = Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.ic_launcher))
                    .setContentIntent(pendingIntent)
        }
        notificationManager.notify(533972,builder.build())
    }

    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    private fun registerAlarmBroadcast() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Toast.makeText(context, "Your Alarm is there", Toast.LENGTH_SHORT).show()
            }
        }

        registerReceiver(broadcastReceiver, IntentFilter("com.example.dellxps13.myapplication"))
        pendingIntent = PendingIntent.getBroadcast(this, 0, Intent("com.example.dellxps13.myapplication"), 0)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

}
