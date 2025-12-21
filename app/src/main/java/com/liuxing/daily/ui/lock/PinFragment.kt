package com.liuxing.daily.ui.lock

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.loadingindicator.LoadingIndicator
import com.liuxing.daily.R
import com.liuxing.daily.databinding.FragmentPinBinding
import com.liuxing.daily.extension.setVisibility
import com.liuxing.daily.ui.main.MainActivity
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.IntentUtil


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PinFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PinFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val binding by lazy {
        FragmentPinBinding.inflate(layoutInflater)
    }

    /**
     * 数字按钮 1-3
     */
    private val btnNumber1 by lazy {
        binding.numberButtonContainer1.buttonParent1.buttonContainer.btnNumber
    }
    private val btnNumber2 by lazy {
        binding.numberButtonContainer1.buttonParent2.buttonContainer.btnNumber
    }
    private val btnNumber3 by lazy {
        binding.numberButtonContainer1.buttonParent3.buttonContainer.btnNumber
    }

    /**
     * 数字按钮 4-6
     */
    private val btnNumber4 by lazy {
        binding.numberButtonContainer2.buttonParent1.buttonContainer.btnNumber
    }
    private val btnNumber5 by lazy {
        binding.numberButtonContainer2.buttonParent2.buttonContainer.btnNumber
    }
    private val btnNumber6 by lazy {
        binding.numberButtonContainer2.buttonParent3.buttonContainer.btnNumber
    }

    /**
     * 数字按钮 7-9
     */
    private val btnNumber7 by lazy {
        binding.numberButtonContainer3.buttonParent1.buttonContainer.btnNumber
    }
    private val btnNumber8 by lazy {
        binding.numberButtonContainer3.buttonParent2.buttonContainer.btnNumber
    }
    private val btnNumber9 by lazy {
        binding.numberButtonContainer3.buttonParent3.buttonContainer.btnNumber
    }

    /**
     * 撤销、0和确定按钮
     */
    private val btnBackspace by lazy {
        binding.numberButtonContainer4.buttonParent1.iconButtonContainer.btnIcon
    }
    private val btnNumber0 by lazy {
        binding.numberButtonContainer4.buttonParent2.buttonContainer.btnNumber
    }
    private val btnConfirm by lazy {
        binding.numberButtonContainer4.buttonParent3.iconButtonContainer.btnIcon
    }

    private val inputPasswordLayout by lazy {
        binding.inputPasswordContainer.inputPasswordLayout
    }
    private val inputPassword by lazy {
        binding.inputPasswordContainer.inputPassword
    }

    /**
     * 当前输入的 Pin 码。
     */
    private var pinCode = ""

    /**
     * 应用的 PIN 码。
     */
    private val appPIN by lazy {
        UnlockActivity.appPassword
    }

    /**
     * 指示器容器
     */
    private val loadingIndicatorContainer by lazy {
        binding.loadingIndicatorContainer
    }

    /**
     * Pin 码发生变化时触发的回调.
     */
    private lateinit var pinCodeListener: PinCodeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PinFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PinFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    /**
     * 初始化数据.
     */
    private fun initData() {
        initButton()
        setInputPasswordAttr()
        refreshPinCode()
    }

    /**
     * 初始化数字按钮和图标按钮。
     */
    private fun initButton() {
        initButtonTextAndOnClick()
        initButtonIconAndOnClick()
    }

    /**
     * 初始化数字按钮文本和点击事件
     */
    private fun initButtonTextAndOnClick() {
        val numberButtons = listOf(
            btnNumber0,
            btnNumber1,
            btnNumber2,
            btnNumber3,
            btnNumber4,
            btnNumber5,
            btnNumber6,
            btnNumber7,
            btnNumber8,
            btnNumber9
        )

        var number = 0
        for (button in numberButtons) {
            button.text = number.toString()
            number++

            button.setOnClickListener {
                pinCode += numberButtons.indexOf(button)
                pinCodeListener.onPinCodeChanged(pinCode)
            }
        }
    }

    /**
     * 初始化图标按钮和点击事件
     */
    private fun initButtonIconAndOnClick() {
        binding.numberButtonContainer4.buttonParent1.buttonContainer.root.setVisibility(false)
        binding.numberButtonContainer4.buttonParent1.iconButtonContainer.root.setVisibility(true)
        btnBackspace.icon =
            ContextCompat.getDrawable(requireContext(), R.drawable.outline_backspace_24)
        btnBackspace.backgroundTintList = AppCompatResources.getColorStateList(
            requireContext(),
            R.color.lock_icon_back_bg_tint_color
        )

        btnBackspace.setOnClickListener {
            onDeleteClicked()
        }
        btnBackspace.setOnLongClickListener {
            if (pinCode.isNotEmpty()) {
                onClearLongClicked()
                return@setOnLongClickListener true
            }
            return@setOnLongClickListener false
        }

        binding.numberButtonContainer4.buttonParent3.buttonContainer.root.setVisibility(false)
        binding.numberButtonContainer4.buttonParent3.iconButtonContainer.root.setVisibility(true)
        btnConfirm.icon =
            ContextCompat.getDrawable(requireContext(), R.drawable.outline_keyboard_tab_24)

        btnConfirm.setOnClickListener {
            val right = HashUtil.hashSHA256(
                binding.inputPasswordContainer.inputPassword.text.toString()
            ) == appPIN

            if (right) {
                IntentUtil.startActivity(
                    requireContext(),
                    MainActivity::class.java,
                    mapOf("lock" to false)
                )

                requireActivity().finish()
            } else {
                val allButtons = listOf(
                    btnNumber1,
                    btnNumber2,
                    btnNumber3,
                    btnNumber4,
                    btnNumber5,
                    btnNumber6,
                    btnNumber7,
                    btnNumber8,
                    btnNumber9,
                    btnBackspace,
                    btnNumber0,
                    btnConfirm
                )

                animateButtonsSequentially(allButtons)

                pinCode = ""
                pinCodeListener.onPinCodeChanged(pinCode)
            }
        }
    }

    /**
     * 按顺序播放按钮动画
     *
     * @param buttons [MaterialButton] 的列表。
     * @param index 从列表中哪个索引开始播放动画。
     */
    fun animateButtonsSequentially(
        buttons: List<MaterialButton>,
        index: Int = 0
    ) {
        if (index >= buttons.size) return

        val button = buttons[index]

        val animator = ValueAnimator.ofFloat(1f, 0.8f).apply {
            duration = 20
            repeatCount = 1
            repeatMode = ValueAnimator.REVERSE

            addUpdateListener {
                val value = it.animatedValue as Float
                button.scaleX = value
                button.scaleY = value
                button.alpha = 2f - value
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animateButtonsSequentially(buttons, index + 1)
                }
            })
        }

        animator.start()
    }

    /**
     * 点击删除最后一个数字。
     */
    private fun onDeleteClicked() {
        pinCode = pinCode.dropLast(1)
        pinCodeListener.onPinCodeChanged(pinCode)
    }

    /**
     * 长按清空所有数字。
     */
    private fun onClearLongClicked() {
        pinCode = pinCode.dropLast(pinCode.length)
        pinCodeListener.onPinCodeChanged(pinCode)
    }

    /**
     * 设置输入密码框属性
     */
    private fun setInputPasswordAttr() {
        val dp0 = requireContext().resources.getDimensionPixelSize(R.dimen.dp_0)

        inputPassword.apply {
            isFocusable = true // 默认获取焦点
            isCursorVisible = false // 光标不可见
            maxLines = 1 // 限制为单行
            isSingleLine = true // 限制为单行
            ellipsize = null // 不用任何省略字符
            isHorizontalScrollBarEnabled = true // 启用水平滚动
            isLongClickable = false // 关闭长按事件
            setTextIsSelectable(false) // 文本不可选择
            setOnTouchListener(null) // 禁用触摸事件
            setPadding(
                dp0,
                paddingTop,
                dp0,
                paddingBottom
            ) // left/right padding 设置为 0dp，top/bottom 保持不变
            inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD // 限制输入为数字
            showSoftInputOnFocus = false // 获取焦点时，软键盘不显示
            textAlignment = View.TEXT_ALIGNMENT_CENTER // 文本居中
            // 变换方法改为 LoadingDotPasswordTransformationMethod
            transformationMethod = getLoadingDotPasswordTransformationMethod()
            requestFocus() // 尝试获取焦点
        }

        inputPasswordLayout.hint = "" // 去除已有的提示词
    }

    /**
     * 获取 [LoadingDotPasswordTransformationMethod]
     *
     * @return LoadingDotPasswordTransformationMethod
     */
    private fun getLoadingDotPasswordTransformationMethod(): LoadingDotPasswordTransformationMethod {
        // 自定义 indicatorColor
        val indicatorColor = MaterialColors.getColor(
            inputPassword,
            com.google.android.material.R.attr.colorSecondaryVariant
        )

        // PasswordTransformationMethod 中的 loadingDrawable。
        val loadingIndicator = LoadingIndicator(requireContext()).apply {
            setIndicatorColor(indicatorColor)
            layoutParams = LinearLayout.LayoutParams(0, 0).apply {
                gravity = Gravity.CENTER
            }
        }

        // 如果不在圆点容器中添加 loadingIndicator，
        // 则无法正常启用 loadingIndicatorDrawable 动画，从而导致固定初始 Drawable。
        loadingIndicatorContainer.addView(loadingIndicator)

        // 获取 loadingIndicatorDrawable。
        val loadingIndicatorDrawable = loadingIndicator.drawable

        // pin_dot_filled 做为 dotDrawable。
        val dotDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.pin_dot_filled)!!

        return LoadingDotPasswordTransformationMethod(
            loadingIndicatorDrawable,
            dotDrawable,
            50, 100

        )
    }

    /**
     * 刷新 Pin 码字符串
     */
    private fun refreshPinCode() {
        setPinCodeListener(object : PinCodeListener {
            override fun onPinCodeChanged(pinCodeStr: String) {
                inputPassword.setText(pinCodeStr)
                inputPassword.setSelection(pinCodeStr.length)
            }
        })
    }

    /**
     * 监听“Pin 码”变化回调接口
     *
     * 当 Pin 码发生变化（添加、移除）时，该接口会触发。
     */
    private interface PinCodeListener {

        /**
         * 当 Pin 码发生变化时调用。
         *
         * @param pinCodeStr 更新后的 Pin 码字符串。
         */
        fun onPinCodeChanged(pinCodeStr: String)
    }

    /**
     * 注册一个监听“Pin 码”变化的监听器
     *
     * @param listener 要注册的监听器实例。
     */
    private fun setPinCodeListener(listener: PinCodeListener) {
        pinCodeListener = listener
    }
}