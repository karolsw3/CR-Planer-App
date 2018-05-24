package com.example.dellxps13.myapplication

import android.os.AsyncTask
import okhttp3.*

class LoginTask(delegate: AsyncResponse) : AsyncTask<String, Void, String>() {

    val del = delegate

    interface AsyncResponse {
        fun processFinish(output: String)
    }

    override fun doInBackground(vararg params: String?): String? {
        val client = OkHttpClient()

        val formBody = FormBody.Builder()
                .add("login", params[0])
                .add("password", params[1])
                .build()
        val request = Request.Builder()
                .url("https://terminy.sprzatania.grobow.pl/api/login.php")
                .post(formBody)
                .build()
        val response = client.newCall(request).execute()
        return response.body()?.string()
        //return json
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        del.processFinish(result.toString())
    }
}