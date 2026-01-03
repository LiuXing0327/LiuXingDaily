/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.extension

import android.app.Activity
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.liuxing.daily.util.IntentUtil

/**
 * 绑定 Preference 点击跳转 Activity
 */
inline fun <reified T : Activity> PreferenceFragmentCompat.bindPreferenceToActivity(key: String) {
    findPreference<Preference>(key)?.setOnPreferenceClickListener {
        IntentUtil.startActivity(requireContext(), T::class.java)
        true
    }
}

/**
 * 绑定 Preference 点击执行 [action]
 */
fun PreferenceFragmentCompat.bindPreferenceAction(
    key: String, action: () -> Unit
) {
    findPreference<Preference>(key)?.setOnPreferenceClickListener {
        action()
        true
    }
}

fun PreferenceFragmentCompat.bindPreferenceToNavigation(
    key: String,
    @IdRes destinationId: Int,
    args: Bundle? = null
) {
    findPreference<Preference>(key)?.setOnPreferenceClickListener {
        findNavController().navigate(destinationId, args)
        true
    }
}
