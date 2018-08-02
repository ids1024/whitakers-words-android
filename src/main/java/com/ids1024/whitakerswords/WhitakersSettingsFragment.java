package com.ids1024.whitakerswords;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class WhitakersSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
