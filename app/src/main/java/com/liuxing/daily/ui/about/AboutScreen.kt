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

package com.liuxing.daily.ui.about

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liuxing.daily.R
import com.liuxing.daily.ui.compose.list.ListItemCard
import com.liuxing.daily.ui.compose.list.ListItemData
import com.liuxing.daily.ui.compose.scaffold.WallpaperScaffold
import com.liuxing.daily.ui.compose.scaffold.getTopAppBarColors
import com.liuxing.daily.ui.updatelog.UpdateLogActivity
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.util.VersionUtil

private const val ABOUT_ITEM_COUNT = 4
private const val SOURCE_CODE_URL = "https://github.com/LiuXing0327/LiuXingDaily"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    wallpaperBitmap: Bitmap?,
    wallpaperAlpha: Float,
    cardAlpha: Float,
    onBack: () -> Unit,
    onJoinGroupClick: () -> Unit
) {
    val context = LocalContext.current

    val qqGroup = stringResource(R.string.qq_920994447).split(Regex("[：:]"))

    val email = stringResource(R.string.my_email)

    val appIconSize = 80.dp

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val message = stringResource(R.string.copy_successful)

    WallpaperScaffold(
        wallpaperBitmap = wallpaperBitmap, wallpaperAlpha = wallpaperAlpha, topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) }, navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }, colors = wallpaperBitmap.getTopAppBarColors()
            )
        }, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { contentPadding, _ ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = stringResource(R.string.app_icon),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(appIconSize)
                        .clip(CircleShape)
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.app_icon),
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp)
                        .size(appIconSize)
                        .clip(CircleShape)
                )
            }

            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .align(Alignment.CenterHorizontally),
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontFamily = FontFamily.SansSerif
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp),
                    onClick = { IntentUtil.startActivity(context, UpdateLogActivity::class.java) }
                ) {
                    Text(
                        text = VersionUtil.getVersionName(context),
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 8.dp)
                            .padding(
                                horizontal = 6.dp, vertical = 2.dp
                            )
                            .align(Alignment.CenterHorizontally),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.SansSerif,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

            }

            ListItemCard(
                data = ListItemData(
                    icon = Icons.Outlined.AccountCircle,
                    title = stringResource(R.string.author_name),
                    supportingText = "正努力做到更好",
                    index = 0,
                    count = ABOUT_ITEM_COUNT
                ),
                alpha = cardAlpha
            ) {

            }

            ListItemCard(
                data = ListItemData(
                    icon = Icons.Outlined.Email,
                    title = "Email",
                    supportingText = buildAnnotatedString {
                        withLink(
                            LinkAnnotation.Url(
                                url = "mailto:$email",
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                )
                            )
                        ) {
                            append(email)
                        }
                    },
                    index = 1,
                    count = ABOUT_ITEM_COUNT
                ),
                alpha = cardAlpha
            ) {
                CopyUtil.copyTextToClipboard(context, email)

                SnackbarUtil.showSnackbarShort(
                    hostState = snackbarHostState,
                    scope = scope,
                    message = message
                )
            }

            ListItemCard(
                data = ListItemData(
                    icon = Icons.Outlined.Group,
                    title = qqGroup.getOrNull(0) ?: "",
                    supportingText = qqGroup.getOrNull(1) ?: "",
                    index = 2,
                    count = ABOUT_ITEM_COUNT
                ),
                alpha = cardAlpha
            ) {
                onJoinGroupClick()
            }

            ListItemCard(
                data = ListItemData(
                    icon = Icons.Outlined.Code,
                    title = stringResource(R.string.source_code),
                    supportingText = buildAnnotatedString {
                        withLink(
                            LinkAnnotation.Url(
                                url = SOURCE_CODE_URL,
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                )
                            )
                        ) {
                            append(SOURCE_CODE_URL)
                        }
                    },
                    index = 3,
                    count = ABOUT_ITEM_COUNT
                ),
                alpha = cardAlpha
            ) {
                CopyUtil.copyTextToClipboard(context, SOURCE_CODE_URL)

                SnackbarUtil.showSnackbarShort(
                    hostState = snackbarHostState,
                    scope = scope,
                    message = message
                )
            }
        }
    }
}