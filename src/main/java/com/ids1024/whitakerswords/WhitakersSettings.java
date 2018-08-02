package com.ids1024.whitakerswords;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class WhitakersSettings extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                                   .replace(android.R.id.content, new WhitakersSettingsFragment())
                                   .commit();
    }
}
