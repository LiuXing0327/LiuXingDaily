package com.liuxing.daily.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.liuxing.daily.ui.video.MyMediaPlayer

/**
 * Author：流星
 * DateTime：2024/11/29 20:20
 * Description：日记视频播放ViewModel
 */
class DailyVideoPlayerModel : ViewModel() {

    val videoPlayer = MyMediaPlayer()
    private val _progressVisibility = MutableLiveData(View.VISIBLE)
    val progressVisibility = _progressVisibility
    private val _videoDimensions = MutableLiveData(Pair(0, 0))
    val videoDimensions = _videoDimensions
    private var videoPath: String? = null
    private val _playerStatus = MutableLiveData(PlayerStatus.NotReady)
    val playerStatus = _playerStatus

    init {
        videoPath?.let {
            loadVideo(it)
        }
    }

    /**
     * 设置视频路径，并加载视频
     */
    fun setVideoPath(videoPath: String) {
        this.videoPath = videoPath
        loadVideo(videoPath)
    }

    /**
     * 加载视频
     *
     * @param videoPath 视频路径
     */
    private fun loadVideo(videoPath: String) {
        videoPlayer.apply {
            _progressVisibility.postValue(View.VISIBLE)
            _playerStatus.postValue(PlayerStatus.NotReady)
            setDataSource(videoPath)
            setOnPreparedListener {
                _progressVisibility.postValue(View.INVISIBLE)
                _playerStatus.postValue(PlayerStatus.Playing)
                it.start()
            }
            setOnVideoSizeChangedListener { _, width, height ->
                _videoDimensions.postValue(Pair(width, height))
            }
            setOnSeekCompleteListener {
                _progressVisibility.postValue(View.INVISIBLE)
            }
            setOnCompletionListener {
                _playerStatus.postValue(PlayerStatus.Paused)
            }
            prepareAsync()
        }
    }

    /**
     * 更新视频尺寸
     */
    fun updateVideoDimensions() {
        _videoDimensions.value = _videoDimensions.value
    }

    override fun onCleared() {
        super.onCleared()
        videoPlayer.release()
    }

    /**
     * 设置播放进度
     */
    fun playerSeekToProgress(position: Int) {
        _progressVisibility.postValue(View.VISIBLE)
        videoPlayer.seekTo(position)
    }

    /**
     * 切换播放状态
     */
    fun togglePlayerStatus() {
        when (_playerStatus.value) {

            PlayerStatus.Playing -> {
                videoPlayer.pause()
                _playerStatus.postValue(PlayerStatus.Paused)
            }

            PlayerStatus.Paused -> {
                videoPlayer.start()
                _playerStatus.postValue(PlayerStatus.Playing)
            }

            else -> return
        }
    }
}

enum class PlayerStatus {
    Playing,
    Paused,
    NotReady
}