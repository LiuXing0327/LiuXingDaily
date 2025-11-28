/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.util

import android.os.Process.*

/**
 * Utility for app
 */
object AppUtil {

    /**
     * Kill the application process
     */
    fun killApp(){
        killProcess(myPid())
    }
}