package com.liuxing.daily.util

import android.content.Context
import android.content.Intent

object IntentUtil{
    fun startActivity(context: Context, cls: Class<*>?) {
        val intent = Intent(context, cls!!)
        context.startActivity(intent)
    }
}