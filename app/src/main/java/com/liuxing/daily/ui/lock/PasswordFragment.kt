package com.liuxing.daily.ui.lock

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.liuxing.daily.R
import com.liuxing.daily.databinding.FragmentPasswordBinding
import com.liuxing.daily.ui.main.MainActivity
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.SoftHideKeyBoardUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PasswordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PasswordFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val binding by lazy {
        FragmentPasswordBinding.inflate(layoutInflater)
    }

    private var sharedPreferences: SharedPreferences? = null
    private val appPassword by lazy {
        UnlockActivity.appPassword
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PasswordFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = PasswordFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        SoftHideKeyBoardUtil(requireActivity())
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        initSharePreference()
        getAppPassword()
        // changeButtonState()
        unlock()
    }

    /**
     * 初始化 [sharedPreferences]
     */
    private fun initSharePreference() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    /**
     * 获取应用密码
     */
    private fun getAppPassword() {

    }

    /**
     * 通过监听文本更改，设置按钮是否启用
     */
    private fun changeButtonState() {
        binding.passwordLayout.inputPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // binding.btnUnlock.isEnabled = HashUtil.hashSHA256(s.toString()) == appPassword
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    /**
     * 解除锁定
     *
     * 如果密码不正确，则显示错误信息，一秒后消失；
     * 密码正确，跳转到 [MainActivity]并关闭当前 Activity。
     */
    private fun unlock() {
        binding.btnUnlock.setOnClickListener {
            // 输入的密码
            val inputPassword = binding.passwordLayout.inputPassword.text.toString()
            // 输入的密码与正确的密码对比
            val right = HashUtil.hashSHA256(inputPassword) == appPassword

            if (!right) {
                binding.passwordLayout.inputPasswordLayout.error =
                    getString(R.string.wrong_password)

                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    binding.passwordLayout.inputPasswordLayout.error = null
                }
            } else {
                IntentUtil.startActivity(
                    requireContext(), MainActivity::class.java, mapOf("lock" to false)
                )
                requireActivity().finish()
            }
        }
    }
}