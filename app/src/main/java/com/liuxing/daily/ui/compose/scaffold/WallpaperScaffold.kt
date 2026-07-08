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

package com.liuxing.daily.ui.compose.scaffold

import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

/**
 * 壁纸脚手架
 *
 * 支持壁纸平滑切换、透明度动画以及沉浸式状态栏适配
 */
@Composable
fun WallpaperScaffold(
    wallpaperBitmap: Bitmap?,
    wallpaperAlpha: Float,
    topBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues, Boolean) -> Unit
) {
    val hasWallpaper = wallpaperBitmap != null

    // 透明度变化平滑过度
    val animatedAlpha by animateFloatAsState(
        targetValue = wallpaperAlpha,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "wallpaperAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = wallpaperBitmap, label = "wallpaperCrossfade") { bitmap ->
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(animatedAlpha)
                )
            } else {
                // 无壁纸时的纯色背景占位
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .alpha(0.05f)
                )
            }
        }

        Scaffold(
            topBar = topBar,
            snackbarHost = snackbarHost,
            containerColor = wallpaperBitmap.getScaffoldBackgroundColor(),
            contentColor = MaterialTheme.colorScheme.onSurface
        ) { paddingValues ->
            content(paddingValues, hasWallpaper)
        }
    }
}

/**
 * 根据是否有壁纸返回脚手架背景颜色
 */
@Composable
fun Bitmap?.getScaffoldBackgroundColor(): Color =
    if (this != null) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerHighest

/**
 * 获取适配壁纸的 TopAppBar 颜色配置
 */
@Composable
fun Bitmap?.getTopAppBarColors(): TopAppBarColors {
    val isWallpaperExist = this != null
    val defaultContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest

    return TopAppBarDefaults.topAppBarColors(
        containerColor = if (isWallpaperExist) Color.Transparent else defaultContainerColor,
        scrolledContainerColor = if (isWallpaperExist) Color.Transparent else defaultContainerColor,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * 获取适配壁纸的卡片背景色
 */
@Composable
fun Bitmap?.getCardBackgroundColor(alpha: Float = 0.7f): Color {
    return if (this != null) {
        MaterialTheme.colorScheme.surface.copy(alpha = alpha)
    } else {
        MaterialTheme.colorScheme.surface
    }
}
