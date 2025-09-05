/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.ui.immersive

import androidx.appcompat.app.AppCompatActivity
import com.liuxing.daily.util.ImmersiveUtil

open class ImmersiveActivity : AppCompatActivity() {

    /**
     * 进入沉浸式
     */
    fun enterImmersive() {
        isImmersive = true
        ImmersiveUtil.enterImmersive(this)
    }

    /**
     * 退出沉浸式
     */
    fun exitImmersive() {
        isImmersive = false
        ImmersiveUtil.exitImmersive(this)
    }
}