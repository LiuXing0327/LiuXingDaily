package com.liuxing.daily.ui.updatelog

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityUpdateLogBinding

class UpdateLogActivity : AppCompatActivity() {

    private lateinit var activityUpdateLogBinding: ActivityUpdateLogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityUpdateLogBinding = ActivityUpdateLogBinding.inflate(layoutInflater)
        setContentView(activityUpdateLogBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initData()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        setActionBar()
        getUpdateLog()
    }

    /**
     * 初始化工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(activityUpdateLogBinding.toolbar)
        this.supportActionBar?.setDisplayShowTitleEnabled(false)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        "更新日志".also { activityUpdateLogBinding.toolbar.title = it }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 获取更新日志
     */
    private fun getUpdateLog() {
        val assets = assets
        val inputStream = assets.open("UpdateLogText.txt")
        val bytes = ByteArray(inputStream.available())
        var length: Int
        val sb = StringBuilder()
        while ((inputStream.read(bytes).also { length = it }) != -1) {
            sb.append(String(bytes, 0, length))
        }
        inputStream.close()
        activityUpdateLogBinding.tvUpdateLog.text = sb.toString()
    }
}