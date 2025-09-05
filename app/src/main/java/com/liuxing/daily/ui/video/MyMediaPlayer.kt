/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.ui.video

import android.media.MediaPlayer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class MyMediaPlayer : MediaPlayer(), DefaultLifecycleObserver {

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        pause()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
    //    start()
    }
}