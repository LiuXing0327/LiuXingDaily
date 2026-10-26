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

package com.liuxing.daily.ui.add

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatColorReset
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.StrikethroughS
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.liuxing.daily.R
import com.liuxing.daily.ui.compose.button.ExpressiveBackButton
import com.liuxing.daily.ui.compose.button.ExpressiveButton
import com.liuxing.daily.ui.compose.button.ExpressiveIconButton
import com.liuxing.daily.ui.compose.button.ExpressiveIconToggleButton
import com.liuxing.daily.ui.compose.theme.CardStyle
import com.liuxing.daily.ui.compose.theme.getCardStyle
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.LogUtil
import com.liuxing.daily.view.DailyTextInputEdit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDailyScreen(
    uiState: AddDailyUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onTitleChange: (String) -> Unit,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onListClick: () -> Unit,
    onDateTimeClick: () -> Unit,
    onMoodClick: () -> Unit,
    onWeatherClick: () -> Unit,
    onLabelClick: () -> Unit,
    onMoreFormattingClick: () -> Unit,
    onLockClick: () -> Unit,
    onDrawingClick: () -> Unit,
    onColorClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onStrikethroughClick: () -> Unit,
    onTextColorClick: () -> Unit,
    onHighlightColorClick: () -> Unit,
    onTextSizeClick: () -> Unit,
    onHeadingClick: () -> Unit,
    onOrderedListClick: () -> Unit,
    onTodoListClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onAlignLeftClick: () -> Unit,
    onAlignCenterClick: () -> Unit,
    onRuleClick: () -> Unit,
    onLinkClick: () -> Unit,
    onAddImageClick: () -> Unit,
    onAddVideoClick: () -> Unit,
    onAddAudioClick: () -> Unit,
    onTitleVisibilityClick: () -> Unit,
    onColorDismiss: () -> Unit,
    onColorSelect: (Int) -> Unit,
    onMoodDismiss: () -> Unit,
    onMoodSelect: (Int) -> Unit,
    onWeatherDismiss: () -> Unit,
    onWeatherSelect: (Int) -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    editorFactory: (DailyTextInputEdit) -> Unit,
) {
    val cardStyle = getCardStyle(uiState.backgroundColorIndex, isFull = true)
    val backgroundColor = when (cardStyle) {
        is CardStyle.SolidColor -> {
            if (uiState.backgroundColorIndex == 0) {
                MaterialTheme.colorScheme.background
            } else {
                cardStyle.composeColor
            }
        }

        is CardStyle.Illustration -> Color.Transparent

        is CardStyle.Glass -> MaterialTheme.colorScheme.surface.copy(alpha = cardStyle.alpha)
    }

    BackHandler(enabled = uiState.isFormattingBarVisible) {
        onMoreFormattingClick()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor,
        contentColor = contentColorFor(backgroundColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if(cardStyle is CardStyle.Illustration){
                Image(
                    painter = painterResource(id = cardStyle.drawableRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Scaffold(
                containerColor = Color.Transparent,
                contentColor = LocalContentColor.current,
                topBar = {
                    Column(modifier = Modifier.background(backgroundColor)) {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // 背景颜色
                                    ExpressiveIconToggleButton(
                                        checked = uiState.isColorSheetVisible,
                                        onCheckedChange = { onColorClick() }) {
                                        Icon(
                                            Icons.Outlined.Palette,
                                            contentDescription = stringResource(R.string.change_color)
                                        )
                                    }

                                    // 锁定按钮
                                    val lockIcon =
                                        if (uiState.isLocked) Icons.Filled.Lock else Icons.Outlined.Lock

                                    ExpressiveIconToggleButton(
                                        checked = uiState.isLocked,
                                        onCheckedChange = { onLockClick() }) {
                                        Icon(
                                            lockIcon,
                                            contentDescription = stringResource(R.string.locked),
                                        )
                                    }

                                    // 更多格式化
                                    ExpressiveIconToggleButton(
                                        checked = uiState.isFormattingBarVisible,
                                        onCheckedChange = { onMoreFormattingClick() }) {
                                        Icon(
                                            Icons.Outlined.AutoAwesome,
                                            contentDescription = stringResource(R.string.heading_level)
                                        )
                                    }
                                }
                            }, navigationIcon = {
                                ExpressiveBackButton(onBack = {
                                    if (uiState.isFormattingBarVisible) {
                                        onMoreFormattingClick()
                                    } else {
                                        onBack()
                                    }
                                })
                            }, actions = {
                                // 删除按钮（仅编辑模式可见）
                                if (onDeleteClick != null) {
                                    ExpressiveIconButton(
                                        onClick = onDeleteClick,
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        )
                                    ) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = stringResource(R.string.delete)
                                        )
                                    }
                                }

                                // 保存按钮
                                ExpressiveButton(
                                    onClick = onSave,
                                    enabled = uiState.canSave,
                                    contentPadding = PaddingValues(
                                        horizontal = 16.dp, vertical = 8.dp
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Done,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.save))
                                }
                            }, colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )

                        AnimatedVisibility(
                            visible = uiState.isTitleVisible,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            TextField(
                                value = uiState.title,
                                onValueChange = onTitleChange,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        stringResource(R.string.title),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontSize = uiState.titleFontSize.sp
                                        )
                                    )
                                },
                                textStyle = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = uiState.titleFontSize.sp
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = LocalContentColor.current,
                                    unfocusedTextColor = LocalContentColor.current
                                )
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.dateTimeText,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable {
                                    onDateTimeClick()
                                })

                            VerticalDivider(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .height(16.dp)
                            )

                            Text(
                                text = "${uiState.wordCount}${stringResource(R.string.word)}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            if (uiState.moodIndex >= 0 && (uiState.moodIndex < ConstUtil.moodList.size - 1)) {
                                IconButton(onClick = onMoodClick, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        painter = painterResource(ConstUtil.moodList[uiState.moodIndex]),
                                        contentDescription = stringResource(ConstUtil.moodLabelList[uiState.moodIndex]),
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Unspecified
                                    )
                                }
                            } else {
                                IconButton(onClick = onMoodClick, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        Icons.Outlined.Mood,
                                        contentDescription = stringResource(R.string.mood),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (uiState.weatherIndex >= 0 && uiState.weatherIndex < ConstUtil.weatherList.size - 1) {
                                IconButton(
                                    onClick = onWeatherClick, modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(ConstUtil.weatherList[uiState.weatherIndex]),
                                        contentDescription = stringResource(ConstUtil.weatherLabelList[uiState.weatherIndex]),
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Unspecified
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = onWeatherClick, modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.WbSunny,
                                        contentDescription = stringResource(R.string.weather),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            val labelIcon =
                                if (uiState.isLabelVisible) Icons.AutoMirrored.Default.Label else Icons.AutoMirrored.Outlined.Label
                            IconButton(onClick = onLabelClick, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    labelIcon,
                                    contentDescription = stringResource(R.string.label),
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = uiState.isLabelVisible,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = uiState.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 4.dp),
                                    maxLines = 1
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = LocalContentColor.current.copy(alpha = 0.12f)
                        )
                    }
                }) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .clipToBounds()
                        .consumeWindowInsets(padding)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                        )
                        .imePadding()
                ) {
                    AndroidView(
                        factory = { context ->
                            android.widget.ScrollView(context).apply {
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                isFillViewport = true
                                isVerticalScrollBarEnabled = false

                                val container = android.widget.LinearLayout(context).apply {
                                    layoutParams = android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    orientation = android.widget.LinearLayout.VERTICAL
                                }

                                val editor = DailyTextInputEdit(context).apply {
                                    layoutParams = android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    gravity = android.view.Gravity.TOP or android.view.Gravity.START
                                    hint = context.getString(R.string.content)
                                    background = null

                                    // 确保输入时能够及时滚动
                                    addTextChangedListener(object : android.text.TextWatcher {
                                        override fun beforeTextChanged(
                                            s: CharSequence?, start: Int, count: Int, after: Int
                                        ) {
                                        }

                                        override fun onTextChanged(
                                            s: CharSequence?, start: Int, before: Int, count: Int
                                        ) {
                                        }

                                        override fun afterTextChanged(s: android.text.Editable?) {
                                            post {
                                                val layout = layout
                                                if (layout != null) {
                                                    val line =
                                                        layout.getLineForOffset(selectionStart)
                                                    val lineBottom = layout.getLineBottom(line)
                                                    val rect = android.graphics.Rect(
                                                        0,
                                                        lineBottom,
                                                        width,
                                                        lineBottom + lineHeight
                                                    )
                                                    requestRectangleOnScreen(rect, false)
                                                }
                                            }
                                        }
                                    })
                                }

                                container.addView(editor)
                                addView(container)
                                editorFactory(editor)
                            }
                        }, update = { scrollView ->
                            val container = scrollView.getChildAt(0) as? android.widget.LinearLayout
                            val editor = container?.getChildAt(0) as? DailyTextInputEdit
                            if (editor?.textSize != uiState.contentFontSize) {
                                editor?.textSize = uiState.contentFontSize
                            }
                        }, modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            // 悬浮格式化栏
            if (uiState.isFormattingBarVisible) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                        toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    content = {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 绘画按钮
                            ExpressiveIconButton(onClick = onDrawingClick) {
                                Icon(
                                    Icons.Outlined.Brush,
                                    contentDescription = stringResource(R.string.drawing)
                                )
                            }
                            ExpressiveIconButton(onClick = onAddImageClick) {
                                Icon(
                                    Icons.Outlined.Image,
                                    contentDescription = stringResource(R.string.image)
                                )
                            }
                            ExpressiveIconButton(onClick = onAddVideoClick) {
                                Icon(
                                    Icons.Outlined.VideoFile,
                                    contentDescription = stringResource(R.string.video)
                                )
                            }
                            ExpressiveIconButton(onClick = onAddAudioClick) {
                                Icon(
                                    Icons.Outlined.AudioFile,
                                    contentDescription = stringResource(R.string.audio)
                                )
                            }

                            ExpressiveIconToggleButton(
                                checked = uiState.isTitleVisible,
                                onCheckedChange = { onTitleVisibilityClick() }) {
                                Icon(
                                    Icons.Outlined.Subtitles,
                                    contentDescription = stringResource(R.string.title)
                                )
                            }

                            VerticalDivider(
                                modifier = Modifier
                                    .height(24.dp)
                                    .padding(horizontal = 4.dp)
                            )

                            ExpressiveIconToggleButton(
                                checked = uiState.isBold, onCheckedChange = { onBoldClick() }) {
                                Icon(
                                    Icons.Outlined.FormatBold,
                                    contentDescription = stringResource(R.string.format_text_bold)
                                )
                            }
                            ExpressiveIconToggleButton(
                                checked = uiState.isItalic, onCheckedChange = { onItalicClick() }) {
                                Icon(
                                    Icons.Outlined.FormatItalic,
                                    contentDescription = stringResource(R.string.format_text_italic)
                                )
                            }
                            ExpressiveIconToggleButton(
                                checked = uiState.isUnderline,
                                onCheckedChange = { onUnderlineClick() }) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_format_underlined_24),
                                    contentDescription = stringResource(R.string.format_text_underline)
                                )
                            }
                            ExpressiveIconToggleButton(
                                checked = uiState.isStrikethrough,
                                onCheckedChange = { onStrikethroughClick() }) {
                                Icon(
                                    Icons.Outlined.StrikethroughS,
                                    contentDescription = stringResource(R.string.format_text_strikethrough)
                                )
                            }
                            ExpressiveIconButton(onClick = onTextColorClick) {
                                Icon(
                                    painterResource(R.drawable.round_format_color_text_24),
                                    contentDescription = stringResource(R.string.format_text_color)
                                )
                            }
                            ExpressiveIconButton(onClick = onHighlightColorClick) {
                                Icon(
                                    painterResource(R.drawable.outline_format_color_fill_24),
                                    contentDescription = stringResource(R.string.format_text_background_color)
                                )
                            }
                            ExpressiveIconButton(onClick = onTextSizeClick) {
                                Icon(
                                    painterResource(R.drawable.outline_format_size_24),
                                    contentDescription = stringResource(R.string.text_font_size)
                                )
                            }
                            ExpressiveIconButton(onClick = onHeadingClick) {
                                Icon(
                                    painterResource(R.drawable.baseline_title_24),
                                    contentDescription = stringResource(R.string.heading_level)
                                )
                            }
                            ExpressiveIconButton(onClick = onListClick) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.FormatListBulleted,
                                    contentDescription = stringResource(R.string.bullet_list)
                                )
                            }
                            ExpressiveIconButton(onClick = onOrderedListClick) {
                                Icon(
                                    Icons.Outlined.FormatListNumbered,
                                    contentDescription = stringResource(R.string.ordered_list)
                                )
                            }
                            ExpressiveIconButton(onClick = onTodoListClick) {
                                Icon(
                                    Icons.Outlined.Checklist,
                                    contentDescription = stringResource(R.string.todo_list)
                                )
                            }
                            ExpressiveIconButton(onClick = onQuoteClick) {
                                Icon(
                                    painterResource(R.drawable.outline_format_quote_24),
                                    contentDescription = stringResource(R.string.quote_block)
                                )
                            }
                            ExpressiveIconButton(onClick = onAlignLeftClick) {
                                Icon(
                                    painterResource(R.drawable.baseline_format_align_left_24),
                                    contentDescription = stringResource(R.string.left_aligned)
                                )
                            }
                            ExpressiveIconButton(onClick = onAlignCenterClick) {
                                Icon(
                                    painterResource(R.drawable.outline_format_align_center_24),
                                    contentDescription = stringResource(R.string.center_aligned)
                                )
                            }
                            ExpressiveIconButton(onClick = onRuleClick) {
                                Icon(
                                    painterResource(R.drawable.outline_horizontal_rule_24),
                                    contentDescription = stringResource(R.string.insert_horizontal_rule)
                                )
                            }
                            ExpressiveIconButton(onClick = onLinkClick) {
                                Icon(
                                    painterResource(R.drawable.outline_insert_link_24),
                                    contentDescription = stringResource(R.string.insert_hyperlink)
                                )
                            }
                        }
                    })
            }
        }

        // 颜色选择 BottomSheet
        if (uiState.isColorSheetVisible) {
            val sheetState = rememberBottomSheetState(
                initialValue = SheetValue.Expanded,
                enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
            )

            ModalBottomSheet(
                onDismissRequest = onColorDismiss,
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                ColorSelectionContent(
                    selectedPosition = uiState.backgroundColorIndex, onColorSelect = {
                        LogUtil.d("ColorSelectionContent", "onColorSelect: $it")
                        onColorSelect(it)
                    })
            }
        }

        // 心情选择 BottomSheet
        if (uiState.isMoodSheetVisible) {
            val sheetState = rememberBottomSheetState(
                initialValue = SheetValue.Expanded,
                enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
            )
            val scope = rememberCoroutineScope()

            ModalBottomSheet(
                onDismissRequest = onMoodDismiss,
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                IconGridSelectionContent(
                    title = stringResource(R.string.mood),
                    iconList = ConstUtil.moodList,
                    labelList = ConstUtil.moodLabelList,
                    selectedPosition = uiState.moodIndex,
                    onSelect = {
                        onMoodSelect(it)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) onMoodDismiss()
                        }
                    })
            }
        }

        // 天气选择 BottomSheet
        if (uiState.isWeatherSheetVisible) {
            val sheetState = rememberBottomSheetState(
                initialValue = SheetValue.Expanded,
                enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
            )
            val scope = rememberCoroutineScope()

            ModalBottomSheet(
                onDismissRequest = onWeatherDismiss,
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                IconGridSelectionContent(
                    title = stringResource(R.string.weather),
                    iconList = ConstUtil.weatherList,
                    labelList = ConstUtil.weatherLabelList,
                    selectedPosition = uiState.weatherIndex,
                    onSelect = {
                        onWeatherSelect(it)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) onWeatherDismiss()
                        }
                    })
            }
        }
    }
}

/**
 * 通用的图标网格选择内容
 */
@Composable
private fun IconGridSelectionContent(
    title: String,
    iconList: List<Int>,
    labelList: List<Int>,
    selectedPosition: Int,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            itemsIndexed(iconList) { index, iconRes ->
                val isSelected = index == selectedPosition

                val animatedRadius by animateDpAsState(
                    targetValue = if (isSelected) 12.dp else 24.dp,
                    animationSpec = spring(dampingRatio = 0.6f),
                    label = "iconRadius"
                )

                Surface(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(animatedRadius))
                        .clickable { onSelect(index) },
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    ),
                    shape = RoundedCornerShape(animatedRadius),
                    border = if (isSelected) BorderStroke(
                        2.dp, MaterialTheme.colorScheme.primary
                    ) else null
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (index == iconList.size - 1) {
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                Color.Unspecified
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(labelList[index]),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 颜色选择内容
 */
@Composable
private fun ColorSelectionContent(
    selectedPosition: Int,
    onColorSelect: (Int) -> Unit
) {
    // 纯色索引列表（0 至 size）+ 插图索引（100 至 100 + size）
    val colorIndices = remember {
        ConstUtil.backgroundColorList.indices.toList() +
                ConstUtil.illustrationThemeFullList.indices.map { it + 100 }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.change_color),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(colorIndices) { index ->
                val isSelected = index == selectedPosition
                val itemStyle = getCardStyle(index)

                val animatedRadius by animateDpAsState(
                    targetValue = if (isSelected) 12.dp else 24.dp,
                    animationSpec = spring(dampingRatio = 0.6f),
                    label = "colorRadius"
                )

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(animatedRadius))
                        .clickable { onColorSelect(index) }
                        .then(
                            if (isSelected) Modifier.border(
                                3.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(animatedRadius)
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (itemStyle) {
                        is CardStyle.SolidColor -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (index == 0) MaterialTheme.colorScheme.surface else itemStyle.composeColor)
                            )
                        }
                        is CardStyle.Illustration -> {
                            Image(
                                painter = painterResource(id = itemStyle.drawableRes),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        is CardStyle.Glass -> { }
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (index == 0) MaterialTheme.colorScheme.onSurface else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier
            .width(1.dp)
            .background(LocalContentColor.current.copy(alpha = 0.12f))
    )
}
