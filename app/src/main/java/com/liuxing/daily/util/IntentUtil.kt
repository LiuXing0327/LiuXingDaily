package com.liuxing.daily.util

import android.content.Context
import android.content.Intent

object IntentUtil{

    /**
     * 打开界面
     *
     * @param context 上下文
     * @param cls 要打开的界面的类
     */
    fun startActivity(context: Context, cls: Class<*>?) {
        val intent = Intent(context, cls!!)
        context.startActivity(intent)
    }
}