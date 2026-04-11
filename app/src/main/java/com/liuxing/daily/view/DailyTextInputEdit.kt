/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Annotation
import android.text.Editable
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ImageSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.color.MaterialColors
import com.google.android.material.textfield.TextInputEditText
import com.liuxing.daily.markdown.span.TodoSpan
import com.liuxing.daily.R
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.ImageUtil.createImageThumbnail
import com.liuxing.daily.util.LogUtil
import com.liuxing.daily.util.TextUtil
import com.liuxing.daily.util.VideoUtil.createVideoThumbnail

/**
 * 写日记编辑
 */
class DailyTextInputEdit : TextInputEditText {

    private val placeholder = "&"
    private val context: Context
    private var isImageInserted = false
    private var isVideoInserted = false
    private var isAudioInserted = false
    private var imagePathList: MutableSet<String> = mutableSetOf()
    private var formattedText: String = ""
    private var imageInsertionListener: ImageInsertionListener? = null
    private var imageDeletionListener: ImageDeletionListener? = null
    private val imageMap: MutableMap<String, String> = mutableMapOf()
    private var oldImageList: List<String> = listOf()
    private var oldVideoList: List<String> = listOf()
    private var oldAudioList: List<String> = listOf()
    private var audioInsertionListener: AudioInsertionListener? = null
    private var videoInsertionListener: VideoInsertionListener? = null
    private var audioPathList: MutableSet<String> = mutableSetOf()
    private var videoPathList: MutableSet<String> = mutableSetOf()
    private var videoDeletionListener: VideoDeletionListener? = null
    private var audioDeletionListener: AudioDeletionListener? = null
    private var textChangeStart = -1
    private var textChangeBefore = 0
    private var textChangeCount = 0
    private var isHandlingRichEnter = false
    private var isNormalizingRichBlocks = false
    private var isApplyingRichFormatting = false

    constructor(context: Context) : super(context) {
        this.context = context
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context
        initialize()
    }

    /**
     * 设置旧版本的图片集合
     *
     * @param oldImageList 旧版本的图片集合
     */
    fun setOldImageList(oldImageList: List<String>) {
        this.oldImageList = oldImageList
        val missingImages = oldImageList.filterNot { imagePathList.contains(it) }
        if (missingImages.isNotEmpty()) {
            insertImages(missingImages)
        }
    }

    /**
     * 获取旧版本的图片集合
     */
    fun getOldImageList(): List<String> {
        return oldImageList
    }

    /**
     * 设置旧版本的视频集合
     *
     * @param oldVideoList 旧版本的视频集合
     */
    fun setOldVideoList(oldVideoList: List<String>) {
        this.oldVideoList = oldVideoList
        val missingVideos = oldVideoList.filterNot { videoPathList.contains(it) }
        if (missingVideos.isNotEmpty()) {
            insertVideos(missingVideos)
        }
    }

    /**
     * 获取旧版本的视频集合
     *
     * @return 旧版本的视频集合
     */
    fun getOldVideoList(): List<String> {
        return oldVideoList
    }

    fun setOldAudioList(oldAudioList: List<String>) {
        this.oldAudioList = oldAudioList
        val missingAudios = oldAudioList.filterNot { audioPathList.contains(it) }
        if (missingAudios.isNotEmpty()) {
            insertAudio(missingAudios)
        }
    }

    fun getOldAudioList(): List<String> {
        return oldAudioList
    }

    /**
     * 解析文本中的图片标签
     *
     * @param text 要解析的文本
     * @return 图片路径列表
     */
    private fun extractImagePaths(text: String): List<String> {
        val imagePaths = mutableListOf<String>()
        val regex = Regex("<img src=\"(.*?)\"/>")
        regex.findAll(text).forEach { matchResult ->
            matchResult.groupValues.getOrNull(1)?.let { imagePaths.add(it) }
        }
        return imagePaths
    }

    /**
     * 设置编辑内容
     *
     * @param content 文本
     */
    fun setEditContent(content: String) {
        val imagePaths = extractImagePaths(content)
        val videoPaths = extractVideoPaths(content)
        val audioPaths = extractAudioPaths(content)

        val ssb = SpannableStringBuilder(content)
        val replacements = mutableListOf<Triple<Int, Int, CharSequence>>()

        imagePaths.forEach { path ->
            val tag = "<img src=\"$path\"/>"
            var index = content.indexOf(tag)
            while (index != -1) {
                replacements.add(
                    Triple(
                        index,
                        index + tag.length,
                        createImageSpannable(path)
                    )
                )
                index = content.indexOf(tag, index + 1)
            }
        }

        videoPaths.forEach { path ->
            val tag = "<video src=\"$path\"/>"
            var index = content.indexOf(tag)
            while (index != -1) {
                replacements.add(
                    Triple(
                        index,
                        index + tag.length,
                        createVideoSpannable(path)
                    )
                )
                index = content.indexOf(tag, index + 1)
            }
        }

        audioPaths.forEach { path ->
            val tag = "<audio src=\"$path\"/>"
            var index = content.indexOf(tag)
            while (index != -1) {
                replacements.add(
                    Triple(
                        index,
                        index + tag.length,
                        createAudioSpannable(path)
                    )
                )
                index = content.indexOf(tag, index + 1)
            }
        }

        replacements.sortByDescending { it.first }

        replacements.forEach { (start, end, replacement) ->
            ssb.replace(start, end, replacement)
        }
        DailyRichText.applyMarkup(ssb)

        setText(ssb)
        setSelection(ssb.length)
    }


    /**
     * 解析视频路径
     *
     * @param text 文本内容
     * @return 视频路径列表
     */
    private fun extractVideoPaths(text: String): List<String> {
        val videoPaths = mutableListOf<String>()
        val regex = Regex("<video src=\"(.*?)\"/>")
        regex.findAll(text).forEach { matchResult ->
            matchResult.groupValues.getOrNull(1)?.let { videoPaths.add(it) }
        }
        return videoPaths
    }

    /**
     * 解析音频路径
     *
     * @param text 文本内容
     * @return 音频路径列表
     */
    private fun extractAudioPaths(text: String): List<String> {
        val audioPaths = mutableListOf<String>()
        val regex = Regex("<audio src=\"(.*?)\"/>")
        regex.findAll(text).forEach { matchResult ->
            matchResult.groupValues.getOrNull(1)?.let { audioPaths.add(it) }
        }
        return audioPaths
    }


    /**
     * 初始化
     */
    private fun initialize() {
        gravity = Gravity.TOP
        addTextChangedListener(textWatcher)
    }

    /**
     * 插入图片监听
     */
    interface ImageInsertionListener {
        fun onImageInserted()
    }

    /**
     * 设置图片插入监听
     */
    fun setImageInsertionListener(listener: ImageInsertionListener?) {
        this.imageInsertionListener = listener
    }

    /**
     * 删除图片监听
     */
    interface ImageDeletionListener {
        fun onImageDeleted(imagePath: String)
    }

    /**
     * 设置删除图片监听
     */
    fun setImageDeletionListener(listener: ImageDeletionListener?) {
        this.imageDeletionListener = listener
    }

    /**
     * 插入图片
     *
     * @param imagePathList 图片路径集合
     */
    fun insertImages(imagePathList: List<String?>) {
        val editable = text ?: return

        imagePathList.forEach { path ->
            path?.let {
                if (!this.imagePathList.contains(it) && FileUtil().checkFileExists(it)) {
                    this.imagePathList.add(it)

                    // 检查文本中是否已经插入该图片占位符，避免重复插入
                    val sequence = createImageSpannable(it)
                    if (!editable.contains(sequence)) {
                        if (selectionStart > 0 && editable[selectionStart - 1] != '\n') {
                            editable.insert(selectionStart, "\n\n")
                        }
                        editable.insert(selectionStart, sequence)
                        val newLength = editable.length
                        if (selectionStart + sequence.length <= newLength) {
                            editable.insert(selectionStart + sequence.length, "\n")
                        } else {
                            editable.append("\n")
                        }
                        isImageInserted = true
                    }
                }
            }
        }
        if(isImageInserted) {
            setSelection(editable.length)
            imageInsertionListener?.onImageInserted()
        }
    }


    /**
     * 获取插入的图片
     */
    fun getInsertedImages(): List<String> = imagePathList.filter { text?.contains(it) == true }

    private val textWatcher: TextWatcher = object : TextWatcher {
        private var previousText: String? = null

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            previousText = s.toString()
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            textChangeStart = start
            textChangeBefore = before
            textChangeCount = count
            isImageInserted = false
            isVideoInserted = false
            isAudioInserted = false
            if (s.length < previousText!!.length) {
                val deletedString = previousText!!.substring(start, start + before)
                val removeImagePathList = imagePathList.filter { deletedString.contains(it) }
                removeImagePathList.forEach { imagePath ->
                    imagePathList.remove(imagePath)
                    imageDeletionListener?.onImageDeleted(imagePath)
                }
                val removeVideoPathList = videoPathList.filter { deletedString.contains(it) }
                removeVideoPathList.forEach { videoPath ->
                    videoPathList.remove(videoPath)
                    videoDeletionListener?.onVideoDeleted(videoPath)
                }
                val removeAudioPathList = audioPathList.filter { deletedString.contains(it) }
                removeAudioPathList.forEach { audioPath ->
                    audioPathList.remove(audioPath)
                    audioDeletionListener?.onAudioDeleted(audioPath)
                }
            }
        }

        override fun afterTextChanged(s: Editable) {
            if (
                isImageInserted ||
                isVideoInserted ||
                isAudioInserted ||
                isHandlingRichEnter ||
                isNormalizingRichBlocks ||
                isApplyingRichFormatting
            ) {
                return
            }
            val insertedNewline =
                textChangeBefore == 0 &&
                    textChangeCount == 1 &&
                    textChangeStart in s.indices &&
                    s[textChangeStart] == '\n'
            if (insertedNewline) {
                handleRichEnter(s, textChangeStart)
            }
            refreshRichFormatting(s, preserveCursorLine = insertedNewline)
        }
    }

    /**
     * 更新格式化文本
     *
     * @param s 字符
     */
    private fun updateFormattedText(s: CharSequence) {
        imagePathList.forEachIndexed { index, imagePath ->
            imageMap[imagePath] = "$placeholder$index$placeholder"
        }

        formattedText = buildString {
            val stringBuffer = StringBuffer(s.toString())
            imageMap.forEach { (imgPath, placeHolder) ->
                val index = stringBuffer.indexOf(imgPath)
                if (index != -1) {
                    stringBuffer.replace(index, index + imgPath.length, placeHolder)
                }
            }
            if (stringBuffer.startsWith(placeholder)) {
                stringBuffer.insert(0, " ")
            }
            append(stringBuffer)
        }
    }

    /**
     * 创建图片
     *
     * @param imagePath 图片路径
     *
     * @return 字符
     */
    private fun createImageSpannable(imagePath: String): CharSequence {
        val imgTag = "<img src=\"$imagePath\"/>"
        val bitmap = createImageThumbnail(imagePath) ?: return imgTag
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
        return ss
    }

    /**
     * 获取字数
     *
     * @return 字数
     */
    fun getWordCount(): Int {
        return TextUtil.getWordCount(getExportText())
    }

    /**
     * 解决上下滑动时光标跳跃
     *
     * @param event
     * @return 结果
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && handleTodoCheckboxTap(event)) {
            return true
        }
        return super.onTouchEvent(event)
    }

    /**
     * 音频插入监听
     */
    interface AudioInsertionListener {
        fun onAudioInserted()
    }

    /**
     * 视频插入监听
     */
    interface VideoInsertionListener {
        fun onVideoInserted()
    }

    /**
     * 设置音频插入监听
     */
    fun setAudioInsertionListener(listener: AudioInsertionListener?) {
        this.audioInsertionListener = listener
    }

    /**
     * 设置视频插入监听
     */
    fun setVideoInsertionListener(listener: VideoInsertionListener?) {
        this.videoInsertionListener = listener
    }

    interface AudioDeletionListener {
        fun onAudioDeleted(audioPath: String)
    }

    interface VideoDeletionListener {
        fun onVideoDeleted(videoPath: String)
    }

    fun setAudioDeletionListener(listener: AudioDeletionListener?) {
        this.audioDeletionListener = listener
    }

    fun setVideoDeletionListener(listener: VideoDeletionListener?) {
        this.videoDeletionListener = listener
    }

    /**
     * 插入音频
     *
     * @param audioPaths 音频路径集合
     */
    fun insertAudio(audioPaths: List<String?>) {
        val editable = text ?: return
        this.audioPathList.clear()
        val currentLength = editable.length
        audioPaths.forEach { path ->
            path?.let {
                if (!this.audioPathList.contains(it) && FileUtil().checkFileExists(it)) {
                    this.audioPathList.add(it)
                    val sequence = createAudioSpannable(it)
                    if (!editable.contains(sequence)) {
                        // 确保格式一致性
                        if (selectionStart > 0 && editable[selectionStart - 1] != '\n') {
                            editable.insert(selectionStart, "\n\n")
                        }
                        if (selectionStart + sequence.length in 0..currentLength) {
                            editable.insert(selectionStart, sequence)
                        } else {
                            editable.append(sequence)
                        }
                        val newLength = editable.length
                        if (selectionStart + sequence.length <= newLength) {
                            editable.insert(selectionStart + sequence.length, "\n")
                        } else {
                            editable.append("\n")
                        }
                        isAudioInserted = true
                    }
                }
            }
        }
        if(isAudioInserted){
            setSelection(text.toString().length)
            audioInsertionListener?.onAudioInserted()
        }
    }


    /**
     * 插入视频
     *
     * @param videoPaths 视频路径集合
     */
    fun insertVideos(videoPaths: List<String?>) {
        val editable = text ?: return
        this.videoPathList.clear()
        val currentLength = editable.length
        videoPaths.forEach { path ->
            path?.let {
                if (!this.videoPathList.contains(it) && FileUtil().checkFileExists(it)) {
                    this.videoPathList.add(it)
                    val sequence = createVideoSpannable(it)
                    if (!editable.contains(sequence)) {
                        if (selectionStart > 0 && editable[selectionStart - 1] != '\n') {
                            editable.insert(selectionStart, "\n\n")
                        }
                        if (selectionStart + sequence.length in 0..currentLength) {
                            editable.insert(selectionStart, sequence)
                        } else {
                            editable.append(sequence)
                        }
                        val newLength = editable.length
                        if (selectionStart + sequence.length <= newLength) {
                            editable.insert(selectionStart + sequence.length, "\n")
                        } else {
                            editable.append("\n")
                        }
                        isVideoInserted = true
                    }
                }
            }
        }
        if(isVideoInserted){
            setSelection(text.toString().length)
            videoInsertionListener?.onVideoInserted()
        }
    }

    /**
     * 创建音频占位符
     *
     * @param audioPath 音频路径
     * @return CharSequence 返回带有音频的SpannableString
     */
    private fun createAudioSpannable(audioPath: String): SpannableString {
        val audioTag = "<audio src=\"$audioPath\"/>"
        val iconDrawable =
            ContextCompat.getDrawable(context, R.drawable.baseline_audiotrack_circle_fille_24)
                ?: return SpannableString("")
        val screenWidth = resources.displayMetrics.widthPixels
        val paddingHorizontal = 0 // 左右基础间距
        // TextInputEditText 固定 paddingEnd，自动适配 dp
        val textInputPaddingEnd = paddingEnd
        val extraRightPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics
        ).toInt()
        val rightPadding = textInputPaddingEnd + extraRightPadding
        val maxWidth = screenWidth - paddingHorizontal
        val borderHeightDp = 40
        val borderHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            borderHeightDp.toFloat(),
            resources.displayMetrics
        ).toInt()
        val iconHeightDp = 24
        val iconHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, iconHeightDp.toFloat(), resources.displayMetrics
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
            setBounds(0, 0, maxWidth - rightPadding, borderHeightPx) // 设置最终的绘制范围
        }
        val ss = SpannableString(audioTag)
        val imageSpan = ImageSpan(drawableWithBorder, ImageSpan.ALIGN_BOTTOM)
        ss.setSpan(imageSpan, 0, ss.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return ss
    }

    /**
     * 创建视频占位符
     *
     * @param videoPath 视频路径
     * @return CharSequence 返回包含视频占位符的 SpannableString
     */
    private fun createVideoSpannable(videoPath: String): CharSequence {
        val videoTag = "<video src=\"$videoPath\"/>"
        val bitmap = createVideoThumbnail(videoPath)
        val spannableString = SpannableString(videoTag)
        // 如果 bitmap 为空
        if (bitmap == null) {
            val drawable = ContextCompat.getDrawable(context, android.R.color.transparent)
            if (drawable != null) {
                spannableString.setSpan(
                    ImageSpan(drawable),
                    0,
                    spannableString.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return spannableString
        }
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val maxWidth = resources.displayMetrics.widthPixels - 40
        val toWidth = maxWidth.toFloat() / originalWidth
        val scaleFactor = maxWidth.toFloat() / originalWidth
        val newWidth = maxWidth
        val newHeight = (originalHeight * toWidth).toInt()
        val ss = SpannableString(videoTag)
        val createBitmap = createBitmap(newWidth, newHeight)
        val canvas = Canvas(createBitmap)
        val matrix = Matrix()
        matrix.setScale(scaleFactor, scaleFactor)
        canvas.drawBitmap(bitmap, matrix, null)
        val playDrawable =
            ContextCompat.getDrawable(context, R.drawable.baseline_play_circle_filled_24)
        val playBitmap = playDrawable?.let {
            val width = it.intrinsicWidth
            val height = it.intrinsicHeight
            val bitmap = createBitmap(width, height)
            val canvas = Canvas(bitmap)
            it.setBounds(0, 0, width, height)
            it.draw(canvas)
            bitmap
        }
        playBitmap?.let {
            val centerX = ((newWidth - paddingEnd) - it.width) / 2f
            val centerY = (newHeight - it.height) / 2f
            canvas.drawBitmap(it, centerX, centerY, null)
        }
        val drawable = createBitmap.toDrawable(resources).apply {
            setBounds(0, 0, newWidth, newHeight)
        }
        val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
        ss.setSpan(imageSpan, 0, ss.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return ss
    }

    /** 切换粗体文本 */
    fun toggleBoldText() = toggleStyle(Typeface.BOLD)

    /** 切换斜体文本 */
    fun toggleItalicText() = toggleStyle(Typeface.ITALIC)

    /** 切换下划线文本 */
    fun toggleUnderlineText() = toggleUnderline()

    /** 切换删除线文本 */
    fun toggleStrikethroughText() = toggleStrikethroughSpan()

    /** 应用文本颜色 */
    fun applyTextColor(color: Int) = withValidSelection { editable, start, end ->
        DailyRichText.removeForegroundSpans(editable, start, end)
        DailyRichText.removeAnnotations(editable, start, end) { it.startsWith("color:") }
        DailyRichText.applyColorSpans(editable, start, end, color)
    }

    /** 应用文本高亮颜色 */
    fun applyHighlightColor(color: Int) = withValidSelection { editable, start, end ->
        DailyRichText.removeBackgroundSpans(editable, start, end)
        DailyRichText.removeAnnotations(editable, start, end) { it.startsWith("bg:") }
        DailyRichText.applyHighlightSpans(editable, start, end, color)
    }

    /** 应用文本字号 */
    fun applyFontPreset(preset: DailyRichText.FontPreset) = withValidSelection { editable, start, end ->
        DailyRichText.removeRelativeSizeSpans(editable, start, end)
        DailyRichText.removeAnnotations(editable, start, end) { it.startsWith("size:") }
        DailyRichText.applyFontPresetSpans(editable, start, end, preset)
    }

    /** 应用标题层级 */
    fun applyHeading(level: Int) = performRichFormattingChange {
        withParagraphSelection(createEmptyParagraph = true) { editable, start, end ->
            clearParagraphFormats(editable, start, end)
            DailyRichText.applyHeadingSpans(editable, start, end, level)
        }
    }

    /** 应用引用块 */
    fun applyBlockQuote() = performRichFormattingChange {
        withParagraphSelection(createEmptyParagraph = true) { editable, start, end ->
            DailyRichText.removeQuoteSpans(editable, start, end)
            DailyRichText.removeAnnotations(editable, start, end) { it == "blockquote" }
            DailyRichText.applyQuoteSpans(editable, start, end)
        }
    }

    /** 应用文本居中 */
    fun applyCenterAlignment() = performRichFormattingChange {
        withParagraphSelection(createEmptyParagraph = true) { editable, start, end ->
            DailyRichText.removeAlignmentSpans(editable, start, end)
            DailyRichText.removeAnnotations(editable, start, end) { it.startsWith("align:") }
            DailyRichText.applyAlignmentSpans(editable, start, end, Layout.Alignment.ALIGN_CENTER)
        }
    }

    /** 应用文本左对齐 */
    fun applyNormalAlignment() = performRichFormattingChange {
        withParagraphSelection(createEmptyParagraph = true) { editable, start, end ->
            DailyRichText.removeAlignmentSpans(editable, start, end)
            DailyRichText.removeAnnotations(editable, start, end) { it.startsWith("align:") }
            DailyRichText.applyAlignmentSpans(editable, start, end, Layout.Alignment.ALIGN_NORMAL)
        }
    }

    /** 应用超链接 */
    fun applyLink(url: String) = withValidSelection { editable, start, end ->
        DailyRichText.removeUrlSpans(editable, start, end)
        DailyRichText.removeAnnotations(editable, start, end) { it.startsWith("a:") }
        DailyRichText.applyLinkSpans(editable, start, end, url)
    }

    /** 插入分割线 */
    fun insertHorizontalRule() {
        val editable = text ?: return
        val cursor = maxOf(selectionStart, 0)
        val lineStart = findLineStart(editable, cursor)
        val lineEnd = findLineEndExclusive(editable, cursor)
        val currentLineBlank = getLineContent(editable, lineStart, lineEnd).isBlank()
        val markerLine = "${DailyRichText.HR_MARKER}\n"
        val replaceStart: Int
        val replaceEnd: Int
        val insertText: String

        if (currentLineBlank) {
            replaceStart = lineStart
            replaceEnd = lineEnd
            insertText = markerLine
        } else {
            replaceStart = cursor
            replaceEnd = cursor
            insertText = if (cursor == 0) {
                DailyRichText.HR_PLACEHOLDER
            } else {
                "\n${DailyRichText.HR_PLACEHOLDER.trim('\n')}\n"
            }
        }
        editable.replace(replaceStart, replaceEnd, insertText)
        val markerIndex = editable.toString().indexOf(DailyRichText.HR_MARKER, replaceStart)
        if (markerIndex != -1) {
            DailyRichText.applyHorizontalRuleSpan(editable, markerIndex)
        }
        setSelection((replaceStart + insertText.length).coerceAtMost(editable.length))
    }

    /** 切换无序列表 */
    fun toggleBulletList() = performRichFormattingChange {
        transformSelectedParagraphs { editable, ranges ->
            val allBullets = ranges.all { isBulletLine(editable, it.first) }
            ranges.forEach { (start, end) ->
                clearListFormats(editable, start, end)
                if (!allBullets) {
                    DailyRichText.applyBulletListSpans(editable, start, end)
                }
            }
        }
    }

    /** 切换有序列表 */
    fun toggleOrderedList() = performRichFormattingChange {
        transformSelectedParagraphs { editable, ranges ->
            val allOrdered = ranges.all { isOrderedLine(editable, it.first) }
            ranges.forEachIndexed { index, range ->
                clearListFormats(editable, range.first, range.second)
                if (!allOrdered) {
                    DailyRichText.applyOrderedListSpans(editable, range.first, range.second, index + 1)
                }
            }
        }
    }

    /** 切换待办列表 */
    fun toggleTodoList(checked: Boolean = false) = performRichFormattingChange {
        transformSelectedParagraphs { editable, ranges ->
            val allTodo = ranges.all { isTodoLine(editable, it.first) }
            ranges.forEach { (start, end) ->
                clearListFormats(editable, start, end)
                if (!allTodo) {
                    DailyRichText.applyTodoSpans(editable, start, end, checked)
                }
            }
        }
    }

    /** 切换样式 */
    private fun toggleStyle(style: Int) {
        withValidSelection { editable, start, end ->
            val token = if (style == Typeface.BOLD) "b" else "i"
            val hasStyle = editable.getSpans(start, end, StyleSpan::class.java).any { it.style == style }
            if (hasStyle) {
                DailyRichText.removeStyleSpans(editable, start, end, style)
                DailyRichText.removeAnnotations(editable, start, end) { it == token }
            } else {
                editable.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                DailyRichText.addAnnotation(editable, start, end, token)
            }
        }
    }

    /** 切换下划线 */
    private fun toggleUnderline() {
        withValidSelection { editable, start, end ->
            val hasUnderline = editable.getSpans(start, end, UnderlineSpan::class.java).isNotEmpty()
            if (hasUnderline) {
                DailyRichText.removeUnderlineSpans(editable, start, end)
                DailyRichText.removeAnnotations(editable, start, end) { it == "u" }
            } else {
                editable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                DailyRichText.addAnnotation(editable, start, end, "u")
            }
        }
    }

    /** 切换删除线 */
    private fun toggleStrikethroughSpan() {
        withValidSelection { editable, start, end ->
            val hasStrike = editable.getSpans(start, end, StrikethroughSpan::class.java).isNotEmpty()
            if (hasStrike) {
                DailyRichText.removeStrikeSpans(editable, start, end)
                DailyRichText.removeAnnotations(editable, start, end) { it == "s" }
            } else {
                editable.setSpan(StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                DailyRichText.addAnnotation(editable, start, end, "s")
            }
        }
    }

    /**
     * 导出不带标签的文本
     *
     * @return 导出的文本字符串（为空时返回 ""）
     */
    fun getExportText(): String {
        val editable = text ?: return ""
        if (editable.isEmpty()) return ""

        return DailyRichText.export(editable).also {
            LogUtil.d(message = "Export: $it")
        }
    }

    private inline fun performRichFormattingChange(action: () -> Unit) {
        val editable = text ?: return
        isApplyingRichFormatting = true
        try {
            action()
        } finally {
            isApplyingRichFormatting = false
        }
        refreshRichFormatting(editable)
    }

    private fun refreshRichFormatting(editable: Editable, preserveCursorLine: Boolean = false) {
        isNormalizingRichBlocks = true
        try {
            cleanupLegacyRichMarkers(editable)
            normalizeEmptyBlockParagraphs(editable, preserveCursorLine = preserveCursorLine)
            DailyRichText.normalizeListSpans(editable)
            DailyRichText.normalizeQuoteSpans(editable)
            DailyRichText.normalizeAlignmentSpans(editable)
            DailyRichText.normalizeHeadingSpans(editable)
        } finally {
            isNormalizingRichBlocks = false
        }
        invalidate()
        requestLayout()
        updateFormattedText(editable)
    }

    private inline fun withValidSelection(block: (Editable, Int, Int) -> Unit) {
        val editable = text ?: return
        val start = minOf(selectionStart, selectionEnd)
        val end = maxOf(selectionStart, selectionEnd)
        if (start < 0 || end <= start) return
        block(editable, start, end)
    }

    private inline fun withParagraphSelection(
        createEmptyParagraph: Boolean = false,
        block: (Editable, Int, Int) -> Unit
    ) {
        val editable = text ?: return
        val start = minOf(selectionStart, selectionEnd).coerceAtLeast(0)
        val end = maxOf(selectionStart, selectionEnd).coerceAtLeast(start)
        val textValue = editable.toString()
        val paragraphStart = textValue.lastIndexOf('\n', (start - 1).coerceAtLeast(0)).let {
            if (it == -1) 0 else it + 1
        }
        val paragraphEndIndex = if (end >= textValue.length) -1 else textValue.indexOf('\n', end)
        var paragraphEnd = if (paragraphEndIndex == -1) editable.length else paragraphEndIndex + 1
        val createdEmptyParagraph = paragraphEnd <= paragraphStart
        if (createdEmptyParagraph) {
            if (!createEmptyParagraph) return
            val anchoredRange = ensureParagraphAnchor(editable, paragraphStart, paragraphEnd)
            paragraphEnd = anchoredRange.second
        }
        block(editable, paragraphStart, paragraphEnd)
        val collapsedSelection = start == end
        val keepCursorOnCurrentParagraph =
            createEmptyParagraph &&
                collapsedSelection &&
                getLineContent(editable, paragraphStart, paragraphEnd).isBlank()
        val targetSelection = if (collapsedSelection) {
            start.coerceAtMost(editable.length)
        } else if (createdEmptyParagraph && createEmptyParagraph || keepCursorOnCurrentParagraph) {
            paragraphStart
        } else {
            paragraphEnd
        }
        setSelection(targetSelection.coerceAtMost(editable.length))
    }

    private fun clearParagraphFormats(editable: Editable, start: Int, end: Int) {
        DailyRichText.removeRelativeSizeSpans(editable, start, end)
        DailyRichText.removeStyleSpans(editable, start, end, Typeface.BOLD)
        DailyRichText.removeAnnotations(editable, start, end) {
            it.matches(Regex("h[123]")) || it.startsWith("size:")
        }
    }

    private fun clearListFormats(editable: Editable, start: Int, end: Int) {
        if (end < start) return
        DailyRichText.removeBulletSpans(editable, start, end)
        DailyRichText.removeOrderedListSpans(editable, start, end)
        DailyRichText.removeTodoSpans(editable, start, end)
        DailyRichText.removeAnnotations(editable, start, end) {
            it == "ul" || it.startsWith("ol:") || it.startsWith("todo:")
        }
    }

    private fun collectParagraphRanges(
        editable: Editable,
        selectionStart: Int,
        selectionEnd: Int
    ): List<Pair<Int, Int>> {
        val ranges = mutableListOf<Pair<Int, Int>>()
        var cursor = selectionStart
        while (cursor < selectionEnd) {
            val lineStart = findLineStart(editable, cursor)
            val lineEnd = findLineEndExclusive(editable, cursor)
            if (ranges.lastOrNull() != lineStart to lineEnd) {
                ranges.add(lineStart to lineEnd)
            }
            if (lineEnd <= cursor) break
            cursor = lineEnd
        }
        if (ranges.isEmpty()) {
            val lineStart = findLineStart(editable, selectionStart)
            val lineEnd = findLineEndExclusive(editable, selectionStart)
            ranges.add(lineStart to lineEnd)
        }
        return ranges
    }

    private fun normalizeEmptyBlockParagraphs(editable: Editable, preserveCursorLine: Boolean) {
        val annotations = editable.getSpans(0, editable.length, Annotation::class.java)
            .filter {
                it.key == DailyRichText.ANNOTATION_KEY &&
                    (
                        it.value == "ul" ||
                            it.value.startsWith("ol:") ||
                            it.value.startsWith("todo:") ||
                            it.value == "blockquote"
                        )
            }
            .sortedByDescending { editable.getSpanStart(it) }

        annotations.forEach { annotation ->
            val start = editable.getSpanStart(annotation)
            val end = editable.getSpanEnd(annotation)
            if (start < 0 || end < start) return@forEach
            val lineText = getLineContent(editable, start, end)
            if (lineText.isNotBlank()) return@forEach
            if (editable.subSequence(start, end.coerceAtMost(editable.length)).contains(DailyRichText.EMPTY_BLOCK_MARKER)) {
                return@forEach
            }
            // Empty block lines need a real paragraph terminator so paragraph spans render immediately.
            ensureParagraphTerminator(editable, start)
        }
    }

    private fun findLineStart(text: CharSequence, index: Int): Int {
        val safeIndex = index.coerceIn(0, text.length)
        val previousBreak = text.toString().lastIndexOf('\n', (safeIndex - 1).coerceAtLeast(0))
        return if (previousBreak == -1) 0 else previousBreak + 1
    }

    private fun findLineEndExclusive(text: CharSequence, index: Int): Int {
        val safeIndex = index.coerceIn(0, text.length)
        val nextBreak = text.toString().indexOf('\n', safeIndex)
        return if (nextBreak == -1) text.length else nextBreak + 1
    }

    private fun getLineContent(editable: Editable, lineStart: Int, lineEndExclusive: Int): String {
        val safeStart = lineStart.coerceIn(0, editable.length)
        val safeEnd = lineEndExclusive.coerceIn(safeStart, editable.length)
        val raw = editable.subSequence(safeStart, safeEnd).toString()
        return raw
            .removeSuffix("\n")
            .replace(DailyRichText.EMPTY_BLOCK_MARKER, "")
            .replace(DailyRichText.PLAIN_EXIT_MARKER, "")
    }

    private fun getRichAnnotations(editable: Editable, position: Int): List<Annotation> {
        val lineStart = findLineStart(editable, position.coerceAtLeast(0))
        val lineEnd = findLineEndExclusive(editable, position.coerceAtLeast(0))
        return editable.getSpans(lineStart, lineEnd, Annotation::class.java)
            .filter { annotation ->
                annotation.key == DailyRichText.ANNOTATION_KEY &&
                    editable.getSpanStart(annotation) < lineEnd &&
                    editable.getSpanEnd(annotation) > lineStart
            }
    }

    private fun getTodoState(editable: Editable, position: Int): Boolean? {
        return getRichAnnotations(editable, position)
            .firstOrNull { it.value.startsWith("todo:") }
            ?.value
            ?.removePrefix("todo:")
            ?.toBooleanStrictOrNull()
    }

    private fun isTodoLine(editable: Editable, position: Int): Boolean = getTodoState(editable, position) != null

    private fun isBulletLine(editable: Editable, position: Int): Boolean {
        return getRichAnnotations(editable, position).any { it.value == "ul" }
    }

    private fun isOrderedLine(editable: Editable, position: Int): Boolean {
        return getRichAnnotations(editable, position).any { it.value.startsWith("ol:") }
    }

    private fun getOrderedIndex(editable: Editable, position: Int): Int? {
        return getRichAnnotations(editable, position)
            .firstOrNull { it.value.startsWith("ol:") }
            ?.value
            ?.removePrefix("ol:")
            ?.toIntOrNull()
    }

    private inline fun transformSelectedParagraphs(
        block: (Editable, List<Pair<Int, Int>>) -> Unit
    ) {
        withParagraphSelection(createEmptyParagraph = true) { editable, start, end ->
            val ranges = collectParagraphRanges(editable, start, end)
            if (ranges.isEmpty()) return@withParagraphSelection
            val anchoredRanges = mutableListOf<Pair<Int, Int>>()
            var delta = 0
            ranges.forEach { (rangeStart, rangeEnd) ->
                val adjustedStart = (rangeStart + delta).coerceAtLeast(0)
                val adjustedEnd = (rangeEnd + delta).coerceAtLeast(adjustedStart)
                val anchoredRange = ensureParagraphAnchor(editable, adjustedStart, adjustedEnd)
                delta += anchoredRange.second - adjustedEnd
                anchoredRanges.add(anchoredRange)
            }
            block(editable, anchoredRanges)
        }
    }

    private fun handleRichEnter(editable: Editable, newlineIndex: Int) {
        val previousLineStart = findInsertedNewlineSourceLineStart(editable, newlineIndex)
        val previousLineEnd = newlineIndex.coerceIn(previousLineStart, editable.length)
        val previousLine = getLineContent(editable, previousLineStart, previousLineEnd)

        when {
            isBulletLine(editable, previousLineStart) -> {
                continueListLine(editable, newlineIndex, previousLineStart, previousLine) { start, end ->
                    DailyRichText.applyBulletListSpans(editable, start, end)
                }
            }
            isTodoLine(editable, previousLineStart) -> {
                continueTodoLine(
                    editable,
                    newlineIndex,
                    previousLineStart,
                    previousLine,
                    getTodoState(editable, previousLineStart) ?: false
                )
            }
            isOrderedLine(editable, previousLineStart) -> {
                continueOrderedLine(editable, newlineIndex, previousLineStart, previousLine)
            }
            isQuoteLine(previousLineStart) -> {
                continueQuote(editable, newlineIndex, previousLineStart, previousLine)
            }
            isHeadingLine(previousLineStart) -> {
                clearHeadingFromNewParagraph(editable, newlineIndex)
            }
        }
    }

    private fun findInsertedNewlineSourceLineStart(text: CharSequence, newlineIndex: Int): Int {
        if (newlineIndex <= 0) return 0
        val searchIndex = (newlineIndex - 1).coerceAtLeast(0)
        val previousBreak = text.toString().lastIndexOf('\n', searchIndex)
        return if (previousBreak == -1) 0 else previousBreak + 1
    }

    private fun continueListLine(
        editable: Editable,
        newlineIndex: Int,
        lineStart: Int,
        previousLine: String,
        applyToNewLine: (Int, Int) -> Unit
    ) {
        val content = previousLine.trim()
        isHandlingRichEnter = true
        if (content.isEmpty()) {
            exitCurrentParagraphBlock(editable, lineStart, newlineIndex) { start, end ->
                clearListFormats(editable, start, end)
            }
        } else {
            val newLineStart = (newlineIndex + 1).coerceAtMost(editable.length)
            val newLineEnd = ensureParagraphTerminator(editable, newLineStart)
            applyToNewLine(newLineStart, maxOf(newLineStart, newLineEnd))
            setSelection(newLineStart.coerceAtMost(editable.length))
        }
        isHandlingRichEnter = false
    }

    private fun continueOrderedLine(
        editable: Editable,
        newlineIndex: Int,
        lineStart: Int,
        previousLine: String
    ) {
        val index = getOrderedIndex(editable, lineStart) ?: return
        val content = previousLine.trim()
        isHandlingRichEnter = true
        if (content.isEmpty()) {
            exitCurrentParagraphBlock(editable, lineStart, newlineIndex) { start, end ->
                clearListFormats(editable, start, end)
            }
        } else {
            val newLineStart = (newlineIndex + 1).coerceAtMost(editable.length)
            val newLineEnd = ensureParagraphTerminator(editable, newLineStart)
            DailyRichText.applyOrderedListSpans(editable, newLineStart, maxOf(newLineStart, newLineEnd), index + 1)
            setSelection(newLineStart.coerceAtMost(editable.length))
        }
        isHandlingRichEnter = false
    }

    private fun continueTodoLine(
        editable: Editable,
        newlineIndex: Int,
        lineStart: Int,
        previousLine: String,
        checked: Boolean
    ) {
        continueListLine(editable, newlineIndex, lineStart, previousLine) { start, end ->
            DailyRichText.applyTodoSpans(editable, start, end, checked)
        }
    }

    private fun continueQuote(
        editable: Editable,
        newlineIndex: Int,
        lineStart: Int,
        previousLine: String
    ) {
        val currentParagraphEnd = editable.toString().indexOf('\n', newlineIndex + 1)
            .let { if (it == -1) editable.length else it + 1 }
        isHandlingRichEnter = true
        if (previousLine.isBlank()) {
            exitCurrentParagraphBlock(editable, lineStart, newlineIndex) { start, end ->
                DailyRichText.removeQuoteSpans(editable, start, end)
                DailyRichText.removeAnnotations(editable, start, end) { it == "blockquote" }
            }
        } else {
            val newLineStart = (newlineIndex + 1).coerceAtMost(editable.length)
            val newLineEnd = ensureParagraphTerminator(editable, newLineStart)
            val quoteBlockStart = findQuoteBlockStart(editable, lineStart)
            val quoteBlockEnd = maxOf(newLineStart, maxOf(currentParagraphEnd, newLineEnd))
            DailyRichText.removeQuoteSpans(editable, quoteBlockStart, quoteBlockEnd)
            DailyRichText.removeAnnotations(editable, quoteBlockStart, quoteBlockEnd) { it == "blockquote" }
            DailyRichText.applyQuoteSpans(editable, quoteBlockStart, quoteBlockEnd)
            setSelection(newLineStart.coerceAtMost(editable.length))
        }
        isHandlingRichEnter = false
    }

    private inline fun exitCurrentParagraphBlock(
        editable: Editable,
        lineStart: Int,
        newlineIndex: Int,
        clearBlock: (Int, Int) -> Unit
    ) {
        val paragraphEnd = findLineEndExclusive(editable, newlineIndex)
        clearBlock(lineStart, paragraphEnd)
        if (paragraphEnd > lineStart) {
            editable.delete(lineStart, paragraphEnd)
        }
        setSelection(lineStart.coerceAtMost(editable.length))
    }

    private fun ensureParagraphTerminator(editable: Editable, lineStart: Int): Int {
        val safeStart = lineStart.coerceIn(0, editable.length)
        if (safeStart == editable.length) {
            editable.insert(safeStart, "\n")
            return safeStart + 1
        }
        val lineEnd = findLineEndExclusive(editable, safeStart)
        if (lineEnd <= safeStart || editable[lineEnd - 1] != '\n') {
            editable.insert(lineEnd, "\n")
            return lineEnd + 1
        }
        return lineEnd
    }

    private fun ensureParagraphAnchor(editable: Editable, start: Int, end: Int): Pair<Int, Int> {
        val safeStart = start.coerceIn(0, editable.length)
        val safeEnd = end.coerceIn(safeStart, editable.length)
        if (safeEnd > safeStart) {
            return safeStart to safeEnd
        }
        editable.insert(safeStart, DailyRichText.EMPTY_BLOCK_MARKER)
        return safeStart to (safeStart + DailyRichText.EMPTY_BLOCK_MARKER.length)
    }

    private fun cleanupLegacyRichMarkers(editable: Editable) {
        var index = editable.length - 1
        while (index >= 0) {
            val current = editable[index].toString()
            val shouldDelete = when (current) {
                DailyRichText.PLAIN_EXIT_MARKER -> true
                DailyRichText.EMPTY_BLOCK_MARKER -> {
                    val lineStart = findLineStart(editable, index)
                    val lineEnd = findLineEndExclusive(editable, index)
                    val visibleText = getLineContent(editable, lineStart, lineEnd)
                    val hasBlockAnnotation = editable.getSpans(lineStart, lineEnd, Annotation::class.java)
                        .any { annotation ->
                            annotation.key == DailyRichText.ANNOTATION_KEY &&
                                (
                                    annotation.value == "ul" ||
                                        annotation.value.startsWith("ol:") ||
                                        annotation.value.startsWith("todo:") ||
                                        annotation.value == "blockquote" ||
                                        annotation.value.startsWith("align:") ||
                                        annotation.value.matches(Regex("h[123]"))
                                    )
                        }
                    visibleText.isNotBlank() || !hasBlockAnnotation
                }
                else -> false
            }
            if (shouldDelete) {
                editable.delete(index, index + 1)
                if (selectionStart > index) {
                    setSelection((selectionStart - 1).coerceAtLeast(0))
                }
            }
            index--
        }
    }

    private fun findQuoteBlockStart(editable: Editable, position: Int): Int {
        return editable.getSpans(position.coerceIn(0, editable.length), position.coerceIn(0, editable.length), Annotation::class.java)
            .filter { it.key == DailyRichText.ANNOTATION_KEY && it.value == "blockquote" }
            .map { editable.getSpanStart(it) }
            .filter { it >= 0 }
            .minOrNull()
            ?: position
    }

    private fun clearHeadingFromNewParagraph(editable: Editable, newlineIndex: Int) {
        val currentParagraphEnd = editable.toString().indexOf('\n', newlineIndex + 1)
            .let { if (it == -1) editable.length else it + 1 }
        isHandlingRichEnter = true
        clearParagraphFormats(editable, newlineIndex + 1, currentParagraphEnd)
        setSelection((newlineIndex + 1).coerceAtMost(editable.length))
        isHandlingRichEnter = false
    }

    private fun isQuoteLine(position: Int): Boolean {
        val editable = text ?: return false
        return getRichAnnotations(editable, position).any { it.value == "blockquote" }
    }

    private fun isHeadingLine(position: Int): Boolean {
        val editable = text ?: return false
        return getRichAnnotations(editable, position).any { it.value.matches(Regex("h[123]")) }
    }

    private fun handleTodoCheckboxTap(event: MotionEvent): Boolean {
        val editable = text ?: return false
        val layout = layout ?: return false
        val x = event.x - totalPaddingLeft + scrollX
        val y = event.y - totalPaddingTop + scrollY
        val line = layout.getLineForVertical(y.toInt())
        val lineStart = layout.getLineStart(line)
        val lineEnd = layout.getLineEnd(line)
        val todoState = getTodoState(editable, lineStart) ?: return false
        val leadingMargin = editable.getSpans(lineStart, lineEnd, TodoSpan::class.java)
            .firstOrNull()
            ?.getLeadingMargin(true)
            ?: return false
        if (x > leadingMargin) return false

        clearListFormats(editable, lineStart, lineEnd)
        DailyRichText.applyTodoSpans(editable, lineStart, lineEnd, !todoState)
        setSelection(lineStart.coerceAtMost(editable.length))
        return true
    }

    /**
     * 获取插入的音频路径集合
     */
    fun getInsertedAudios(): List<String> = audioPathList.filter { text?.contains(it) == true }

    /**
     * 获取插入的视频路径集合
     */
    fun getInsertedVideos(): List<String> = videoPathList.filter { text?.contains(it) == true }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return if (id == android.R.id.copy) {
            CopyUtil.copyTextToClipboard(
                context,
                TextUtil.replaceTag(text?.substring(selectionStart, selectionEnd) ?: "", "")
            )
            clearFocus()
            true
        } else super.onTextContextMenuItem(id)
    }

}