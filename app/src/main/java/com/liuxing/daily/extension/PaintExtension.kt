/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.extension

import android.graphics.Paint

/**
 * 计算文本的中线位置
 *
 * @param baseLine
 */
fun Paint.centerYFromBaseLine(baseLine: Int): Float = baseLine + (fontMetrics.ascent + fontMetrics.descent) / 2