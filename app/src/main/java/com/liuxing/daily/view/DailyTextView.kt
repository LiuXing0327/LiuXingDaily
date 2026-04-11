/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.view

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Annotation
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.markdown.span.TodoSpan
import com.liuxing.daily.R
import com.liuxing.daily.ui.audio.PlayAudioActivity
import com.liuxing.daily.ui.image.LookDailyImageActivity
import com.liuxing.daily.ui.video.LookDailyVideoActivity
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.HighlightUtil
import com.liuxing.daily.util.ImageUtil.createImageThumbnail
import com.liuxing.daily.util.TextUtil
import com.liuxing.daily.util.VideoUtil.createVideoThumbnail

/**
 * 看日记文本
 */
class DailyTextView : MaterialTextView {

    interface TodoToggleListener {
        fun onTodoToggled(updatedContent: String)
    }

    private val context: Context
    private var imagePathList: MutableSet<String> = mutableSetOf()
    private var videoPathList: MutableSet<String> = mutableSetOf()
    private var audioPathList: MutableSet<String> = mutableSetOf()
    private var dailyUuid: String = ""

    private val privateText = "***" // 日记被锁时显示的文本

    /**
     * 是否显示全文内容
     *
     * 设置为 false 时，文字和媒体都会被隐藏，仅显示 "***"。
     */
    var showAllText = true
        set(value) {
            field = value
            refreshIfNeeded()
        }

    /**
     * 是否显示图片
     *
     * 设置为 false 时，所有图片都会被替换为 "***"。
     */
    var showImages = true
        set(value) {
            field = value
            refreshIfNeeded()
        }

    /**
     * 是否显示视频
     *
     * 设置为 false 时，所有视频都会被替换为 "***"。
     */
    var showVideos = true
        set(value) {
            field = value
            refreshIfNeeded()
        }

    /**
     * 是否显示音频
     *
     * 设置为 false 时，所有音频都会被替换为 "***"。
     */
    var showAudios = true
        set(value) {
            field = value
            refreshIfNeeded()
        }

    private lateinit var originalText: SpannableString // 原始日记文本

    // 原始媒体路径
    private var originalImageList: List<String> = emptyList()
    private var originalVideoList: List<String> = emptyList()
    private var originalAudioList: List<String> = emptyList()

    /**
     * 搜索关键词
     */
    private var keyword = ""
    private var todoToggleListener: TodoToggleListener? = null

    constructor(context: Context) : super(context) {
        this.context = context
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context
        initialize()
    }

    /**
     * 初始化
     */
    private fun initialize() {
        // 启用点击事件
        movementMethod = LinkMovementMethod.getInstance()
    }

    fun setTodoToggleListener(listener: TodoToggleListener?) {
        todoToggleListener = listener
    }

    /**
     * 设置日记Uuid
     *
     * @param dailyUuid 日记Uuid
     */
    fun setDailyUuid(dailyUuid: String) {
        this.dailyUuid = dailyUuid
    }

    /**
     *  设置日记文本和媒体路径
     *
     *  @param text 日记文本
     *  @param imagePaths 图片路径
     *  @param videoPaths 视频路径
     *  @param audioPaths 音频路径
     */
    fun setDailyText(
        text: SpannableString,
        imagePaths: List<String> = emptyList(),
        videoPaths: List<String> = emptyList(),
        audioPaths: List<String> = emptyList(),
        keyword: String = ""
    ) {
        originalText = text
        originalImageList = imagePaths.toList()
        originalVideoList = videoPaths.toList()
        originalAudioList = audioPaths.toList()
        this.keyword = keyword

        refreshIfNeeded()
    }

    /**
     *  根据当前锁定/显示状态刷新文本显示
     */
    private fun refreshIfNeeded() {
        if (!showAllText || !showImages || !showVideos || !showAudios) {
            text = privateText
            return
        }

        setFormattedText(
            originalText, originalImageList, originalVideoList, originalAudioList
        )
    }

    /**
     * 设置媒体路径集合
     *
     * @param text 日记内容
     * @param newImagePathList 新的图片路径集合
     * @param newVideoPathList 新的视频路径集合
     * @param newAudioPathList 新的音频路径集合
     */
    fun setMediaPathList(
        text: SpannableString,
        newImagePathList: List<String>,
        newVideoPathList: List<String>,
        newAudioPathList: List<String>
    ) {
        this.imagePathList.clear()
        this.videoPathList.clear()
        this.audioPathList.clear()
        this.imagePathList.addAll(newImagePathList)
        this.videoPathList.addAll(newVideoPathList)
        this.audioPathList.addAll(newAudioPathList)

        // 如果被锁，显示 ***，未被锁则渲染原文和媒体
        if (!showAllText || !showImages || !showVideos || !showAudios) {
            setText(privateText)
        } else {
            setFormattedText(text, newImagePathList, newVideoPathList, newAudioPathList)
        }
    }

    /**
     * 设置格式化文本
     *
     * @param text 日记内容
     * @param newImagePathList 新的图片路径集合
     * @param newVideoPathList 新的视频路径集合
     */
    private fun setFormattedText(
        text: SpannableString,
        newImagePathList: List<String>,
        newVideoPathList: List<String>,
        newAudioPathList: List<String>
    ) {
        val spannableString = SpannableStringBuilder(text)
        val replacements = mutableListOf<Triple<Int, Int, SpannableString>>()

        newImagePathList.forEachIndexed { index, path ->
            val tag = "<img src=\"$path\"/>"
            val start = spannableString.indexOf(tag)
            if (start != -1) {
                val end = start + tag.length
                replacements.add(Triple(start, end, createImageSpannable(path, index)))
            }
        }
        newVideoPathList.forEachIndexed { index, path ->
            val tag = "<video src=\"$path\"/>"
            val start = spannableString.indexOf(tag)
            if (start != -1) {
                val end = start + tag.length
                replacements.add(Triple(start, end, createVideoSpannable(path, index)))
            }
        }
        newAudioPathList.forEachIndexed { index, path ->
            val tag = "<audio src=\"$path\"/>"
            val start = spannableString.indexOf(tag)
            if (start != -1) {
                val end = start + tag.length
                replacements.add(Triple(start, end, createAudioSpannable(path, index)))
            }
        }

        replacements.sortByDescending { it.first }
        for ((start, end, span) in replacements) {
            spannableString.replace(start, end, span)
        }
        DailyRichText.applyMarkup(spannableString)
        HighlightUtil.highlightKeyword(context, spannableString, keyword)

        setText(spannableString)
        invalidate()
    }


    /**
     * 创建图片
     *
     * @param imagePath 图片路径
     * @param imagePathIndex 图片路径索引
     *
     * @return 字符
     */
    private fun createImageSpannable(imagePath: String, imagePathIndex: Int): SpannableString {
        val imgTag = "<img src=\"$imagePath\"/>"
        val bitmap =
            createImageThumbnail(imagePath) ?: return SpannableString(ContextCompat.getDrawable(
                context,
                android.R.color.transparent
            )?.let {
                SpannableString("").apply {
                    setSpan(ImageSpan(it), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } ?: SpannableString(""))
        // 计算宽高
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val maxWidth = resources.displayMetrics.widthPixels - 40
        val toWidth = maxWidth.toFloat() / originalWidth
        val newWidth = maxWidth
        val newHeight = (originalHeight * toWidth).toInt()
        val ss = SpannableString(imgTag)
        val drawable = bitmap.toDrawable(resources).apply {
            setBounds(0, 0, newWidth, newHeight)
        }
        val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
        ss.setSpan(imageSpan, 0, ss.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(
            createImageClickableSpan(imagePathIndex),
            0,
            ss.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return ss
    }

    /**
     * 创建图片点击事件
     *
     * @param imagePathIndex 图片路径索引
     */
    private fun createImageClickableSpan(imagePathIndex: Int) = object : ClickableSpan() {
        override fun onClick(widget: View) {
            val intent = Intent(context, LookDailyImageActivity::class.java).apply {
                putExtra("look_daily_image_position", imagePathIndex)
                putExtra("look_daily_image_uuid", dailyUuid)
            }
            context.startActivity(intent)
        }

        override fun updateDrawState(ds: TextPaint) {}
    }

    /**
     * 检查图片是否存在，并去除不存在的视频标签
     *
     * @param text 日记内容
     * @param imagePathList 图片路径集合
     * @return 去除后的文本，去除了不存在视频的标签
     */
    fun checkImageExists(text: String, imagePathList: List<String>): String {
        val sb = StringBuilder(text)
        imagePathList.forEach { imagePath ->
            val imgTag = "<img src=\"$imagePath\"/>"
            if (!FileUtil().checkFileExists(imagePath)) {
                // 如果图片不存在但标签存在，则删除标签
                var startIndex = sb.indexOf(imgTag)
                while (startIndex != -1) {
                    val endIndex = startIndex + imgTag.length
                    sb.delete(startIndex, endIndex)
                    startIndex = sb.indexOf(imgTag, startIndex)
                }
            }
        }
        return sb.toString()
    }

    /**
     * 创建视频占位符
     *
     * @param videoPath 视频路径
     * @param videoPathIndex 视频路径索引
     *
     * @return SpannableString
     */
    private fun createVideoSpannable(videoPath: String, videoPathIndex: Int): SpannableString {
        val videoTag = "<video src=\"$videoPath\"/>"
        val bitmap = createVideoThumbnail(videoPath)
        if (bitmap == null) {
            val drawable = ContextCompat.getDrawable(context, android.R.color.transparent)
            val placeholder = SpannableString(" ")
            drawable?.let {
                placeholder.setSpan(ImageSpan(it), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return placeholder
        }
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val maxWidth = resources.displayMetrics.widthPixels - 40
        val scaleFactor = maxWidth.toFloat() / originalWidth
        val newWidth = maxWidth
        val newHeight = (originalHeight * scaleFactor).toInt()
        val scaledBitmap = createBitmap(newWidth, newHeight)
        val canvas = Canvas(scaledBitmap)
        val matrix = Matrix().apply { setScale(scaleFactor, scaleFactor) }
        canvas.drawBitmap(bitmap, matrix, null)
        val playDrawable =
            ContextCompat.getDrawable(context, R.drawable.baseline_play_circle_filled_24)
        playDrawable?.let {
            val width = it.intrinsicWidth
            val height = it.intrinsicHeight
            val playBitmap = createBitmap(width, height)
            val playCanvas = Canvas(playBitmap)
            it.setBounds(0, 0, width, height)
            it.draw(playCanvas)
            // 获取 @dimen/dp_16 的像素值
            val parentPaddingPx = resources.getDimensionPixelSize(R.dimen.dp_16)
            val extraRightPadding = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8f,
                resources.displayMetrics
            ).toInt()
            val rightPadding = parentPaddingPx + extraRightPadding
            val centerX = ((newWidth - rightPadding) - width) / 2f
            val centerY = (newHeight - height) / 2f
            canvas.drawBitmap(playBitmap, centerX, centerY, null)
        }

        val ss = SpannableString(videoTag.ifEmpty { " " })
        val drawable = scaledBitmap.toDrawable(resources).apply {
            setBounds(0, 0, newWidth, newHeight)
        }
        val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
        ss.setSpan(imageSpan, 0, ss.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(
            createVideoClickableSpan(videoPathIndex),
            0,
            ss.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return ss
    }


    /**
     * 创建视频点击事件
     *
     * @param videoPathIndex 视频路径索引
     */
    private fun createVideoClickableSpan(videoPathIndex: Int) = object : ClickableSpan() {
        override fun onClick(widget: View) {
            val intent = Intent(context, LookDailyVideoActivity::class.java).apply {
                putExtra("look_daily_video_path", videoPathList.elementAt(videoPathIndex))
                putExtra("look_daily_video_position", videoPathIndex)
                putExtra("look_daily_video_uuid", dailyUuid)
            }
            context.startActivity(intent)
        }

        override fun updateDrawState(ds: TextPaint) {}
    }

    /**
     * 检查视频是否存在，并去除不存在的视频标签
     *
     * @param text 日记内容
     * @param videoPathList 视频路径集合
     * @return 去除后的文本，去除了不存在视频的标签
     */
    fun checkVideoExists(text: String, videoPathList: List<String>): String {
        val sb = StringBuilder(text)
        videoPathList.forEach { videoPath ->
            val videoTag = "<video src=\"$videoPath\"/>"
            if (!FileUtil().checkFileExists(videoPath)) {
                var startIndex = sb.indexOf(videoTag)
                while (startIndex != -1) {
                    val endIndex = startIndex + videoTag.length
                    sb.delete(startIndex, endIndex)
                    startIndex = sb.indexOf(videoTag, startIndex)
                }
            }
        }
        return sb.toString()
    }

    /**
     * 创建音频占位符
     *
     * @param audioPath 音频路径
     * @param audioIndex 音频索引
     * @return CharSequence 返回带有音频的SpannableString
     */
    private fun createAudioSpannable(audioPath: String, audioIndex: Int): SpannableString {
        val audioTag = "<audio src=\"$audioPath\"/>"
        val iconDrawable =
            ContextCompat.getDrawable(context, R.drawable.baseline_audiotrack_circle_fille_24)
                ?: return SpannableString("")

        val screenWidth = resources.displayMetrics.widthPixels
        val paddingHorizontal = 40 // 左右基础间距
        // 获取 @dimen/dp_16 的像素值，并加上额外安全间距
        val parentPaddingPx = resources.getDimensionPixelSize(R.dimen.dp_16)
        val extraRightPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8f,
            resources.displayMetrics
        ).toInt()
        val rightPadding = parentPaddingPx + extraRightPadding
        val maxWidth = screenWidth - paddingHorizontal
        val borderHeightDp = 40
        val borderHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            borderHeightDp.toFloat(),
            resources.displayMetrics
        ).toInt()
        val iconHeightDp = 24
        val iconHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            iconHeightDp.toFloat(),
            resources.displayMetrics
        ).toInt()
        val originalWidth = iconDrawable.intrinsicWidth
        val originalHeight = iconDrawable.intrinsicHeight
        val scaleFactor = iconHeightPx.toFloat() / originalHeight
        val iconWidthPx = (originalWidth * scaleFactor).toInt()
        iconDrawable.setBounds(0, 0, iconWidthPx, iconHeightPx)

        val bitmapWithBorder =
            createBitmap(maxWidth, borderHeightPx)
        val canvas = Canvas(bitmapWithBorder)
        val colorPrimary = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary)
        val borderPaint = Paint().apply {
            color = colorPrimary
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        val halfStroke = borderPaint.strokeWidth / 2
        canvas.drawRect(
            halfStroke,
            halfStroke,
            maxWidth.toFloat() - rightPadding - halfStroke,
            borderHeightPx.toFloat() - halfStroke,
            borderPaint
        )
        val centerX = (maxWidth - rightPadding - iconWidthPx) / 2f
        val centerY = (borderHeightPx - iconHeightPx) / 2f
        canvas.drawBitmap(iconDrawable.toBitmap(), centerX, centerY, null)

        val drawableWithBorder = bitmapWithBorder.toDrawable(resources).apply {
            setBounds(0, 0, maxWidth - rightPadding, borderHeightPx)
        }

        val ss = SpannableString(audioTag)
        val imageSpan = ImageSpan(drawableWithBorder, ImageSpan.ALIGN_BOTTOM)
        ss.setSpan(imageSpan, 0, ss.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(
            createAudioClickableSpan(audioIndex),
            0,
            ss.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return ss
    }


    /**
     * 创建音频点击事件
     *
     * @param audioPathIndex 音频路径索引
     */
    private fun createAudioClickableSpan(audioPathIndex: Int) = object : ClickableSpan() {
        override fun onClick(widget: View) {
            val intent = Intent(context, PlayAudioActivity::class.java).apply {
                putExtra("look_daily_audio_path", audioPathList.elementAt(audioPathIndex))
                putExtra("look_daily_audio_position", audioPathIndex)
                putExtra("look_daily_audio_uuid", dailyUuid)
            }
            context.startActivity(intent)
        }

        override fun updateDrawState(ds: TextPaint) {}
    }

    /**
     * 检查音频是否存在，并去除不存在的音频标签
     *
     * @param text 日记内容
     * @param audioPathList 音频路径集合
     * @return 去除后的文本，去除了不存在音频的标签
     */
    fun checkAudioExists(text: String, audioPathList: List<String>): String {
        val sb = StringBuilder(text)
        audioPathList.forEach { audioPath ->
            val audioTag = "<audio src=\"$audioPath\"/>"
            if (!FileUtil().checkFileExists(audioPath)) {
                var startIndex = sb.indexOf(audioTag)
                while (startIndex != -1) {
                    val endIndex = startIndex + audioTag.length
                    sb.delete(startIndex, endIndex)
                    startIndex = sb.indexOf(audioTag, startIndex)
                }
            }
        }
        return sb.toString()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && handleTodoTap(event)) {
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return if (id == android.R.id.copy) {
            CopyUtil.copyTextToClipboard(
                context,
                TextUtil.replaceTag(text.substring(selectionStart, selectionEnd), "")
            )
            clearFocus()
            true
        } else super.onTextContextMenuItem(id)
    }

    private fun handleTodoTap(event: MotionEvent): Boolean {
        val sourceText = text ?: return false
        val currentText = SpannableStringBuilder(sourceText)
        val layout = layout ?: return false
        val x = event.x - totalPaddingLeft + scrollX
        val y = event.y - totalPaddingTop + scrollY
        val line = layout.getLineForVertical(y.toInt())
        val lineStart = layout.getLineStart(line)
        val lineEnd = layout.getLineEnd(line)
        val todoSpan = currentText.getSpans(lineStart, lineEnd, TodoSpan::class.java).firstOrNull() ?: return false
        val leadingMargin = todoSpan.getLeadingMargin(true)
        if (x > leadingMargin) return false

        val checked = currentText.getSpans(lineStart, lineStart, Annotation::class.java)
            .firstOrNull { it.key == DailyRichText.ANNOTATION_KEY && it.value.startsWith("todo:") }
            ?.value
            ?.removePrefix("todo:")
            ?.toBooleanStrictOrNull()
            ?: todoSpan.checked

        DailyRichText.removeTodoSpans(currentText, lineStart, lineEnd)
        DailyRichText.removeAnnotations(currentText, lineStart, lineEnd) { it.startsWith("todo:") }
        DailyRichText.applyTodoSpans(currentText, lineStart, lineEnd, !checked)

        val updatedContent = DailyRichText.export(currentText)
        originalText = SpannableString(updatedContent)
        setFormattedText(originalText, originalImageList, originalVideoList, originalAudioList)
        todoToggleListener?.onTodoToggled(updatedContent)
        return true
    }
}