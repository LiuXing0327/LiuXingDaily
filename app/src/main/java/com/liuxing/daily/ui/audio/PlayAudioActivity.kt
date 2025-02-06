package com.liuxing.daily.ui.audio

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityPlayAudioBinding
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.viewmodel.DailyAudioPlayerModel
import com.liuxing.daily.viewmodel.DailyViewModel
import com.liuxing.daily.viewmodel.PlayerStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayAudioActivity : AppCompatActivity() {

    private lateinit var activityPlayAudioBinding: ActivityPlayAudioBinding
    private lateinit var dailyViewModel: DailyViewModel
    private var audioPath: String? = ""
    private lateinit var dailyAudioPlayerModel: DailyAudioPlayerModel
    private var isUserUpdateProgress = false
    private var isAudioInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        activityPlayAudioBinding = ActivityPlayAudioBinding.inflate(layoutInflater)
        setContentView(activityPlayAudioBinding.root)
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
        controllerAudio()
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(activityPlayAudioBinding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(this.application)
        dailyAudioPlayerModel = ViewModelProvider(this)[DailyAudioPlayerModel::class.java]
        getAudioPath()
        dailyAudioPlayerModel.playerStatus.observe(this) { status ->
            val drawableRes =
                if (status == PlayerStatus.Playing) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24
            activityPlayAudioBinding.floatingActionButton.setImageResource(drawableRes)
        }
        lifecycle.addObserver(dailyAudioPlayerModel.audioPlayer)
    }

    /**
     * 获取音频路径
     */
    private fun getAudioPath() {
        if (isAudioInitialized) return
        audioPath = intent.getStringExtra("look_daily_audio_path")
        audioPath?.let {
            dailyAudioPlayerModel.audioPlayer.reset()
            dailyAudioPlayerModel.setAudioPath(it)
            isAudioInitialized = true
        }
    }

    /**
     * 用户更新播放进度
     */
    private fun userUpdatePlayerProgress() {
        activityPlayAudioBinding.slider.addOnChangeListener { _, value, fromUser ->
            // 判断是否是用户更新
            if (fromUser) {
                isUserUpdateProgress = true
                val newValue = value.coerceIn(
                    activityPlayAudioBinding.slider.valueFrom,
                    activityPlayAudioBinding.slider.valueTo
                )
                dailyAudioPlayerModel.playerSeekToProgress(newValue.toInt())
                isUserUpdateProgress = false
            }
        }
    }

    /**
     * 更新音频播放进度
     */
    private fun updatePlayerProgress() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 等待音频开始播放
                while (!dailyAudioPlayerModel.audioPlayer.isPlaying) {
                    delay(100)
                }

                // 当音频正在播放时持续更新进度
                while (dailyAudioPlayerModel.audioPlayer.isPlaying) {
                    delay(500) // 每500ms更新一次进度
                    if (!isUserUpdateProgress) {  // 判断是否是用户操作
                        val audioDuration = dailyAudioPlayerModel.audioPlayer.duration.toFloat()
                        // 如果音频的持续时间和Slider的最大值不同，则更新Slider最大值
                        if (audioDuration != activityPlayAudioBinding.slider.valueTo) {
                            activityPlayAudioBinding.slider.valueTo = audioDuration
                        }

                        val currentPosition = dailyAudioPlayerModel.audioPlayer.currentPosition.toFloat()
                        // 更新Slider的当前位置，限制在valueFrom和valueTo之间
                        activityPlayAudioBinding.slider.value =
                            currentPosition.coerceIn(
                                activityPlayAudioBinding.slider.valueFrom,
                                activityPlayAudioBinding.slider.valueTo
                            )
                    }
                }
            }
        }
    }

    /**
     * 控制音频po
     */
    private fun controllerAudio(){
        activityPlayAudioBinding.floatingActionButton.setOnClickListener {
            dailyAudioPlayerModel.togglePlayerStatus()
            updatePlayerProgress()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_play_audio, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_delete) {
            audioPath?.let {
                val fileUtil = FileUtil()
                if (fileUtil.checkFileExists(it)) fileUtil.deleteFile(it)
                dailyViewModel.deleteSelectPathAudio(it)
                finish()
            }
        } else finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentPosition", dailyAudioPlayerModel.audioPlayer.currentPosition)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedPosition = savedInstanceState.getInt("currentPosition", 0)
        dailyAudioPlayerModel.playerSeekToProgress(savedPosition)
    }
}