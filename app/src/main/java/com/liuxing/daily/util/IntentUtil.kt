package com.liuxing.daily.util

import android.content.Context
import android.content.Intent

object IntentUtil{

    /**
     * 打开界面
     *
     * @param context 上下文
     * @param cls 要打开的界面的类
     * @param params 传递参数的集合，key 参数名，value 参数值
     */
    fun startActivity(
        context: Context,
        cls: Class<*>,
        params: Map<String, Any?> = emptyMap()
    ) {
        val intent = Intent(context, cls)
        params.forEach { (key, value) ->
            when (value) {
                is Int -> intent.putExtra(key, value)
                is Long -> intent.putExtra(key, value)
                is Float -> intent.putExtra(key, value)
                is Double -> intent.putExtra(key, value)
                is Boolean -> intent.putExtra(key, value)
                is String -> intent.putExtra(key, value)
                is CharSequence -> intent.putExtra(key, value)
                else -> Unit
            }
        }
        context.startActivity(intent)
    }
}