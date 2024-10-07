package com.liuxing.daily.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.liuxing.daily.databinding.ActivityMainBinding
import com.liuxing.daily.util.CheckAppUpdateUtil
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.VersionUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        /*        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }*/
        val okHttpClient = OkHttpClient()
        val request = Request.Builder().url(ConstUtil.CHECK_APP_VERSION_URL).build()
        val handler = Handler(Looper.getMainLooper())
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string()
                val jsonObject = JSONObject(jsonString.toString())
                val latestVersionCode = jsonObject.getInt("versionCode")
                val currentVersionCode = VersionUtil.getVersionCode(this@MainActivity)
                if (latestVersionCode > currentVersionCode) {
                    handler.post {
                        CheckAppUpdateUtil.checkUpdate(this@MainActivity)
                    }

                }
            }

        })
    }
}