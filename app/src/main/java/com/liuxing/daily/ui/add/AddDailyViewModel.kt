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

package com.liuxing.daily.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * 添加日记界面的 ViewModel
 */
class AddDailyViewModel : ViewModel() {
    var uiState by mutableStateOf(AddDailyUiState())
        private set

    /**
     * 更新标题
     *
     * @param title 标题
     */
    fun updateTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    /**
     * 更新内容
     *
     * @param content 内容
     */
    fun updateContent(content: String) {
        uiState = uiState.copy(content = content)
    }

    /**
     * 初始化编辑界面状态
     *
     * @param title 标题
     * @param content 内容
     * @param dateTime 日期时间
     * @param moodIndex 心情索引
     * @param weatherIndex 天气索引
     * @param label 标签
     * @param backgroundColorIndex 背景颜色索引
     * @param isLocked 是否锁定
     * @param isPinned 是否置顶
     * @param singlePassword 密码
     */
    fun initEditState(
        title: String,
        content: String,
        dateTime: String,
        moodIndex: Int,
        weatherIndex: Int,
        label: String,
        backgroundColorIndex: Int,
        isLocked: Boolean,
        isPinned: Boolean = false,
        singlePassword: String = ""
    ) {
        uiState = uiState.copy(
            title = title,
            content = content,
            dateTimeText = dateTime,
            moodIndex = moodIndex,
            weatherIndex = weatherIndex,
            label = label,
            backgroundColorIndex = backgroundColorIndex,
            isLocked = isLocked,
            isPinned = isPinned,
            singlePassword = singlePassword,
            isTitleVisible = title.isNotEmpty()
        )
    }

    /**
     * 更新日期时间
     *
     * @param dateTime 日期时间
     */
    fun updateDateTime(dateTime: String) {
        uiState = uiState.copy(dateTimeText = dateTime)
    }

    /**
     * 更新字数
     *
     * @param count 字数
     */
    fun updateWordCount(count: Int) {
        uiState = uiState.copy(wordCount = count)
    }

    /**
     * 更新心情
     *
     * @param index 心情索引
     */
    fun updateMood(index: Int) {
        uiState = uiState.copy(moodIndex = index)
    }

    /**
     * 更新天气
     *
     * @param index 天气索引
     */
    fun updateWeather(index: Int) {
        uiState = uiState.copy(weatherIndex = index)
    }

    /**
     * 更新标签
     *
     * @param label 标签
     */
    fun updateLabel(label: String) {
        uiState = uiState.copy(label = label)
    }

    /**
     * 更新标题可见性
     *
     * @param visible 是否可见
     */
    fun updateTitleVisibility(visible: Boolean) {
        uiState = uiState.copy(isTitleVisible = visible)
    }

    /**
     * 切换格式化栏可见性
     */
    fun toggleFormattingBar() {
        uiState = uiState.copy(isFormattingBarVisible = !uiState.isFormattingBarVisible)
    }

    /**
     * 显示颜色选择栏
     *
     * @param visible 是否显示
     */
    fun showColorSheet(visible: Boolean) {
        uiState = uiState.copy(isColorSheetVisible = visible)
    }

    /**
     * 显示心情选择栏
     *
     * @param visible 是否显示
     */
    fun showMoodSheet(visible: Boolean) {
        uiState = uiState.copy(isMoodSheetVisible = visible)
    }

    /**
     * 显示天气选择栏
     *
     * @param visible 是否显示
     */
    fun showWeatherSheet(visible: Boolean) {
        uiState = uiState.copy(isWeatherSheetVisible = visible)
    }

    /**
     * 显示标签
     *
     * @param visible 是否显示
     */
    fun showLabel(visible: Boolean){
        uiState = uiState.copy(isLabelVisible = visible)
    }

    /**
     * 更新密码以及是否锁定
     *
     * @param password 密码
     */
    fun updateSinglePassword(password: String) {
        uiState = uiState.copy(singlePassword = password, isLocked = password.isNotEmpty())
    }

    /**
     * 更新是否置顶
     *
     * @param pinned 是否置顶
     */
    fun updatePinnedStatus(pinned: Boolean) {
        uiState = uiState.copy(isPinned = pinned)
    }

    /**
     * 更新背景
     *
     * @param index 背景索引
     */
    fun updateBackgroundColor(index: Int) {
        uiState = uiState.copy(backgroundColorIndex = index)
    }

    /**
     * 更新是否可以保存
     *
     * @param canSave 是否可以保存
     */
    fun updateCanSave(canSave: Boolean) {
        uiState = uiState.copy(canSave = canSave)
    }

    /**
     * 更新标题字体大小
     *
     * @param size 字体大小
     */
    fun updateTitleFontSize(size: Float) {
        uiState = uiState.copy(titleFontSize = size)
    }

    /**
     * 更新内容字体大小
     *
     * @param size 字体大小
     */
    fun updateContentFontSize(size: Float) {
        uiState = uiState.copy(contentFontSize = size)
    }

    private fun checkCanSave() {
        // uiState = uiState.copy(canSave = uiState.title.isNotEmpty() || uiState.content.isNotEmpty())
    }
}
