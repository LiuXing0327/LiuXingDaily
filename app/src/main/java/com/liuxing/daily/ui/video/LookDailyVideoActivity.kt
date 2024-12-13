package com.liuxing.daily.ui.video

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.appbar.AppBarLayout
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityLookDailyVideoBinding
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.viewmodel.DailyVideoPlayerModel
import com.liuxing.daily.viewmodel.DailyViewModel
import com.liuxing.daily.viewmodel.PlayerStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LookDailyVideoActivity : AppCompatActivity() {

    private lateinit var dailyVideoPlayerModel: DailyVideoPlayerModel
    private lateinit var lookDailyVideoBinding: ActivityLookDailyVideoBinding
    private var dailyVideoPath: String? = ""
    private var isUserUpdateProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lookDailyVideoBinding = ActivityLookDailyVideoBinding.inflate(layoutInflater)
        setContentView(lookDailyVideoBinding.root)
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
        initViewModel()
        userUpdatePlayerProgress()
        updatePlayerProgress()
        setVideo()
        changeImmersive()
        controllerVideo()
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(lookDailyVideoBinding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        dailyVideoPlayerModel = ViewModelProvider(this)[DailyVideoPlayerModel::class.java].apply {
            progressVisibility.observe(this@LookDailyVideoActivity) {
                lookDailyVideoBinding.progressIndicator.visibility = it
            }

            videoDimensions.observe(this@LookDailyVideoActivity) {
                lookDailyVideoBinding.videoFrame.post {
                    resizeVideo(it.first, it.second)
                }
            }

            playerStatus.observe(this@LookDailyVideoActivity) {
                when (it) {
                    PlayerStatus.Paused -> lookDailyVideoBinding.videoController.controlIvBt.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@LookDailyVideoActivity,
                            R.drawable.baseline_play_arrow_24
                        )
                    )

                    else -> lookDailyVideoBinding.videoController.controlIvBt.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@LookDailyVideoActivity,
                            R.drawable.baseline_pause_24
                        )
                    )
                }
            }
        }
        lifecycle.addObserver(dailyVideoPlayerModel.videoPlayer)
    }

    /**
     * 设置视频
     */
    private fun setVideo() {
        lookDailyVideoBinding.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (dailyVideoPlayerModel.videoPlayer != null && !dailyVideoPlayerModel.videoPlayer.isPlaying) {
                    dailyVideoPath = intent.getStringExtra("look_daily_video_path")
                    dailyVideoPlayerModel.videoPlayer.reset()
                    dailyVideoPath?.let {
                        dailyVideoPlayerModel.setVideoPath(it)
                    }
                }
                dailyVideoPlayerModel.videoPlayer.setDisplay(holder)
                dailyVideoPlayerModel.videoPlayer.setScreenOnWhilePlaying(true)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }

        })
    }

    /**
     * 刷新视频大小
     *
     * @param width 宽度
     * @param height 高度
     */
    private fun resizeVideo(width: Int, height: Int) {
        if (width == 0 || height == 0) return
        lookDailyVideoBinding.surfaceView.layoutParams = FrameLayout.LayoutParams(
            lookDailyVideoBinding.videoFrame.height * width / height,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
    }

    /**
     * 切换沉浸式
     */
    private fun changeImmersive() {
        lookDailyVideoBinding.surfaceView.setOnClickListener {
            setImmersive()
        }
        lookDailyVideoBinding.main.setOnClickListener {
            setImmersive()
        }
    }

    /**
     * 设置沉浸式
     */
    private fun setImmersive() {
        lookDailyVideoBinding.appBarLayout?.let {
            if (it.visibility == View.VISIBLE) {
                enterImmersive(it)
            } else {
                exitImmersive(it)
            }
        }
    }


    /**
     * 进入沉浸式
     *
     * @param appBarLayout appBarLayout
     */
    private fun enterImmersive(appBarLayout: AppBarLayout) {
        appBarLayout.visibility = View.GONE
        lookDailyVideoBinding.videoController.main.visibility = View.GONE
        isImmersive = true
        lookDailyVideoBinding.main.setBackgroundColor(Color.BLACK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    /**
     * 退出沉浸式
     *
     * @param appBarLayout appBarLayout
     */
    private fun exitImmersive(appBarLayout: AppBarLayout) {
        appBarLayout.visibility = View.VISIBLE
        lookDailyVideoBinding.videoController.main.visibility = View.VISIBLE
        isImmersive = false
        lookDailyVideoBinding.main.setBackgroundColor(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    /**
     * 用户更新播放进度
     */
    private fun userUpdatePlayerProgress() {
        lookDailyVideoBinding.videoController.slider.addOnChangeListener { _, value, fromUser ->
            // 判断是否是用户更新
            if (fromUser) {
                isUserUpdateProgress = true
                val newValue = value.coerceIn(
                    lookDailyVideoBinding.videoController.slider.valueFrom,
                    lookDailyVideoBinding.videoController.slider.valueTo
                )
                dailyVideoPlayerModel.playerSeekToProgress(newValue.toInt())
                isUserUpdateProgress = false
            }
        }
    }

    /**
     * 更新播放进度
     */
    private fun updatePlayerProgress() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 等待视频开始播放
                while (!dailyVideoPlayerModel.videoPlayer.isPlaying) {
                    delay(100)
                }

                while (dailyVideoPlayerModel.videoPlayer.isPlaying) {
                    delay(500)
                    if (!isUserUpdateProgress) {
                        val videoDuration = dailyVideoPlayerModel.videoPlayer.duration.toFloat()
                        if (videoDuration != lookDailyVideoBinding.videoController.slider.valueTo) {
                            lookDailyVideoBinding.videoController.slider.valueTo = videoDuration
                        }
                        val currentPosition =
                            dailyVideoPlayerModel.videoPlayer.currentPosition.toFloat()
                        lookDailyVideoBinding.videoController.slider.value =
                            currentPosition.coerceIn(
                                lookDailyVideoBinding.videoController.slider.valueFrom,
                                lookDailyVideoBinding.videoController.slider.valueTo
                            )
                    }
                }
            }
        }
    }


    /**
     * 控制视频
     */
    private fun controllerVideo() {
        lookDailyVideoBinding.videoController.controlIvBt.setOnClickListener {
            dailyVideoPlayerModel.togglePlayerStatus()
            updatePlayerProgress()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_delete -> {
                val dailyViewModel = DailyViewModel(this.application)
                dailyVideoPath?.let {
                    val fileUtil = FileUtil()
                    if (fileUtil.checkFileExists(it)) {
                        fileUtil.deleteFile(it)
                    }
                    dailyViewModel.deleteSelectPathVideo(it)
                    finish()
                }
            }

            else -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_look_video, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            dailyVideoPlayerModel.updateVideoDimensions()
        }
    }
}