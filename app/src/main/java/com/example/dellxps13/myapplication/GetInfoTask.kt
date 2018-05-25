package com.example.dellxps13.myapplication

import android.os.AsyncTask
import okhttp3.*

class GetInfoTask(delegate: AsyncResponse) : AsyncTask<Void, Void, String>() {

    val del = delegate

    interface AsyncResponse {
        fun processFinish(output: String)
    }

    override fun doInBackground(vararg params: Void?): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url("https://terminy.sprzatania.grobow.pl/api/getinfo.php")
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