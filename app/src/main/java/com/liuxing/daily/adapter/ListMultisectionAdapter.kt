/*
 * Copyright (c) 2026 流星
 */

package com.liuxing.daily.adapter

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.listitem.ListItemCardView
import com.google.android.material.listitem.ListItemLayout
import com.google.android.material.listitem.ListItemViewHolder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.R
import com.liuxing.daily.data.BaseListItemData
import com.liuxing.daily.extension.setVisibility
import com.liuxing.daily.ui.datamanagement.DataManagementActivity
import com.liuxing.daily.util.ConstUtil.VIEW_TYPE_DAILY
import com.liuxing.daily.util.ConstUtil.VIEW_TYPE_HEADER
import kotlin.math.max

private const val VIEW_TYPE_EXPANDABLE_ITEM = 2
private const val VIEW_TYPE_SWITCH_ITEM = 3

private const val EXPANDED_SECTION_COUNT = 3

class ListMultisectionAdapter(
    private val onItemClick: ((BaseListItemData.Item) -> Unit)? = null,
    private val onExpandableFirstItemClick: ((BaseListItemData.ExpandableItem) -> Unit)? = null,
    private val onExpandableSecondItemOnClick: ((BaseListItemData.ExpandableItem) -> Unit)? = null,
    private val onCheckedChange: ((BaseListItemData.SwitchItem, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var list: List<BaseListItemData> = emptyList()

    fun setData(list: List<BaseListItemData>) {
        this.list = list
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_subheader, parent, false)
                SubheadingViewHolder(view)
            }

            VIEW_TYPE_DAILY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_multisection_list, parent, false)
                ViewHolder(view)
            }

            VIEW_TYPE_SWITCH_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_switch_list, parent, false)
                SwitchViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_expandable_list, parent, false)
                ExpandableViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int
    ) {
        when (val data = list[position]) {
            is BaseListItemData.Header -> (holder as SubheadingViewHolder).bind(data)
            is BaseListItemData.Item -> (holder as ViewHolder).bind(data, onItemClick)
            is BaseListItemData.ExpandableItem -> (holder as ExpandableViewHolder).bind(
                data,
                onExpandableFirstItemClick,
                onExpandableSecondItemOnClick
            )

            is BaseListItemData.SwitchItem -> (holder as SwitchViewHolder).bind(
                data,
                onCheckedChange
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is BaseListItemData.Header -> VIEW_TYPE_HEADER
            is BaseListItemData.Item -> VIEW_TYPE_DAILY
            is BaseListItemData.ExpandableItem -> VIEW_TYPE_EXPANDABLE_ITEM
            is BaseListItemData.SwitchItem -> VIEW_TYPE_SWITCH_ITEM
        }
    }

    override fun getItemCount(): Int = list.size


    class ViewHolder(itemView: View) : ListItemViewHolder(itemView) {

        val textView: MaterialTextView = itemView.findViewById(R.id.list_item_over_text)
        val textView2: MaterialTextView = itemView.findViewById(R.id.list_item_text)
        val startIcon: ShapeableImageView = itemView.findViewById(R.id.list_item_image)
        val cardView: MaterialCardView = itemView.findViewById(R.id.list_item_card_view)

        fun bind(data: BaseListItemData.Item, onItemClick: ((BaseListItemData.Item) -> Unit)?) {
            super.bind(data.indexInSelection, data.selectionCount)

            textView.text = data.text
            textView2.text = data.subText
            if (data.subText.isNotEmpty()) {
                textView2.setVisibility(true)
            } else {
                textView2.setVisibility(false)
            }

            startIcon.setImageResource(data.iconResource)

            if (data.key == DataManagementActivity.ItemKey.LOCAL_BACKUP_PATH) {
                cardView.setCardBackgroundColor(
                    MaterialColors.getColor(
                        itemView,
                        com.google.android.material.R.attr.colorPrimaryContainer
                    )
                )
            }
            cardView.setOnClickListener {
                onItemClick?.invoke(data)
            }
        }
    }

    class SubheadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: MaterialTextView = itemView.findViewById(R.id.list_subheading)

        fun bind(data: BaseListItemData.Header) {
            textView.text = data.subheading
        }
    }

    class ExpandableViewHolder(itemView: View) : ListItemViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.list_item_start_icon)
        private val textView: MaterialTextView = itemView.findViewById(R.id.list_item_text)
        private val cardView: ListItemCardView = itemView.findViewById(R.id.list_item_card_view)
        private val expandButton: ShapeableImageView =
            itemView.findViewById(R.id.list_item_expand_button)
        private val expandableContent: View = itemView.findViewById(R.id.lists_expandable_content)
        private val firstExpandedItem: ListItemLayout =
            itemView.findViewById(R.id.list_first_expanded_item)
        private val secondExpandedItem: ListItemLayout =
            itemView.findViewById(R.id.list_second_expanded_item)
        private val firstExpandedCard: ListItemCardView =
            itemView.findViewById(R.id.list_first_expanded_card)
        private val secondExpandedCard: ListItemCardView =
            itemView.findViewById(R.id.list_second_expanded_card)
        private val firstExpandedText: MaterialTextView =
            itemView.findViewById(R.id.list_first_expanded_text)
        private val secondExpandedText: MaterialTextView =
            itemView.findViewById(R.id.list_second_expanded_text)
        private val expandButtonBackgroundColor = MaterialColors.getColor(
            itemView, com.google.android.material.R.attr.colorSurfaceContainer
        )
        private val spring = SpringForce().setStiffness(800f).setDampingRatio(0.8f)
        private val arrowUpDrawable = R.drawable.baseline_keyboard_arrow_up_24
        private val arrowDownDrawable = R.drawable.baseline_keyboard_arrow_down_24
        private val firstExpandedImageView: ImageView =
            itemView.findViewById(R.id.list_first_expanded_start_icon)
        private val secondExpandedImageView: ImageView =
            itemView.findViewById(R.id.list_second_expanded_start_icon)

        fun bind(
            data: BaseListItemData.ExpandableItem,
            onExpandableFirstItemClick: ((BaseListItemData.ExpandableItem) -> Unit)?,
            onExpandableSecondItemOnClick: ((BaseListItemData.ExpandableItem) -> Unit)?
        ) {
            super.bind(
                if (data.expanded) 0 else data.indexInSelection,
                if (data.expanded) EXPANDED_SECTION_COUNT else data.selectionCount
            )

            imageView.setImageResource(data.iconResource)
            firstExpandedImageView.setImageResource(data.firstExpandedIconResource)
            secondExpandedImageView.setImageResource(data.secondExpandedIconResource)

            textView.text = data.text
            firstExpandedText.text = data.firstExpandedText
            secondExpandedText.text = data.secondExpandedText

            expandableContent.setVisibility(data.expanded)

            val expandButtonIcon = if (data.expanded) arrowUpDrawable else arrowDownDrawable
            expandButton.setImageResource(expandButtonIcon)
            val expandButtonColor = if (data.expanded) expandButtonBackgroundColor else 0
            expandButton.setBackgroundColor(expandButtonColor)

            firstExpandedItem.updateAppearance(1, EXPANDED_SECTION_COUNT)
            secondExpandedItem.updateAppearance(2, EXPANDED_SECTION_COUNT)

            cardView.setOnClickListener {
                toggleExpandedState(data)
            }
            firstExpandedCard.setOnClickListener {
                onExpandableFirstItemClick?.invoke(data)
            }
            secondExpandedCard.setOnClickListener {
                onExpandableSecondItemOnClick?.invoke(data)
            }

            setUpAccessibility(data)
        }

        private fun setUpAccessibility(data: BaseListItemData.ExpandableItem) {
            cardView.accessibilityDelegate = object : View.AccessibilityDelegate() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View,
                    info: AccessibilityNodeInfo
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.className = ListItemCardView::class.java.name
                    if (data.expanded) {
                        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_COLLAPSE)
                    } else {
                        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND)
                    }
                }

                override fun performAccessibilityAction(
                    host: View,
                    action: Int,
                    args: Bundle?
                ): Boolean {
                    if (action == AccessibilityNodeInfoCompat.ACTION_COLLAPSE) {
                        toggleExpandedState(data)
                        return true
                    } else if (action == AccessibilityNodeInfoCompat.ACTION_EXPAND) {
                        toggleExpandedState(data)
                        return true
                    }
                    return super.performAccessibilityAction(host, action, args)
                }
            }
        }

        private fun toggleExpandedState(data: BaseListItemData.ExpandableItem) {
            data.expanded = !data.expanded

            val expandButtonIcon = if (data.expanded) arrowUpDrawable else arrowDownDrawable
            expandButton.setImageResource(expandButtonIcon)

            val expandButtonColor = if (data.expanded) expandButtonBackgroundColor else 0
            expandButton.setBackgroundColor(expandButtonColor)

            bind(
                if (data.expanded) 0 else data.indexInSelection,
                if (data.expanded) EXPANDED_SECTION_COUNT else data.selectionCount
            )

            if (data.expanded) runExpandAnimation() else runCollapseAnimation()
        }

        private fun runExpandAnimation() {
            expandableContent.visibility = View.VISIBLE
            val measuredHeight = cardView.measuredHeight

            firstExpandedItem.translationY = -measuredHeight.toFloat()
            secondExpandedItem.translationY = -measuredHeight * 2.toFloat()

            SpringAnimation(firstExpandedItem, SpringAnimation.TRANSLATION_Y)
                .setSpring(spring)
                .animateToFinalPosition(0f)

            val secondExpandedCardAnimation =
                SpringAnimation(secondExpandedItem, SpringAnimation.TRANSLATION_Y).setSpring(spring)
            secondExpandedCardAnimation.addUpdateListener { _, _, _ ->
                val layoutParams = expandableContent.layoutParams
                layoutParams.height = max(0, (secondExpandedItem.y + measuredHeight).toInt())
                expandableContent.layoutParams = layoutParams
            }
            secondExpandedCardAnimation.animateToFinalPosition(0f)
        }

        private fun runCollapseAnimation() {
            val measuredHeight = cardView.measuredHeight
            SpringAnimation(firstExpandedItem, SpringAnimation.TRANSLATION_Y)
                .setSpring(spring)
                .animateToFinalPosition((-measuredHeight).toFloat())

            val secondExpandableCardAnimation =
                SpringAnimation(secondExpandedItem, SpringAnimation.TRANSLATION_Y).setSpring(spring)
            secondExpandableCardAnimation.addUpdateListener { _, _, _ ->
                val layoutParams = expandableContent.layoutParams
                layoutParams.height = max(0, (secondExpandedItem.y + measuredHeight).toInt())
                expandableContent.layoutParams = layoutParams
            }
            secondExpandableCardAnimation.addEndListener { _, _, _, _ ->
                expandableContent.setVisibility(false)
            }
            secondExpandableCardAnimation.animateToFinalPosition((-measuredHeight * 2).toFloat())
        }
    }

    class SwitchViewHolder(itemView: View) : ListItemViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.list_item_start_icon)
        private val textView: MaterialTextView = itemView.findViewById(R.id.list_item_text)
        private val textView2: MaterialTextView =
            itemView.findViewById(R.id.list_item_supporting_Text)
        private val cardView: ListItemCardView = itemView.findViewById(R.id.list_item_card_view)
        private val switch: MaterialSwitch = itemView.findViewById(R.id.list_item_switch)

        fun bind(
            data: BaseListItemData.SwitchItem,
            onCheckedChange: ((BaseListItemData.SwitchItem, Boolean) -> Unit)?
        ) {
            super.bind(data.indexInSelection, data.selectionCount)

            imageView.setImageResource(data.iconResource)
            textView.text = data.text
            textView2.text = data.subText
            switch.isChecked = data.checked

            textView2.setVisibility(data.subText.isNotEmpty())

            cardView.setOnClickListener {
                data.checked = !data.checked
                val newChecked = data.checked
                switch.isChecked = newChecked

                onCheckedChange?.invoke(data, newChecked)
            }
        }
    }

    class MarginItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

        var itemMargin = 0

        init {
            itemMargin = context.resources.getDimensionPixelSize(R.dimen.dp_4)
        }

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {

            val position = parent.getChildAdapterPosition(view)

            if (position != state.itemCount - 1) outRect.bottom = itemMargin
        }
    }
}