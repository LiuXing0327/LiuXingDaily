package com.liuxing.daily.view

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.ui.image.LookDailyImageActivity
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.ImageUtil.createImageThumbnail

/**
 * Author：流星
 * DateTime：2024/11/2 12:55
 * Description：看日记文本
 */
class DailyTextView : MaterialTextView {

    private val context: Context
    private var imagePathList: MutableSet<String> = mutableSetOf()
    private var dailyUuid: String = ""

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

    /**
     * 设置日记Uuid
     *
     * @param dailyUuid 日记Uuid
     */
    fun setDailyUuid(dailyUuid: String) {
        this.dailyUuid = dailyUuid
    }

    /**
     * 设置图片路径集合
     *
     * @param text 日记内容
     * @param newImagePathList 新的图片路径集合
     */
    fun setImagePathList(text: String, newImagePathList: List<String>) {
        this.imagePathList.clear()
        this.imagePathList.addAll(newImagePathList)
        setFormattedText(text, newImagePathList)
    }

    /**
     * 设置格式化文本
     *
     * @param text 日记内容
     * @param newImagePathList 新的图片路径集合
     */
    private fun setFormattedText(
        text: String,
        newImagePathList: List<String>
    ) {
        val spannableString = SpannableStringBuilder()
        var currentIndex = 0
        newImagePathList.forEachIndexed { imageIndex, imagePath ->
            val imgTag = "<img src=\"$imagePath\"/>"
            val imgTagIndex = text.indexOf(imgTag, currentIndex)

            if (imgTagIndex != -1) {
                if (imgTagIndex > currentIndex) {
                    spannableString.append(text.substring(currentIndex, imgTagIndex))
                }
                val imageSpannable = createImageSpannable(imagePath, imageIndex)
                spannableString.append(imageSpannable)
                currentIndex = imgTagIndex + imgTag.length
            }
        }
        if (currentIndex < text.length) {
            spannableString.append(text.substring(currentIndex))
        }
        setText(spannableString)
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
        val drawable = BitmapDrawable(resources, bitmap).apply {
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
     * 检查图片是否存在
     *
     * @param text 日记内容
     * @param imagePathList 图片路径集合
     * @return 结果
     */
    fun checkImage(text: String, imagePathList: List<String>): String {
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
}