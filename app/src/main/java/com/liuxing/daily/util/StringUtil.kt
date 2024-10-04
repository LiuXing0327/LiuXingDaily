package com.liuxing.daily.util

import com.google.android.material.textfield.TextInputEditText

/**
 * Author：流星
 * DateTime：2024/10/4 下午3:14
 * Description：字符串工具类
 */
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