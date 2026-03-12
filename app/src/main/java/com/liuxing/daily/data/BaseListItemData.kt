/*
 * Copyright (c) 2026 流星
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
}