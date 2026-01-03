/*
 * Copyright (c) 2026 流星
 */

package com.liuxing.daily.listener

/**
 * 用于监听“字符串”变化的回调接口
 *
 * 当字符串发生更新时，该接口会触发。
 */
interface StringChangedListener {

    /**
     * 当字符串发生变化时调用。
     *
     * @param newString 新的字符串。
     */
    fun onStringChanged(newString: String)
}