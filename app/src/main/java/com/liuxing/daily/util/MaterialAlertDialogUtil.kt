/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.util

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object MaterialAlertDialogUtil {

    private const val TAG = "MaterialAlertDialogUtil"

    /**
     * 显示对话框
     *
     * 当 [layoutRes] 不为 null 时，消息内容 [message] 将被忽略。
     *
     * @param context 上下文
     * @param title 标题
     * @param message 消息；若指定了 [layoutRes]，则忽略该参数
     * @param layoutRes 自定义布局资源 ID
     * @param positiveText [onPositive]文本
     * @param onPositive setPositiveButton的回调函数
     * @param negativeText [onNegative]文本
     * @param onNegative setNegativeButton的回调函数
     * @param neutralText [onNeutral]文本
     * @param onNeutral setNeutralButton的回调函数
     * @param onViewCreated 当使用自定义布局时，会在布局创建完成后回调函数，
     *                      允许在此操作 view 或设置监听。
     *
     * @return MaterialAlertDialog
     */
    fun showDialog(
        context: Context,
        title: String? = null,
        message: String? = null,
        layoutRes: Int? = null,
        positiveText: String = "",
        onPositive: (() -> Unit)? = null,
        negativeText: String = "",
        onNegative: (() -> Unit)? = null,
        neutralText: String = "",
        onNeutral: (() -> Unit)? = null,
        onViewCreated: ((View, AlertDialog) -> Unit)? = null
    ): AlertDialog? {
        val activity = (context as? Activity) ?: return null
        if(activity.isFinishing || activity.isDestroyed){
            LogUtil.w(TAG, "showDialog: null activity")
            return null
        }

        var view: View? = null

        MaterialAlertDialogBuilder(activity).apply {
            setTitle(title)
            if (layoutRes != null) {
                view = LayoutInflater.from(context).inflate(layoutRes, null)
                setView(view)
            } else {
                setMessage(message)
            }
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

            if (view != null) {
                onViewCreated?.invoke(view, dialog)
            }
            return dialog
        }
    }
}