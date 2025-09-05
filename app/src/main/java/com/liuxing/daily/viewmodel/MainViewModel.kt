/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * 主界面的ViewModel
 */
class MainViewModel : ViewModel() {

    /**
     * 记录选择的年月日，用于指定日期记录日记
     */
    private val _selectedYearMonthDay = MutableLiveData<String>()
    val selectedYearMonthDay = _selectedYearMonthDay
    private val _enableAppBarOffsetChange = MutableLiveData(true)
    val enableAppBarOffsetChange = _enableAppBarOffsetChange

    /**
     * 设置年月日
     *
     * @param selectedYearMonthDay 选择的年月日
     */
    fun setYearMonthDay(selectedYearMonthDay: String) {
        this._selectedYearMonthDay.value = selectedYearMonthDay
    }

    /**
     * 设置是否启用应用栏偏移更改
     *
     * @param enable true 启用；
     *               false 禁用
     */
    fun setEnableAppBarOffsetChange(enable: Boolean) {
        this._enableAppBarOffsetChange.value = enable
    }
}