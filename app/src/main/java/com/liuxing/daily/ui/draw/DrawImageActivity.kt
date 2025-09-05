package com.liuxing.daily.ui.draw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityDrawImageBinding
import com.liuxing.daily.listener.UndoRedoListener
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.view.DrawView
import com.liuxing.library.ColorPickerView
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DrawImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDrawImageBinding
    private var selectedColor = Color.BLACK
    private var selectedBackgroundColor = Color.TRANSPARENT
    private val imageName = UUID.randomUUID().toString() + ".jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        binding = ActivityDrawImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
/*        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        initView()
        initData()
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        selectedBackgroundColor = typedValue.data
        binding.drawView.setBackgroundColor(selectedBackgroundColor)

        binding.floatingToolbarButtonUndo.isEnabled = false
        binding.floatingToolbarButtonRedo.isEnabled = false

        binding.drawView.undoRedoListener?.onUndoRedoChanged(
            binding.drawView.canUndo(),
            binding.drawView.canRedo()
        )

        binding.drawView.undoRedoListener = object : UndoRedoListener {
            override fun onUndoRedoChanged(canUndo: Boolean, canRedo: Boolean) {
                binding.floatingToolbarButtonUndo.isEnabled = canUndo
                binding.floatingToolbarButtonRedo.isEnabled = canRedo
            }

        }
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        fullScreenMode()
        changeBrushColor()
        back()
        setEraserMode()
        initEraserColor()
        changeSliderVisibility()
        setStrokeWidth()
        setCanvasBackgroundColor()
        saveDrawingImage()
        undo()
        redo()
    }

    /**
     * 全屏模式
     */
    private fun fullScreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    /**
     * 更改画笔颜色
     */
    private fun changeBrushColor() {
        binding.floatingToolbarButtonColor.setOnClickListener {
            MaterialAlertDialogBuilder(this).apply {
                val colorPickerLayout = layoutInflater.inflate(R.layout.color_picker_layout, null)
                val colorPickerView =
                    colorPickerLayout.findViewById<ColorPickerView>(R.id.color_picker_view)

                colorPickerView.onColorChanged = { color ->
                    selectedColor = color
                }
                setView(colorPickerLayout)
                setNeutralButton(getString(R.string.cancel), null)
                setPositiveButton(getString(R.string.sure)) { _, _ ->
                    binding.drawView.setStrokeColor(selectedColor)
                }
                setOnDismissListener {
                    binding.floatingToolbarButtonColor.isChecked = false
                }
                create()
                show()
            }
        }
    }

    /**
     * 返回
     */
    private fun back() {
        binding.floatingToolbarButtonBack.setOnClickListener {
            finish()
        }
    }

    /**
     * 设置橡皮擦模式
     */
    private fun setEraserMode() {
        binding.floatingToolbarButtonEraser.setOnClickListener {

            binding.drawView.setEraserMode(!binding.drawView.isEraserOnP)
        }
    }

    /**
     * 初始化橡皮擦颜色
     */
    private fun initEraserColor() {
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        selectedBackgroundColor = typedValue.data
        binding.drawView.setEraserStrokeColor(selectedBackgroundColor)
    }

    /**
     * 更改 Slider 可见性
     */
    private fun changeSliderVisibility() {
        binding.floatingToolbarButtonWidth.setOnClickListener {
            binding.floatingToolbarLayout2.visibility =
                if (binding.floatingToolbarLayout2.isVisible) {
                    View.GONE
                } else View.VISIBLE
        }
    }

    /**
     * 设置线宽
     */
    private fun setStrokeWidth() {
        binding.strokeWidthSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                binding.drawView.setStrokeWidth(value)
            }
        }
    }

    /**
     * 设置绘制的背景颜色
     */
    private fun setCanvasBackgroundColor() {
        binding.floatingToolbarButtonPalette.setOnClickListener {
            MaterialAlertDialogBuilder(this).apply {
                val colorPickerLayout = layoutInflater.inflate(R.layout.color_picker_layout, null)
                val colorPickerView =
                    colorPickerLayout.findViewById<ColorPickerView>(R.id.color_picker_view)

                colorPickerView.onColorChanged = { color ->
                    selectedBackgroundColor = color
                }
                setView(colorPickerLayout)
                setNeutralButton(getString(R.string.cancel), null)
                setPositiveButton(getString(R.string.sure)) { _, _ ->
                    binding.drawView.setBackgroundColor(selectedBackgroundColor)
                    binding.drawView.setEraserStrokeColor(selectedBackgroundColor)
                }
                setOnDismissListener {
                    binding.floatingToolbarButtonPalette.isChecked = false
                }
                create()
                show()
            }
        }
    }

    /**
     * 通过按钮点击保存绘制的图片
     */
    private fun saveDrawingImage() {
        binding.floatingToolbarButtonSave.setOnClickListener {
            val width = binding.drawView.width
            val height = binding.drawView.height
            saveImage(width, height, binding.drawView)
            intent.putExtra("draw_image_name", imageName)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    /**
     * 存储绘制的图片
     *
     * @param width 宽度
     * @param height 高度
     * @param drawView DrawView
     */
    private fun saveImage(width: Int, height: Int, drawView: DrawView) {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawView.draw(canvas)
        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            imageName
        )
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
        }
    }

    /**
     * 撤销
     */
    private fun undo() {
        binding.floatingToolbarButtonUndo.setOnClickListener {
            binding.drawView.undo()
            binding.floatingToolbarButtonUndo.isChecked = false
        }
    }

    /**
     * 重做
     */
    private fun redo() {
        binding.floatingToolbarButtonRedo.setOnClickListener {
            binding.drawView.redo()
            binding.floatingToolbarButtonRedo.isChecked = false
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) fullScreenMode()
    }
}