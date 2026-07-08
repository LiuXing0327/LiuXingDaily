/*
 * Copyright 2026 流星
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

import android.view.View
import androidx.compose.material3.SnackbarHostState
import com.google.android.material.floatingtoolbar.FloatingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Snackbar 工具类
 */
object SnackbarUtil {

    /**
     * 显示 Snackbar 短时间
     *
     * @param view    显示 Snackbar 的 View
     * @param message 要显示的文本
     */
    fun showSnackbarShort(view: View?, message: String?) {
        val snackbar = Snackbar.make(view!!, message!!, Snackbar.LENGTH_SHORT)

        // 如果传入的 View 是 FloatingToolbarLayout，则设置锚点。
        if (view is FloatingToolbarLayout) {
            snackbar.setAnchorView(view)
        }

        snackbar.show()
    }

    /**
     * 显示 Snackbar 短时间
     *
     * @param hostState 页面上的 SnackbarHostState
     * @param scope 协程
     * @param message 要显示的文本
     */
    fun showSnackbarShort(hostState: SnackbarHostState, scope: CoroutineScope, message: String?) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()
            hostState.showSnackbar(message = message ?: "")
        }
    }
}