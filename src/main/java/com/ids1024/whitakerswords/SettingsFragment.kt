package com.ids1024.whitakerswords

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

/**
* Fragment for the settings page.
*/
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return if (preference.key == "light_theme") {
            requireActivity().recreate()
            true
        } else {
            false
        }
    }
}
