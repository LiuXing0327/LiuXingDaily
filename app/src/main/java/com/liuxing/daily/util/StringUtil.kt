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

import com.google.android.material.textfield.TextInputEditText

object StringUtil {

    /**
     * 检查输入框内容长度
     *
     * @param text 输入框的字符
     * @param length 需要判断的长度
     * @param inputEditText 输入框
     */
    fun checkedEditContentLength(
        text: CharSequence?,
        length: Int,
        inputEditText: TextInputEditText
    ) {
        val currentLength = text?.length ?: 0
        if (currentLength > length) {
            inputEditText.setText(text!!.substring(0, length))
            inputEditText.setSelection(length)
        }
    }

    /**
     * 检查字符串长度并截断
     *
     * @param text 文本
     * @param length 最大长度
     * @return 截断后的文本
     */
    fun checkStringLength(text: String, length: Int): String {
        return if (text.length > length) {
            text.substring(0, length)
        } else {
            text
        }
    }
}