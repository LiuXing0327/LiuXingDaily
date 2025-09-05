/*
 * Copyright (c) 2024 流星
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
}