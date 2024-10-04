package com.liuxing.daily.ui.about

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityAboutBinding
import com.liuxing.daily.ui.updatelog.UpdateLogActivity
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.SnackbarUtil

class AboutActivity : AppCompatActivity() {

    private lateinit var activityAboutBinding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityAboutBinding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(activityAboutBinding.root)
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
        getAboutText()
        aboutAuthor()
        initMenu()
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(activityAboutBinding.toolbar)
        this.supportActionBar?.setDisplayShowTitleEnabled(false)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (getString(R.string.about) + getString(R.string.app_name)).also {
            activityAboutBinding.toolbar.title = it
        }
    }

    /**
     * 获取关于文本
     */
    private fun getAboutText() {
        val assets = assets
        val inputStream = assets.open("AboutText.txt")
        // 避免乱码
        val bytes = ByteArray(inputStream.available())
        var length: Int
        val sb = StringBuilder()
        while ((inputStream.read(bytes).also { length = it }) != -1) {
            sb.append(String(bytes, 0, length))
        }
        inputStream.close()
        activityAboutBinding.tvAboutText.text = sb.toString()
    }

    /**
     * 关于作者
     */
    private fun aboutAuthor() {
        activityAboutBinding.tvName.text = "\n\n作者：流星"
        val email = "1926879119@qq.com"
        activityAboutBinding.tvEmail.text =
            Html.fromHtml("<a href='mailto:$email'>Email：$email</a>", Html.FROM_HTML_MODE_COMPACT)
        // 设置可点击
        activityAboutBinding.tvEmail.movementMethod = LinkMovementMethod.getInstance()
        activityAboutBinding.tvEmail.setOnLongClickListener {
            CopyUtil.copyTextToClipboard(this@AboutActivity, email)
            SnackbarUtil.showSnackbarShort(activityAboutBinding.tvEmail, "复制成功")
            true
        }
         val sourceCodeUrl = "https://github.com/LiuXing0327/LiuXingDaily"
        activityAboutBinding.tvGithub.text =
            Html.fromHtml("<a href='$sourceCodeUrl'>开源地址：$sourceCodeUrl</a>", Html.FROM_HTML_MODE_COMPACT)
        activityAboutBinding.tvGithub.setOnLongClickListener {
            CopyUtil.copyTextToClipboard(this@AboutActivity, sourceCodeUrl)
            SnackbarUtil.showSnackbarShort(activityAboutBinding.tvGithub, "复制成功")
            true
        }
        // 设置可点击
        activityAboutBinding.tvGithub.movementMethod = LinkMovementMethod.getInstance()
    }

    // 初始化菜单
    private fun initMenu() {
        val menuHost = this
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_about, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    android.R.id.home -> finish()

                    R.id.item_update_log -> IntentUtil.startActivity(
                        this@AboutActivity,
                        UpdateLogActivity::class.java
                    )
                }
                return true
            }

        })
    }
}