/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.util

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object MaterialAlertDialogUtil {

    /**
     * 显示对话框
     *
     * @param context 上下文
     * @param message 消息
     * @param positiveText [onPositive]文本
     * @param onPositive setPositiveButton的回调函数
     * @param negativeText [onNegative]文本
     * @param onNegative setNegativeButton的回调函数
     * @param neutralText [onNeutral]文本
     * @param onNeutral setNeutralButton的回调函数
     * @return MaterialAlertDialog
     */
    fun showDialog(
        context: Context,
        message: String,
        positiveText: String = "",
        onPositive: (() -> Unit)? = null,
        negativeText: String = "",
        onNegative: (() -> Unit)? = null,
        neutralText: String = "",
        onNeutral: (() -> Unit)? = null
    ): AlertDialog? {
        val activity = (context as? Activity) ?: return null
        if(activity.isFinishing || activity.isDestroyed){
            LogUtil.w("showDialog: null activity")
            return null
        }
        MaterialAlertDialogBuilder(activity).apply {
            setMessage(message)
            setPositiveButton(positiveText) { _, _ ->
                onPositive?.invoke()
            }
            setNegativeButton(negativeText) { _, _ ->
                onNegative?.invoke()
            }
            setNeutralButton(neutralText) { _, _ ->
                onNeutral?.invoke()
            }
            val dialog = create()
            dialog.show()
            return dialog
        }
    }
}