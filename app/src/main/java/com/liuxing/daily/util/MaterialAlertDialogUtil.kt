package com.liuxing.daily.util

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Author：流星
 * DateTime：2025/4/28 11:38
 * Description：对话框工具类
 */
object MaterialAlertDialogUtil {

    /**
     * 显示带有 PositiveButton 的对话框
     *
     * @param context 上下文
     * @param message 消息
     * @param positiveText 按钮文本
     * @param onPositive setPositiveButton的回调函数
     */
    fun showPositiveDialog(
        context: Context,
        message: String,
        positiveText: String,
        onPositive: (() -> Unit)?
    ) {
        MaterialAlertDialogBuilder(context).apply {
            setMessage(message)
            setPositiveButton(positiveText) { _, _ ->
                onPositive?.invoke()
            }
            create()
            show()
        }
    }
}