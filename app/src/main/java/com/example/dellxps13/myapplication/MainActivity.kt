package com.example.dellxps13.myapplication

import android.app.*
import android.content.Context
import android.content.Intent
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
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONException
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import android.view.LayoutInflater
import android.widget.Switch

class MainActivity : AppCompatActivity() {

    lateinit var alarmManager: AlarmManager
    lateinit var pendingIntent: PendingIntent
    lateinit var calendar: Calendar
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder

    val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val loginButton = findViewById<AppCompatButton>(R.id.loginButton)
        loginButton.setOnClickListener {
            var login = findViewById<TextInputEditText>(R.id.login).text.toString()
            var password = findViewById<TextInputEditText>(R.id.password).text.toString()

            if (isInternetAvailable()) {
                LoginTask(object : AsyncResponse {
                    override fun processFinish(output: String) {
                        val jsonObj = JSONObject(output.substring(output.indexOf("{"), output.lastIndexOf("}") + 1))
                        if (jsonObj.getBoolean("success")) {
                            setContentView(R.layout.dashboard)

                            val switchNotify0days = findViewById<Switch>(R.id.switch_notify_0days)
                            val switchNotify2days = findViewById<Switch>(R.id.switch_notify_2days)

                            setAlarm(switchNotify0days.isChecked, switchNotify2days.isChecked)
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
                            private val KEY_NAME = "day"
                            override fun compare(a: JSONObject, b: JSONObject): Int {
                                var valA = 0
                                var valB = 0
                                try {
                                    valA = a.get(KEY_NAME) as Int
                                    valB = b.get(KEY_NAME) as Int
                                } catch (e: JSONException) {}
                                return valA.compareTo(valB)
                            }
                        })

                        Collections.sort(jsonValues, object : Comparator<JSONObject> {
                            private val KEY_NAME = "month"
                            override fun compare(a: JSONObject, b: JSONObject): Int {
                                var valA = 0
                                var valB = 0
                                try {
                                    valA = a.get(KEY_NAME) as Int
                                    valB = b.get(KEY_NAME) as Int
                                } catch (e: JSONException) {}
                                return valA.compareTo(valB)
                            }
                        })

                        for (i in 0 until clients.length()) {
                            sortedJsonArray.put(jsonValues[i])
                        }

                        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

                        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                        Log.d("-", "Current day: $currentDay Current month: $currentMonth")

                        for (i in 0 until sortedJsonArray.length()) {
                            val rec = sortedJsonArray.getJSONObject(i)
                            if (rec.getInt("day") >= currentDay || rec.getInt("month") > currentMonth) {
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

    private fun makeCard(client: JSONObject): View {
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

        symbolTextView.text = client.getString("symbol")
        descriptionTextView.text = client.getString("date")
        opiekunTextView.text = client.getString("opiekun")

        return card
    }

    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    private fun setAlarm(notify0Days : Boolean, notify2Days: Boolean) {

        intent = Intent(this@MainActivity, AlarmReceiver::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra("notify0days", notify0Days)
        intent.putExtra("notify2days", notify2Days)

        cancelAlarmIfExists(intent)

        pendingIntent = PendingIntent.getBroadcast(this@MainActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 7)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000, 60 * 100000, pendingIntent)
    }

    private fun cancelAlarmIfExists(intent : Intent){
        try {
            val pendingIntent = PendingIntent.getBroadcast(this@MainActivity, 0, intent,0)
            val am = this@MainActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
