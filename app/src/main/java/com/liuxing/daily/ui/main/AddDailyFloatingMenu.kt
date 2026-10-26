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

package com.liuxing.daily.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.liuxing.daily.R

@Composable
fun DailyFloatingMenu(
    onTextClick: () -> Unit = {},
    onDrawingClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    BackHandler(
        enabled = expanded
    ) {
        expanded = !expanded
    }

    val fabSize by animateDpAsState(
        targetValue = if (expanded) 48.dp else 64.dp,
        label = "FAB size"
    )

    val textString = stringResource(R.string.text)
    val drawingString = stringResource(R.string.drawing)

    val cornerRadius by animateDpAsState(
        targetValue = if (expanded) 32.dp else 12.dp,
        label = "FAB corner radius"
    )

    val cancelString = stringResource(R.string.cancel)

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        expanded = false
                    }
                    .semantics {
                        contentDescription = cancelString
                    }
            )
        }

        FloatingActionButtonMenu(
            expanded = expanded,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            button = {
                FloatingActionButton(
                    onClick = {
                        expanded = !expanded
                    },
                    modifier = Modifier.size(fabSize),
                    shape = RoundedCornerShape(cornerRadius)
                ) {
                    AnimatedContent(
                        targetState = expanded,
                        label = "FAB icon content"
                    ) { isExpanded ->
                        Icon(
                            imageVector = if (isExpanded) Icons.Outlined.Close else Icons.Outlined.Edit,
                            contentDescription = stringResource(
                                if (isExpanded) R.string.close else R.string.add
                            ),
                            modifier = Modifier.size(if (isExpanded) 24.dp else 32.dp)
                        )
                    }
                }
            }
        ) {
            FloatingActionButtonMenuItem(
                modifier = Modifier.semantics {
                    contentDescription = textString
                },
                onClick = {
                    expanded = false
                    onTextClick()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.TextFields,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = textString
                    )
                }
            )

            FloatingActionButtonMenuItem(
                modifier = Modifier.semantics {
                    contentDescription = drawingString
                },
                onClick = {
                    expanded = false
                    onDrawingClick()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.ColorLens,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = drawingString
                    )
                }
            )
        }
    }
}