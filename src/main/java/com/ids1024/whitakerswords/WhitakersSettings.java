package com.ids1024.whitakerswords;

import java.util.Map;
import android.preference.PreferenceActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.app.ActionBar;
import java.io.IOException;
import java.io.FileOutputStream;

public class WhitakersSettings extends PreferenceActivity
                               implements OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
    String changed_key) {
        try {
            FileOutputStream fos = openFileOutput("WORD.MOD", MODE_PRIVATE);
            for (String setting: new String[] {"trim_output",
		    "do_unknowns_only", "ignore_unknown_names",
		    "ignore_unknown_caps", "do_compounds", "do_fixes",
                    "do_dictionary_forms", "show_age", "show_frequency",
		    "do_examples", "do_only_meanings",
		    "do_stems_for_unknown"}) {
		if (sharedPreferences.contains(setting)) {
                    String value = sharedPreferences.getBoolean(setting, false) ? "Y" : "N";
                    String line = setting.toUpperCase() + " " + value + "\n";
                    fos.write(line.getBytes());
		}
            }
            fos.close();
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
