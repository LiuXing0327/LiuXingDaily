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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.liuxing.daily.R

data class ListItemData(
    val icon: ImageVector? = null,
    val title: Any,
    val supportingText: Any = "",
    val index: Int = 1,
    val count: Int = 1
)

sealed class ListItemTrailing {
    object None : ListItemTrailing()
    data class Switch(val checked: Boolean) : ListItemTrailing()
}

@Composable
fun ListItemCard(
    data: ListItemData,
    alpha: Float = 1f,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    trailing: ListItemTrailing = ListItemTrailing.None,
    shape: Shape? = null,
    onAction: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow
        ), label = "scale"
    )

    val animatedCornerRadius by animateDpAsState(
        targetValue = if (isPressed) 24.dp else 16.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "corner"
    )

    // 如果没传 shape，则根据 index/count 自动计算
    val currentShape = shape ?: getListItemShape(data.index, data.count, animatedCornerRadius)

    Card(
        shape = currentShape,
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = alpha)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(currentShape)
            .clickable(
                interactionSource = interactionSource, indication = null, onClick = onAction
            )) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = {
                when (val text = data.title) {
                    is String -> if (text.isNotBlank()) Text(
                        text, style = MaterialTheme.typography.titleMedium
                    )

                    is AnnotatedString -> Text(text, style = MaterialTheme.typography.titleMedium)
                }
            },
            supportingContent = when (val text = data.supportingText) {
                is AnnotatedString -> if (text.isNotEmpty()) {
                    { Text(text = text, style = MaterialTheme.typography.bodyMedium) }
                } else null

                is String -> if (text.isNotBlank()) {
                    { Text(text = text, style = MaterialTheme.typography.bodyMedium) }
                } else null

                else -> null
            },
            leadingContent = data.icon?.let {
                {
                    Icon(
                        it,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            trailingContent = when (trailing) {
                is ListItemTrailing.None -> null
                is ListItemTrailing.Switch -> {
                    {
                        Switch(
                            checked = trailing.checked,
                            onCheckedChange = null,
                            thumbContent = if (trailing.checked) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            } else {
                                {
                                    Icon(
                                        Icons.Default.Close,
                                        null,
                                        Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    }
                }
            })
    }
}

/**
 * 带输入框的列表项组件
 */
@Composable
fun InputListItemCard(
    data: ListItemData,
    enabled: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    alpha: Float = 1f,
    onEnabledChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedCornerRadius by animateDpAsState(
        targetValue = if (isPressed) 24.dp else 16.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "inputToggleCorner"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        ListItemCard(
            data = data,
            alpha = alpha,
            trailing = ListItemTrailing.Switch(enabled),
            shape = if (enabled) RoundedCornerShape(4.dp) else null,
            onAction = { onEnabledChange(!enabled) }
        )

        AnimatedVisibility(
            visible = enabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val lastShape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 4.dp,
                bottomStart = animatedCornerRadius,
                bottomEnd = animatedCornerRadius
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp),
                shape = lastShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha)
                )
            ) {
                var passwordVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    label = { Text(stringResource(R.string.password)) },
                    placeholder = { Text(placeholder) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                null
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SegmentedListItemWrapper(
    selected: Boolean, index: Int, count: Int, label: String, onClick: () -> Unit
) {
    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        content = { Text(label) },
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun Subheader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
    )
}

private fun getListItemShape(index: Int, count: Int, baseShape: Dp = 16.dp): Shape = when {
    count == 1 -> RoundedCornerShape(baseShape)
    index == 0 -> RoundedCornerShape(
        topStart = baseShape,
        topEnd = baseShape,
        bottomStart = 4.dp,
        bottomEnd = 4.dp
    )

    index == count - 1 -> RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 4.dp,
        bottomStart = baseShape,
        bottomEnd = baseShape
    )

    else -> RoundedCornerShape(4.dp)
}
