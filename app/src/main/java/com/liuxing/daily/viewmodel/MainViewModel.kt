package com.liuxing.daily.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Author：流星
 * DateTime：2025/5/24 16:58
 * Description：主界面的ViewModel
 */
class MainViewModel : ViewModel() {

    /**
     * 记录选择的年月日，用于指定日期记录日记
     */
    val selectedYearMonthDay = MutableLiveData<String>()

    /**
     * 设置年月日
     *
     * @param selectedYearMonthDay 选择的年月日
     */
    fun setYearMonthDay(selectedYearMonthDay: String) {
        this.selectedYearMonthDay.value = selectedYearMonthDay
    }
}