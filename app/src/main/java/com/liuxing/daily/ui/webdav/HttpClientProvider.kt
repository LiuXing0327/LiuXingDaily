/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.ui.webdav

import okhttp3.OkHttpClient

/**
 * 单例 OkHttpClient
 */
val sharedOkHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder().build()
}