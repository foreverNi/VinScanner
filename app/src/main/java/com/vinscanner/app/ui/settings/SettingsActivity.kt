package com.vinscanner.app.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.vinscanner.app.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "vin_scanner_settings"
            val email = EditTextPreference(requireContext()).apply {
                key = getString(R.string.settings_email_key)
                title = getString(R.string.settings_email_title)
                summary = getString(R.string.settings_email_summary)
            }
            val subject = EditTextPreference(requireContext()).apply {
                key = getString(R.string.settings_subject_key)
                title = getString(R.string.settings_subject_title)
                summary = getString(R.string.settings_subject_summary)
                text = getString(R.string.settings_default_subject)
            }
            preferenceScreen = preferenceManager.createPreferenceScreen(requireContext()).apply {
                addPreference(email)
                addPreference(subject)
            }
        }
    }
}
