/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.crash

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityCrashBinding
import com.liuxing.daily.util.ThemeUtil
import java.io.File

class CrashActivity : AppCompatActivity() {

    private val activityCrashBinding by lazy {
        ActivityCrashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        setContentView(activityCrashBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val crashLog = intent.getStringExtra("crash_log") ?: ""
        activityCrashBinding.btnSend.setOnClickListener {
            if (crashLog.isEmpty()) return@setOnClickListener
            sendCrashEmail(crashLog)
        }

        activityCrashBinding.btnExit.setOnClickListener {
            finish()
        }
    }

    /**
     * 通过邮箱发送崩溃报告
     *
     * @param crashLog 完整崩溃日志
     */
    private fun sendCrashEmail(crashLog: String) {
        val mainText = "醒悟发生异常，请查看附件。"

        val crashFile = File(externalCacheDir, "crash_${System.currentTimeMillis()}.txt")
        crashFile.writeText(crashLog)
        val fileUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", crashFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("1926879119@qq.com"))
            putExtra(Intent.EXTRA_SUBJECT, "醒悟崩溃报告")
            putExtra(Intent.EXTRA_TEXT, mainText)
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "选择邮件应用发送异常报告"))
    }
}