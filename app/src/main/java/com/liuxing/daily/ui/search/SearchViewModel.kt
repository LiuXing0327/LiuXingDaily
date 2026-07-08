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

package com.liuxing.daily.ui.search

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.repository.DailyRepository
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.TextUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds

import androidx.annotation.StringRes
import com.liuxing.daily.R

/**
 * 搜索过滤器枚举
 */
enum class SearchFilter(@field:StringRes val labelRes: Int) {
    ALL(R.string.all),
    TEXT(R.string.text),
    IMAGE(R.string.image),
    VIDEO(R.string.video),
    AUDIO(R.string.audio),
    STARRED(R.string.starred),
}

/**
 * 搜索结果分组
 */
data class SearchResultGroup(
    val header: String,
    val items: List<DailyEntity>
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DailyRepository(application)
    private val fileUtil = FileUtil()
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    
    private val _queryFlow = MutableStateFlow("")
    private val _filterFlow = MutableStateFlow(SearchFilter.ALL)
    
    var query by mutableStateOf("")
        private set
    var selectedFilter by mutableStateOf(SearchFilter.ALL)
        private set
    var textSize by mutableStateOf(16f)

    // 渲染结果
    private val _pinnedResults = mutableStateListOf<DailyEntity>()
    val pinnedResults: List<DailyEntity> get() = _pinnedResults

    private val _groupedResults = mutableStateListOf<SearchResultGroup>()
    val groupedResults: List<SearchResultGroup> get() = _groupedResults

    private val _imageMap = mutableStateMapOf<String, String>()
    val imageMap: Map<String, String> get() = _imageMap

    private val _recentSearches = mutableStateListOf<String>()
    val recentSearches: List<String> get() = _recentSearches

    init {
        refreshSettings()
        setupSearchLogic()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchLogic() {
        viewModelScope.launch {
            combine(
                _queryFlow.debounce(300.milliseconds),
                _filterFlow,
                repository.queryAllDaily().asFlow()
            ) { q: String, filter: SearchFilter, allDaily: List<DailyEntity> ->
                val filtered = allDaily.filter { daily ->
                    if (daily.isDeleted) return@filter false
                    
                    val matchesQuery = if (q.isBlank()) {
                        true 
                    } else {
                        val filteredTitle = TextUtil.replaceTag(daily.title ?: "")
                        val filteredContent = TextUtil.replaceTag(daily.content ?: "")
                        filteredTitle.contains(q, ignoreCase = true) || 
                                filteredContent.contains(q, ignoreCase = true)
                    }
                    if (!matchesQuery) return@filter false
                    
                    when (filter) {
                        SearchFilter.ALL, SearchFilter.TEXT -> true
                        SearchFilter.IMAGE -> hasMedia(daily, "img")
                        SearchFilter.VIDEO -> hasMedia(daily, "video")
                        SearchFilter.AUDIO -> hasMedia(daily, "audio")
                        SearchFilter.STARRED -> daily.isPinned
                    }
                }
                filtered
            }.collect { filteredList ->
                updateUIResults(filteredList)
            }
        }
    }

    private fun updateUIResults(results: List<DailyEntity>) {
        val currentSortIndex = sharedPreferences.getInt("daily_sort_by", 0)
        val headerBoolean = sharedPreferences.getBoolean("switch_preference_header_display", true)

        val pinned = results.filter { it.isPinned }.sortedByDescending { it.dateTime }
        val nonPinned = results.filterNot { it.isPinned }
        
        // 按时间戳进行排序
        val sortedNonPinned = when (currentSortIndex) {
            1 -> nonPinned.sortedBy { it.dateTime ?: 0L }
            else -> nonPinned.sortedByDescending { it.dateTime ?: 0L }
        }

        val groups = sortedNonPinned.groupBy {
            DateUtil.getDateString(0, Date(it.dateTime ?: 0L)).substring(0, 7)
        }.map { (yearMonth, items) ->
            val displayHeader = if (headerBoolean) {
                yearMonth
            } else {
                try {
                    // "2026年01" -> "1"
                    yearMonth.substringAfterLast("-").substringAfterLast("年").substring(0, 2).toInt().toString()
                } catch (_: Exception) {
                    yearMonth
                }
            }
            SearchResultGroup(displayHeader, items)
        }

        _pinnedResults.clear()
        _pinnedResults.addAll(pinned)
        
        _groupedResults.clear()
        _groupedResults.addAll(groups)
        
        viewModelScope.launch { loadImagesForResults(results) }
    }

    fun performSearch(newQuery: String) {
        query = newQuery
        _queryFlow.value = newQuery
    }

    fun updateFilter(filter: SearchFilter) {
        selectedFilter = filter
        _filterFlow.value = filter
    }

    fun refreshSettings() {
        textSize = sharedPreferences.getFloat(ConstUtil.TEXT_SIZE_KEY, 16f)
    }

    private suspend fun loadImagesForResults(results: List<DailyEntity>) = withContext(Dispatchers.IO) {
        val mapping = mutableMapOf<String, String>()
        results.forEach { daily ->
            val uuid = daily.dailyUUID ?: return@forEach
            val images = repository.queryDailyImageByUuidToList(uuid)
            val firstImage = images.firstOrNull()?.imagePath
            if (firstImage != null && fileUtil.checkFileExists(firstImage)) {
                mapping[uuid] = firstImage
            }
        }
        withContext(Dispatchers.Main) {
            _imageMap.clear()
            _imageMap.putAll(mapping)
        }
    }

    private fun hasMedia(daily: DailyEntity, tag: String): Boolean {
        return daily.content?.contains("<$tag", ignoreCase = true) == true
    }

    fun addRecentSearch(text: String) {
        if (text.isBlank()) return
        _recentSearches.remove(text)
        _recentSearches.add(0, text)
        while (_recentSearches.size > 8) _recentSearches.removeAt(_recentSearches.lastIndex)
    }
    
    fun clearRecentSearches() {
        _recentSearches.clear()
    }
}