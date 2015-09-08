package com.ids1024.whitakerswords;

import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.app.ActionBar;

public class WhitakersSettings extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
