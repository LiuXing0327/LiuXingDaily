package com.liuxing.daily.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import com.liuxing.daily.R

/**
 * Author：流星
 * DateTime：2025/1/18 18:09
 * Description：
 */
class AutoDeleteRecyclerBinDailyPreference(context: Context, attrs: AttributeSet) :
    Preference(context, attrs) {

    init {
        layoutResource = R.layout.preference_auto_delete_recycler_bin_daily
    }

    private var sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val autoDeleteDailyPreference =
            holder.findViewById(R.id.auto_delete_daily_preference) as LinearLayout
        val textView = holder.findViewById(R.id.preference_summary) as TextView
        val autoDeleteIndex = sharedPreferences.getInt("auto_delete_recycler_bin_daily", 7)
        textView.text = when (autoDeleteIndex) {
            3 -> {
                context.getString(R.string.days_3)
            }

            7 -> {
                context.getString(R.string.days_7)
            }

            14 -> {
                context.getString(R.string.days_14)
            }

            30 -> {
                context.getString(R.string.days_30)
            }

            else -> {
                context.getString(R.string.close)
            }
        }

        autoDeleteDailyPreference.setOnClickListener {
            val popupMenu = PopupMenu(context, it)
            popupMenu.menuInflater.inflate(
                R.menu.menu_auto_delete_recycler_bin_daily,
                popupMenu.menu
            )

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.item_3 -> {
                        putAutoDeleteIndex(3)
                        textView.text = context.getString(R.string.days_3)
                    }

                    R.id.item_7 -> {
                        putAutoDeleteIndex(7)
                        textView.text = context.getString(R.string.days_7)
                    }

                    R.id.item_14 -> {
                        putAutoDeleteIndex(14)
                        textView.text = context.getString(R.string.days_14)
                    }

                    R.id.item_30 -> {
                        putAutoDeleteIndex(30)
                        textView.text = context.getString(R.string.days_30)
                    }

                    else -> {
                        putAutoDeleteIndex(0)
                        textView.text = context.getString(R.string.close)
                    }
                }
                true
            }
            popupMenu.show()
        }

    }

    /**
     * 存入自动删除的索引
     *
     * @param index 索引
     */
    private fun putAutoDeleteIndex(index: Int) {
        sharedPreferences.edit {
            putInt("auto_delete_recycler_bin_daily", index)
        }
    }
}