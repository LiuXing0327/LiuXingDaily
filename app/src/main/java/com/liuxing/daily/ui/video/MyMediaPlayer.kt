package com.liuxing.daily.ui.video

import android.media.MediaPlayer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer


/**
 * Author：流星
 * DateTime：2024/11/29 22:04
 * Description：
 */
class MyMediaPlayer : MediaPlayer(), DefaultLifecycleObserver {

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        pause()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        start()
    }
}


