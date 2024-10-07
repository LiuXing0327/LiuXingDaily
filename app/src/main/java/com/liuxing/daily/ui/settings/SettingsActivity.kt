package com.liuxing.daily.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.materialswitch.MaterialSwitch
import com.liuxing.daily.R
import com.liuxing.daily.databinding.SettingsActivityBinding
import com.liuxing.daily.ui.about.AboutActivity
import com.liuxing.daily.ui.updatelog.UpdateLogActivity
import com.liuxing.daily.util.CheckAppUpdateUtil
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.IntentUtil


class SettingsActivity : AppCompatActivity() {

    private lateinit var activityBinding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityBinding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(activityBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val aboutPreference = findPreference<Preference>("about_preference")
            aboutPreference?.setOnPreferenceClickListener {
                IntentUtil.startActivity(requireContext(), AboutActivity::class.java)
                true
            }

            val checkUpdatePreference = findPreference<Preference>("check_update_preference")
            checkUpdatePreference?.setOnPreferenceClickListener {
                CheckAppUpdateUtil.checkUpdate(requireContext())
                true
            }

            val updateLogPreference = findPreference<Preference>("update_log_preference")
            updateLogPreference?.setOnPreferenceClickListener {
                IntentUtil.startActivity(requireContext(), UpdateLogActivity::class.java)
                true
            }
        }
    }
}