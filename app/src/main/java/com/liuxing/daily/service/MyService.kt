package com.liuxing.daily.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Author：流星
 * DateTime：2024/10/19 下午10:52
 * Description：
 */
class MyService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }
}