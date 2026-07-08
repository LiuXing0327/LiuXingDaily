package com.liuxing.daily.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.core.view.MenuProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityAboutBinding
import com.liuxing.daily.ui.compose.theme.DailyTheme
import com.liuxing.daily.ui.compose.theme.DailyThemeManager
import com.liuxing.daily.ui.qrx.QRXActivity
import com.liuxing.daily.ui.updatelog.UpdateLogActivity
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.util.VersionUtil


class AboutActivity : QRXActivity() {

    private lateinit var activityAboutBinding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)/*        enableEdgeToEdge()
                ThemeUtil.applyTheme(this)
                activityAboutBinding = ActivityAboutBinding.inflate(layoutInflater)
                setContentView(activityAboutBinding.root)
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                    insets
                }*/
        //  initData()
        enableEdgeToEdge()
        setContent {

            initCompose()
            LaunchedEffect(Unit) {
                checkStatusBarColorForCompose(
                    topAppBarColor = Color.Transparent
                )
            }

            DailyTheme(
                themeType = DailyThemeManager.currentThemeType,
                themeMode = DailyThemeManager.themeMode,
                isAmoled = DailyThemeManager.isAmoled,
                dynamicColor = DailyThemeManager.isDynamicColor,
            ) {
                val wallpaperBitmap = remember { safeWallpaperBitmap }
                val currentWallpaperAlpha = remember { safeWallpaperAlpha }
                val cardAlpha = remember { safeCardAlpha }

                AboutScreen(
                    wallpaperBitmap = wallpaperBitmap,
                    wallpaperAlpha = currentWallpaperAlpha,
                    cardAlpha = cardAlpha,
                    onBack = ::finish,
                    {
                        MaterialAlertDialogBuilder(this).apply {
                            setTitle("注意事项")
                            setMessage("在加入交流群之前，请注意以下事项：\n1、请遵守群规，文明交流。\n2、不要分享个人隐私。\n3、若有疑问请联系群主或管理员。")
                            setPositiveButton(
                                getString(R.string.sure)
                            ) { _, _ -> joinQQGroup("5XhiuTnwUF3YfNpZauGW2ZItbLnuZ2Xs") }
                            setNegativeButton(getString(R.string.cancel), null)
                            create()
                            show()
                        }
                    }
                )
            }
        }
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
        activityAboutBinding.tvName.text = getString(R.string.author_name)
        activityAboutBinding.tvVersionName.text =
            getString(R.string.current_version, VersionUtil.getVersionName(this))
        val email = getString(R.string.my_email)
        activityAboutBinding.tvEmail.text =
            Html.fromHtml("<a href='mailto:$email'>Email：$email</a>", Html.FROM_HTML_MODE_COMPACT)
        activityAboutBinding.tvJoinGroup.text = getString(R.string.qq_920994447)
        activityAboutBinding.tvJoinGroup.setOnClickListener {


        }
        activityAboutBinding.tvJoinGroup.setOnLongClickListener {
            CopyUtil.copyTextToClipboard(this@AboutActivity, "920994447")
            SnackbarUtil.showSnackbarShort(
                activityAboutBinding.tvGithub, getString(R.string.copy_successful)
            )
            true
        }
        // 设置可点击
        activityAboutBinding.tvEmail.movementMethod = LinkMovementMethod.getInstance()
        activityAboutBinding.tvEmail.setOnLongClickListener {
            CopyUtil.copyTextToClipboard(this@AboutActivity, email)
            SnackbarUtil.showSnackbarShort(
                activityAboutBinding.tvEmail, getString(R.string.copy_successful)
            )
            true
        }
        val sourceCodeUrl = "https://github.com/LiuXing0327/LiuXingDaily"
        activityAboutBinding.tvGithub.text = Html.fromHtml(
            getString(R.string.a_href_a, sourceCodeUrl, sourceCodeUrl), Html.FROM_HTML_MODE_COMPACT
        )
        activityAboutBinding.tvGithub.setOnLongClickListener {
            CopyUtil.copyTextToClipboard(this@AboutActivity, sourceCodeUrl)
            SnackbarUtil.showSnackbarShort(
                activityAboutBinding.tvGithub, getString(R.string.copy_successful)
            )
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
                        this@AboutActivity, UpdateLogActivity::class.java
                    )
                }
                return true
            }

        })
    }

    /****************
     *
     * 发起添加群流程。群号：醒悟官方交流群(920994447) 的 key 为： 5XhiuTnwUF3YfNpZauGW2ZItbLnuZ2Xs
     * 调用 joinQQGroup(5XhiuTnwUF3YfNpZauGW2ZItbLnuZ2Xs) 即可发起手Q客户端申请加群 醒悟官方交流群(920994447)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     */
    private fun joinQQGroup(key: String): Boolean {
        val intent = Intent()
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D$key"))
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
            return true
        } catch (e: Exception) {
            // 未安装手Q或安装的版本不支持
//            SnackbarUtil.showSnackbarShort(
//                activityAboutBinding.tvJoinGroup, "未安装手Q或安装的版本不支持"
//            )
            CopyUtil.copyTextToClipboard(this, "920994447")
            return false
        }
    }
}