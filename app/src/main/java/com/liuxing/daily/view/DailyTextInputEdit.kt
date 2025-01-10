package com.liuxing.daily.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.textfield.TextInputEditText
import com.liuxing.daily.R
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.ImageUtil.createImageThumbnail
import com.liuxing.daily.util.VideoUtil.createVideoThumbnail

/**
 * Author：流星
 * DateTime：2024/10/29 8:14
 * Description：写日记编辑
 */
class DailyTextInputEdit : TextInputEditText {

    private val placeholder = "&"
    private val context: Context
    private var isImageInserted = false
    private var imagePathList: MutableSet<String> = mutableSetOf()
    private var formattedText: String = ""
    private var imageInsertionListener: ImageInsertionListener? = null
    private var imageDeletionListener: ImageDeletionListener? = null
    private val imageMap: MutableMap<String, String> = mutableMapOf()
    private var oldImageList: List<String> = listOf()
    private var oldVideoList: List<String> = listOf()
    private var audioInsertionListener: AudioInsertionListener? = null
    private var videoInsertionListener: VideoInsertionListener? = null
    private var audioPathList: MutableSet<String> = mutableSetOf()
    private var videoPathList: MutableSet<String> = mutableSetOf()
    private var videoDeletionListener: VideoDeletionListener? = null
    private var audioDeletionListener: AudioDeletionListener? = null

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

        this.imagePathList = imagePaths.toMutableSet()
        this.videoPathList = videoPaths.toMutableSet()
        this.audioPathList = audioPaths.toMutableSet()

        val editableContent = Editable.Factory.getInstance().newEditable(content)
        imagePaths.forEach { imagePath ->
            val imgTag = "<img src=\"$imagePath\"/>"
            val startIndex = editableContent.indexOf(imgTag)
            if (startIndex != -1) {
                editableContent.replace(
                    startIndex, startIndex + imgTag.length, createImageSpannable(imagePath)
                )
            }
        }

        videoPaths.forEach { videoPath ->
            val videoTag = "<video src=\"$videoPath\"/>"
            val startIndex = editableContent.indexOf(videoTag)
            if (startIndex != -1) {
                editableContent.replace(
                    startIndex, startIndex + videoTag.length, createVideoSpannable(videoPath)
                )
            }
        }

        audioPaths.forEach { audioPath ->
            val audioTag = "<audio src=\"$audioPath\"/>"
            val startIndex = editableContent.indexOf(audioTag)
            if (startIndex != -1) {
                editableContent.replace(
                    startIndex,
                    startIndex + audioTag.length,
                    createAudioSpannable(audioPath)
                )
            }
        }

        text = editableContent
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
        var isImageAddedThisTime = false

        imagePathList.forEach { path ->
            path?.let {
                if (!this.imagePathList.contains(it) && FileUtil().checkFileExists(it)) {
                    this.imagePathList.add(it)

                    // 检查文本中是否已经插入该图片占位符，避免重复插入
                    if (!editable.contains(createImageSpannable(it))) {
                        if (!isImageAddedThisTime) {
                            if (selectionStart > 0 && editable[selectionStart - 1] != '\n') {
                                editable.insert(selectionStart, "\n\n")
                            }
                            val sequence = createImageSpannable(it)
                            editable.insert(selectionStart, sequence)
                            val newLength = editable.length
                            if (selectionStart + sequence.length <= newLength) {
                                editable.insert(selectionStart + sequence.length, "\n")
                            } else {
                                editable.append("\n")
                            }

                            isImageInserted = true
                            isImageAddedThisTime = true
                            setSelection(editable.length)
                            imageInsertionListener?.onImageInserted()
                        }
                    }
                }
            }
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
            isImageInserted = false
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
            if (isImageInserted) return
            invalidate()
            requestLayout()
            updateFormattedText(s)
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
        val drawable = BitmapDrawable(resources, bitmap).apply {
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
        val editable = text ?: return 0
        val spannableText = SpannableString(editable)
        val imageSpans = spannableText.getSpans(0, spannableText.length, ImageSpan::class.java)
        var totalLength = spannableText.length
        for (imageSpan in imageSpans) {
            val start = spannableText.getSpanStart(imageSpan)
            val end = spannableText.getSpanEnd(imageSpan)
            totalLength -= (end - start)
        }

        return maxOf(totalLength, 0)
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
                    if (!editable.contains(createAudioSpannable(it))) {
                        // 确保格式一致性
                        if (selectionStart > 0 && editable[selectionStart - 1] != '\n') {
                            editable.insert(selectionStart, "\n\n")
                        }
                        val sequence = createAudioSpannable(it)
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
                        setSelection(text.toString().length)
                        audioInsertionListener?.onAudioInserted()
                    }
                }
            }
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
                    if (!editable.contains(createVideoSpannable(it))) {
                        if (selectionStart > 0 && editable[selectionStart - 1] != '\n') {
                            editable.insert(selectionStart, "\n\n")
                        }
                        val sequence = createVideoSpannable(it)
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
                        setSelection(text.toString().length)
                        videoInsertionListener?.onVideoInserted()
                    }
                }
            }
        }
    }

    /**
     * 创建音频占位符
     *
     * @param audioPath 音频路径
     * @return CharSequence 返回带有音频的SpannableString
     */
    private fun createAudioSpannable(audioPath: String): CharSequence {
        val audioTag = "<audio src=\"$audioPath\"/>"
        val iconDrawable = ContextCompat.getDrawable(context, R.drawable.baseline_audiotrack_24)
        val maxWidth = resources.displayMetrics.widthPixels - 40
        val borderHeightDp = 40
        val borderHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            borderHeightDp.toFloat(),
            resources.displayMetrics
        ).toInt()
        iconDrawable?.let {
            val iconHeightDp = 24
            val iconHeightPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                iconHeightDp.toFloat(),
                resources.displayMetrics
            ).toInt()
            val originalWidth = it.intrinsicWidth
            val originalHeight = it.intrinsicHeight
            val scaleFactor = iconHeightPx.toFloat() / originalHeight.toFloat()
            val iconWidthPx = (originalWidth * scaleFactor).toInt()
            it.setBounds(0, 0, iconWidthPx, iconHeightPx)
            val bitmapWithBorder =
                Bitmap.createBitmap(maxWidth, borderHeightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmapWithBorder)
            val borderPaint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRect(0f, 0f, maxWidth.toFloat(), borderHeightPx.toFloat(), borderPaint)
            val centerX = (maxWidth - iconWidthPx) / 2f
            val centerY = (borderHeightPx - iconHeightPx) / 2f
            canvas.drawBitmap(it.toBitmap(), centerX, centerY, null)
            val drawableWithBorder = BitmapDrawable(resources, bitmapWithBorder).apply {
                setBounds(0, 0, maxWidth, borderHeightPx)  // 设置最终的绘制范围
            }
            val ss = SpannableString(audioTag)
            val imageSpan = ImageSpan(drawableWithBorder, ImageSpan.ALIGN_BASELINE)
            ss.setSpan(imageSpan, 0, ss.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            return ss
        }

        return SpannableString("")
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
        val createBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(createBitmap)
        val matrix = Matrix()
        matrix.setScale(scaleFactor, scaleFactor)
        canvas.drawBitmap(bitmap, matrix, null)
        val playDrawable = ContextCompat.getDrawable(context, R.drawable.baseline_play_arrow_24)
        val playBitmap = playDrawable?.let {
            val width = it.intrinsicWidth
            val height = it.intrinsicHeight
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            it.setBounds(0, 0, width, height)
            it.draw(canvas)
            bitmap
        }
        playBitmap?.let {
            val centerX = (newWidth - it.width) / 2f
            val centerY = (newHeight - it.height) / 2f
            canvas.drawBitmap(it, centerX, centerY, null)
        }
        val drawable = BitmapDrawable(resources, createBitmap).apply {
            setBounds(0, 0, newWidth, newHeight)
        }
        val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
        ss.setSpan(imageSpan, 0, ss.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return ss
    }


    /**
     * 获取插入的音频路径集合
     */
    fun getInsertedAudios(): List<String> = audioPathList.filter { text?.contains(it) == true }

    /**
     * 获取插入的视频路径集合
     */
    fun getInsertedVideos(): List<String> = videoPathList.filter { text?.contains(it) == true }

}
