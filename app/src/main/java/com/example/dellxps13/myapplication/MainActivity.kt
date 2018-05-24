package com.example.dellxps13.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.AppCompatButton
import android.widget.Toast
import com.example.dellxps13.myapplication.LoginTask.AsyncResponse
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<AppCompatButton>(R.id.loginButton)

        loginButton.setOnClickListener {
            var login = findViewById<TextInputEditText>(R.id.login).text.toString()
            var password = findViewById<TextInputEditText>(R.id.password).text.toString()

            LoginTask(object : AsyncResponse {

                override fun processFinish(output: String) {
                    val jsonObj = JSONObject(output.substring(output.indexOf("{"), output.lastIndexOf("}") + 1))

                    if (jsonObj.getBoolean("success")) {
                        Toast.makeText(context, "Pomyślnie zalogowano do systemu", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Niepoprawny login lub hasło", Toast.LENGTH_LONG).show()
                    }

                }

            }).execute(login, password)
        }

    }

}
