package com.ids1024.whitakerswords;

import android.preference.PreferenceActivity;
import android.os.Bundle;

public class WhitakersSettings extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
