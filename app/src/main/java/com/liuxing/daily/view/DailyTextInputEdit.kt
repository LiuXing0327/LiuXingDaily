package com.liuxing.daily.view

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import com.google.android.material.textfield.TextInputEditText
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.ImageUtil.createImageThumbnail
import kotlin.math.abs

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
        this.imagePathList = imagePaths.toMutableSet()
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
        text = editableContent
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
        this.imagePathList.clear()
        val currentLength = editable.length
        imagePathList.forEach { path ->
            path?.let {
                if (!this.imagePathList.contains(it) && FileUtil().checkFileExists(it)) {
                    this.imagePathList.add(it)
                    if (!editable.contains(createImageSpannable(it))) {
                        if (selectionStart > 0 && editable[selectionStart - 1] != '\n') {
                            editable.insert(selectionStart, "\n\n")
                        }
                        val sequence = createImageSpannable(it)
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
                        isImageInserted = true
                        setSelection(text.toString().length)
                        imageInsertionListener?.onImageInserted()
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
                val removePathList = imagePathList.filter { deletedString.contains(it) }
                removePathList.forEach { imagePath ->
                    imagePathList.remove(imagePath)
                    imageDeletionListener?.onImageDeleted(imagePath)
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
}
