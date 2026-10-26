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

/**
 * 添加日记界面的 UI 状态
 */
data class AddDailyUiState(
    val title: String = "",
    val content: String = "",
    val dateTimeText: String = "",
    val wordCount: Int = 0,
    val moodIndex: Int = -1,
    val weatherIndex: Int = -1,
    val label: String = "",
    val backgroundColorIndex: Int = 0,
    val isTitleVisible: Boolean = false,
    val isMetadataVisible: Boolean = true,
    val isFormattingBarVisible: Boolean = false,
    val isColorSheetVisible: Boolean = false,
    val isMoodSheetVisible: Boolean = false,
    val isWeatherSheetVisible: Boolean = false,
    val isLabelVisible: Boolean = false,
    val isLocked: Boolean = false,
    val isPinned: Boolean = false,
    val singlePassword: String = "",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val canSave: Boolean = false,
    val titleFontSize: Float = 20F,
    val contentFontSize: Float = 16F,
)
