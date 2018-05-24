package com.example.dellxps13.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.dellxps13.myapplication.LoginTask.AsyncResponse


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val login = "admin"
        val password = "e3FnfuVjy"

        val textOutput = findViewById<TextView>(R.id.textOutput)

        LoginTask(object : AsyncResponse {

            override fun processFinish(output: String) {
                textOutput.text = output
            }

        }).execute(login, password)

    }

}
