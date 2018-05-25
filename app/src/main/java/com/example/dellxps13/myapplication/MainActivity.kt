package com.example.dellxps13.myapplication

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.AppCompatButton
import android.widget.Toast
import com.example.dellxps13.myapplication.LoginTask.AsyncResponse
import org.json.JSONObject
import android.net.ConnectivityManager
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.widget.LinearLayout
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val loginButton = findViewById<AppCompatButton>(R.id.loginButton)

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
                        for (i in 0 until clients.length()) {
                            val card = CardView(context)
                            val rec = clients.getJSONObject(i)
                            val text = TextView(context)
                            text.text = rec.getString("symbol")
                            card.setCardBackgroundColor(0xff5555)
                            card.setContentPadding(20, 10, 20, 10)
                            card.radius = 4F
                            card.addView(text)
                            scroll.addView(card)
                        }
                    }
                }).execute()
            } else {
                Toast.makeText(context, "Brak połączenia z internetem", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

}
