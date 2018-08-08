package com.ids1024.whitakerswords

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat

/**
* Fragment for the settings page.
*/
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
