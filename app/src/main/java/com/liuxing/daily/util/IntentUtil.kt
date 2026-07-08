/*
 * Copyright 2024-2026 流星
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liuxing.daily.util

import android.app.Activity
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
        // 如果从非 Activity Context启动，添加 NEW_TASK 标志
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

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