package com.liuxing.daily.ui.updatelog

import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.liuxing.daily.R
import com.liuxing.daily.data.VersionLogData
import com.liuxing.daily.databinding.ActivityUpdateLogBinding
import com.liuxing.daily.ui.compose.theme.DailyTheme
import com.liuxing.daily.ui.compose.theme.DailyThemeManager
import com.liuxing.daily.ui.qrx.QRXActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader


class UpdateLogActivity : QRXActivity() {

    private var logItems by mutableStateOf<List<VersionLogData>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        loadUpdateLogs()

        setContent {
            initCompose()

            LaunchedEffect(Unit) {
                checkStatusBarColorForCompose(
                    topAppBarColor = androidx.compose.ui.graphics.Color.Transparent
                )
            }

            DailyTheme(
                themeType = DailyThemeManager.currentThemeType,
                themeMode = DailyThemeManager.themeMode,
                isAmoled = DailyThemeManager.isAmoled,
                dynamicColor = DailyThemeManager.isDynamicColor,
            ) {
                val wallpaperBitmap = remember { safeWallpaperBitmap }
                val wallpaperAlpha = remember { safeWallpaperAlpha }
                val cardAlpha = remember { safeCardAlpha }

                UpdateLogScreen(
                    wallpaperBitmap = wallpaperBitmap,
                    wallpaperAlpha = wallpaperAlpha,
                    cardAlpha = cardAlpha,
                    logItems = logItems,
                    onBack = ::finish
                )
            }
        }
    }

    private fun loadUpdateLogs() {
        lifecycleScope.launch {
            val logs = withContext(Dispatchers.IO) {
                parseLogFromAssets()
            }
            logItems = logs
        }
    }

    private fun parseLogFromAssets(): List<VersionLogData> {
        val items = mutableListOf<VersionLogData>()
        try {
            assets.open("UpdateLogText.txt").use { inputStream ->
                val bufferedReader = BufferedReader(inputStream.reader())
                var currentVersion: String? = null
                var currentDate: String? = null
                val currentVersionLog = mutableListOf<String>()

                bufferedReader.forEachLine { line ->
                    val trimmedLine = line.trim()
                    when {
                        trimmedLine.matches(Regex("V\\d+(\\.\\d+)*\\(\\d{4}-\\d{2}-\\d{2}\\)")) -> {
                            if (currentVersion != null) {
                                items.add(
                                    VersionLogData(
                                        currentVersion!!,
                                        currentDate ?: "",
                                        currentVersionLog.toList()
                                    )
                                )
                            }
                            currentVersion = trimmedLine.substringBefore("(")
                            currentDate = trimmedLine.substringAfter("(").substringBefore(")")
                            currentVersionLog.clear()
                        }

                        trimmedLine.startsWith("-") -> {
                            currentVersionLog.add(trimmedLine.removePrefix("-").trim())
                        }
                    }
                }
                if (currentVersion != null) {
                    items.add(
                        VersionLogData(
                            currentVersion!!,
                            currentDate ?: "",
                            currentVersionLog.toList()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return items.reversed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_update_log, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.item_new_to_old -> {
                logItems = logItems.sortedByDescending { it.version }
            }

            R.id.item_old_to_new -> {
                logItems = logItems.sortedBy { it.version }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}