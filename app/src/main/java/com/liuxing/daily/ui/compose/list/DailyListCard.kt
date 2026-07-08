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

package com.liuxing.daily.ui.compose.list

import android.widget.ImageView
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.ui.settings.DailySettingsConst
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.SharedPreferencesUtil
import java.io.File
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DailyListCard(
    daily: DailyEntity,
    imagePath: String? = null,
    title: Any,
    content: Any,
    textSize: Float = 16f,
    alpha: Float = 1f,
    index: Int = 0,
    count: Int = 1,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 弹性缩放
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "scale"
    )

    val topRadius by animateDpAsState(
        targetValue = if (isPressed || index == 0) 24.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "topCorner"
    )
    val bottomRadius by animateDpAsState(
        targetValue = if (isPressed || index == count - 1) 24.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bottomCorner"
    )

    val currentShape = RoundedCornerShape(
        topStart = topRadius,
        topEnd = topRadius,
        bottomStart = bottomRadius,
        bottomEnd = bottomRadius
    )

    // 获取背景颜色
    val backgroundColorIndex = daily.backgroundColorIndex ?: 0
    val containerColor = getDailyItemBackgroundColor(backgroundColorIndex)

    val isGlassMode = backgroundColorIndex == 0
    val finalAlpha = if (isGlassMode) 0.7f * alpha else alpha

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(currentShape)
            .combinedClickable(
                interactionSource = interactionSource,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = currentShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor.copy(alpha = finalAlpha)
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 图片预览
            val hasValidImage = !imagePath.isNullOrEmpty() && File(imagePath).exists()
            if (hasValidImage && daily.singlePassword.isNullOrEmpty()) {
                AndroidView(
                    factory = { ctx ->
                        ImageView(ctx).apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    update = { view ->
                        Glide.with(view.context).load(imagePath).into(view)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 标题
            Box(modifier = Modifier.fillMaxWidth()) {
                when (title) {
                    is AnnotatedString -> Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = (textSize + 4).sp),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    is String -> if (title.isNotBlank()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = (textSize + 4).sp),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (title.toString().isNotBlank()) Spacer(modifier = Modifier.height(4.dp))

            // 正文
            Box(modifier = Modifier.fillMaxWidth()) {
                when (content) {
                    is AnnotatedString -> Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = (textSize * 1.4f).sp
                    )

                    is String -> if (content.isNotBlank()) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = (textSize * 1.4f).sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 心情 & 天气 & 标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (daily.moodIndex != null && daily.moodIndex > 0) {
                        Icon(
                            painter = painterResource(id = ConstUtil.moodList[daily.moodIndex - 1]),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (daily.weatherIndex != null && daily.weatherIndex > 0) {
                        Icon(
                            painter = painterResource(id = ConstUtil.weatherList[daily.weatherIndex - 1]),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (!daily.dailyLabel.isNullOrEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = daily.dailyLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // 时间
                val dateString = DateUtil.getDateString(0, Date(daily.dateTime ?: 0))
                val showWeek = SharedPreferencesUtil.getBoolean(
                    context,
                    DailySettingsConst.WEEK_SWITCH_KEY,
                    true
                )
                val finalDate = if (showWeek) "$dateString ${
                    DateUtil.getWeek(
                        context,
                        dateString
                    )
                }" else dateString

                Text(
                    text = finalDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun getDailyItemBackgroundColor(index: Int): Color {
    return if (index == 0) {
        MaterialTheme.colorScheme.surface
    } else {
        val resId = ConstUtil.backgroundColorList.getOrElse(index) { android.R.color.transparent }
        if (resId == android.R.color.transparent) {
            MaterialTheme.colorScheme.surface
        } else {
            colorResource(id = resId)
        }
    }
}
