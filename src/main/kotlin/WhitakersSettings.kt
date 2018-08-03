package com.ids1024.whitakerswords

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

public class WhitakersSettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
                              .replace(android.R.id.content, WhitakersSettingsFragment())
                              .commit()
    }
}
