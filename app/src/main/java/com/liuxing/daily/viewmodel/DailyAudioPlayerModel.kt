/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuxing.daily.ui.video.MyMediaPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DailyAudioPlayerModel : ViewModel() {
    val audioPlayer = MyMediaPlayer()
    private val _playerStatus = MutableLiveData(PlayerStatus.NotReady)
    val playerStatus: LiveData<PlayerStatus> = _playerStatus
    private var audioPath: String? = null
    private val _currentPosition = MutableLiveData(0)
    val currentPosition = _currentPosition
    private val _duration = MutableLiveData(0)
    val duration: LiveData<Int> = _duration

    init {
        audioPath?.let { loadAudio(it) }
    }

    /**
     * 设置音频路径，并加载音频
     */
    fun setAudioPath(audioPath: String) {
        this.audioPath = audioPath
        loadAudio(audioPath)
    }

    /**
     * 加载音频
     *
     * @param audioPath 音频路径
     */
    private fun loadAudio(audioPath: String) {
        audioPlayer.apply {
            _playerStatus.postValue(PlayerStatus.NotReady)
            setDataSource(audioPath)
            setOnPreparedListener {
                _playerStatus.postValue(PlayerStatus.Playing)
                _duration.postValue(it.duration)
                it.start()
                startUpdatingProgress()
            }
            setOnCompletionListener {
                _playerStatus.postValue(PlayerStatus.Paused)
                _currentPosition.postValue(0)
            }
            prepareAsync()
        }
    }

    /**
     * 设置播放进度
     */
    fun playerSeekToProgress(position: Int) {
        audioPlayer.seekTo(position)
        _currentPosition.postValue(position)
    }

    /**
     * 切换播放状态
     */
    fun togglePlayerStatus() {
        when (_playerStatus.value) {
            PlayerStatus.Playing -> {
                audioPlayer.pause()
                _playerStatus.postValue(PlayerStatus.Paused)
            }

            PlayerStatus.Paused -> {
                audioPlayer.start()
                _playerStatus.postValue(PlayerStatus.Playing)
                startUpdatingProgress()
            }

            else -> return
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }

    private fun startUpdatingProgress() {
        viewModelScope.launch {
            while (_playerStatus.value == PlayerStatus.Playing) {
                _currentPosition.postValue(audioPlayer.currentPosition)
                delay(500) // 每 500 毫秒更新一次进度
            }
        }
    }
}