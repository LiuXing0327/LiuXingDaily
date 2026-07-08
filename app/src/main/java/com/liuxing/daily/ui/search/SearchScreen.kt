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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liuxing.daily.R
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.ui.compose.list.DailyListCard
import com.liuxing.daily.ui.compose.list.Subheader
import com.liuxing.daily.util.HighlightUtil
import com.liuxing.daily.util.TextUtil

private val SearchFilter.icon: ImageVector
    get() = when (this) {
        SearchFilter.ALL -> Icons.Outlined.FilterAlt
        SearchFilter.TEXT -> Icons.AutoMirrored.Outlined.TextSnippet
        SearchFilter.IMAGE -> Icons.Outlined.Image
        SearchFilter.VIDEO -> Icons.Outlined.VideoLibrary
        SearchFilter.AUDIO -> Icons.Outlined.Mic
        SearchFilter.STARRED -> Icons.Outlined.StarOutline
    }

/**
 * 搜索界面组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onResultClick: (DailyEntity) -> Unit,
    onResultLongClick: (DailyEntity) -> Unit,
) {
    val query = viewModel.query
    val selectedFilter = viewModel.selectedFilter

    LaunchedEffect(selectedFilter) {
        if (query.isNotBlank()) viewModel.performSearch(query)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {

        SearchFilterBar(
            selectedFilter = selectedFilter, onFilterSelected = { viewModel.updateFilter(it) })

        SearchResultsList(
            query = query,
            viewModel = viewModel,
            onResultClick = onResultClick,
            onResultLongClick = onResultLongClick
        )
    }
}

@Composable
private fun SearchFilterBar(
    selectedFilter: SearchFilter, onFilterSelected: (SearchFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        SearchFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(stringResource(filter.labelRes)) },
                leadingIcon = {
                    Icon(
                        imageVector = if (selectedFilter == filter) Icons.Default.Check else filter.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == filter,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    selectedBorderColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultsList(
    query: String,
    viewModel: SearchViewModel,
    onResultClick: (DailyEntity) -> Unit,
    onResultLongClick: (DailyEntity) -> Unit
) {
    val pinnedResults = viewModel.pinnedResults
    val groupedResults = viewModel.groupedResults
    val imageMap = viewModel.imageMap
    val textSize = viewModel.textSize
    val recentSearches = viewModel.recentSearches

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 最近搜索历史
        if (query.isEmpty() && recentSearches.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("最近搜索")
                    TextButton(onClick = { viewModel.clearRecentSearches() }) {
                        Text("清空", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            item {
                @OptIn(ExperimentalLayoutApi::class) FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentSearches.forEach { search ->
                        SuggestionChip(text = search, onClick = { viewModel.performSearch(search) })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // 置顶部分
        if (pinnedResults.isNotEmpty()) {
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Subheader(stringResource(R.string.pinned))
                    }
                }
            }
            itemsIndexed(pinnedResults) { index, daily ->
                DailyResultItem(
                    daily = daily,
                    query = query,
                    imageMap = imageMap,
                    textSize = textSize,
                    index = index,
                    count = pinnedResults.size,
                    onResultClick = onResultClick,
                    onResultLongClick = onResultLongClick
                )
            }
        }

        // 分组部分
        if (groupedResults.isNotEmpty()) {
            groupedResults.forEach { group ->
                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            Subheader(group.header)
                        }
                    }
                }
                itemsIndexed(group.items) { index, daily ->
                    DailyResultItem(
                        daily = daily,
                        query = query,
                        imageMap = imageMap,
                        textSize = textSize,
                        index = index,
                        count = group.items.size,
                        onResultClick = onResultClick,
                        onResultLongClick = onResultLongClick
                    )
                }
            }
        }

        if (query.isNotEmpty() && pinnedResults.isEmpty() && groupedResults.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_matching_content_found), color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
private fun DailyResultItem(
    daily: DailyEntity,
    query: String,
    imageMap: Map<String, String>,
    textSize: Float,
    index: Int,
    count: Int,
    onResultClick: (DailyEntity) -> Unit,
    onResultLongClick: (DailyEntity) -> Unit
) {
    val rawContent = TextUtil.replaceTag(daily.content ?: "")
    val highlightedTitle = HighlightUtil.highlightToAnnotatedString(
        fullText = daily.title ?: "",
        keyword = query,
        highlightColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    )
    val highlightedContent = HighlightUtil.highlightToAnnotatedString(
        fullText = rawContent,
        keyword = query,
        highlightColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    )

    DailyListCard(
        daily = daily,
        imagePath = imageMap[daily.dailyUUID],
        title = if (daily.singlePassword.isNullOrEmpty()) highlightedTitle else "***",
        content = if (daily.singlePassword.isNullOrEmpty()) highlightedContent else "***",
        textSize = textSize,
        index = index,
        count = count,
        onClick = { onResultClick(daily) },
        onLongClick = { onResultLongClick(daily) })
}

@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.History,
                null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}
