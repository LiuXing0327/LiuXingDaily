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

package com.liuxing.daily.data

sealed class BaseListItemData {
    data class Header(
        val subheading: String
    ) : BaseListItemData()

    data class Item(
        val key: Any,
        val iconResource: Int,
        val text: String,
        val checked: Boolean,
        val indexInSelection: Int = 1,
        val selectionCount: Int = 1,
        var subText: String = ""
    ) : BaseListItemData()

    data class ExpandableItem(
        val key: Any,
        val iconResource: Int,
        val text: String,
        var expanded: Boolean = false,
        val indexInSelection: Int = 1,
        val selectionCount: Int = 1,
        val iconContentDescription: String = "",
        val firstExpandedText: String,
        val secondExpandedText: String,
        val firstExpandedIconResource: Int,
        val secondExpandedIconResource: Int,
        val firstExpandedIconDescription: String = "",
        val secondExpandedDescription: String = ""
    ) : BaseListItemData()

    data class SwitchItem(
        val key: String,
        val iconResource: Int,
        val text: String,
        var subText: String = "",
        var checked: Boolean,
        val indexInSelection: Int = 1,
        val selectionCount: Int = 1
    ): BaseListItemData()

    data class SliderItem(
        val key: Any,
        val iconResource: Int,
        val text: String,
        val indexInSelection: Int = 1,
        val selectionCount: Int = 1,
        val valueFrom: Float = 0f,
        val valueTo: Float,
        var value: Float = 0f,
        val stepSize: Float = 1f,
        val labels: List<String> = emptyList()
    ) : BaseListItemData()
}