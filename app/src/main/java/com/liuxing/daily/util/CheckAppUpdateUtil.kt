package com.liuxing.daily.util

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liuxing.daily.R
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object CheckAppUpdateUtil {

    /**
     * 检查更新
     */
    fun checkUpdate(context: Context) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.dialog_progress_layout, null, false)
        val hintDialog = MaterialAlertDialogBuilder(context)
        hintDialog.setView(view)
        hintDialog.setCancelable(false)
        hintDialog.create()
        val alertDialog = hintDialog.show()
        val client = OkHttpClient()
        val request = Request.Builder().url(ConstUtil.CHECK_APP_VERSION_URL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                alertDialog.dismiss()
                Handler(Looper.getMainLooper()).post {
                    MaterialAlertDialogBuilder(context)
                        .setMessage(context.getString(R.string.failed_to_check_for_updates))
                        .setPositiveButton(context.getString(R.string.sure),null)
                        .create()
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                alertDialog.dismiss()
                val jsonString = response.body?.string()
                val jsonObject = JSONObject(jsonString.toString())
                val latestVersionCode = jsonObject.getInt("versionCode")
                val latestVersionName = jsonObject.getString("versionName")
                val releaseNotes = jsonObject.getString("releaseNotes")
                val downloadUrl = jsonObject.getString("downloadUrl")
                val currentVersionCode =
                    VersionUtil.getVersionCode(context)
                Handler(Looper.getMainLooper()).post {
                    try {
                        if (latestVersionCode > currentVersionCode) {
                            val updateDialog = MaterialAlertDialogBuilder(context)
                            updateDialog.setTitle(context.getString(R.string.new_version) + "：$latestVersionName")
                            updateDialog.setMessage(releaseNotes)
                            updateDialog.setPositiveButton(
                                context.getString(R.string.sure),
                                DialogInterface.OnClickListener { dialog, which ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                    context.startActivity(intent)
                                })
                            updateDialog.setNegativeButton(context.getString(R.string.cancel), null)
                            updateDialog.create()
                            updateDialog.show()
                        } else {
                            Handler(Looper.getMainLooper()).post {
                                MaterialAlertDialogBuilder(context)
                                    .setMessage(context.getString(R.string.it_is_the_latest_version))
                                    .setPositiveButton(context.getString(R.string.sure),null)
                                    .create()
                                    .show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Handler(Looper.getMainLooper()).post {
                            MaterialAlertDialogBuilder(context)
                                .setMessage(context.getString(R.string.failed_to_check_for_updates))
                                .setPositiveButton(context.getString(R.string.sure),null)
                                .create()
                                .show()
                        }
                    }
                }
            }
        })
    }

}