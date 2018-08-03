package com.ids1024.whitakerswords

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.main.drawer_layout
import kotlinx.android.synthetic.main.main.nav_view

class WhitakersWords : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, SearchFragment(false, false))
            .commit()

        nav_view.inflateMenu(R.menu.navigation)
        nav_view.setCheckedItem(R.id.action_latin_to_english)
        nav_view.setNavigationItemSelectedListener { item ->
            drawer_layout.closeDrawers()
            val fragment = when (item.itemId) {
                R.id.action_latin_to_english -> {
                    SearchFragment(false, true)
                }
                R.id.action_english_to_latin -> {
                    SearchFragment(true, true)
                }
                R.id.action_settings -> {
                    SettingsFragment()
                }
                R.id.action_about -> {
                    AboutFragment()
                }
                else -> {
                    throw RuntimeException() // Unreachable
                }
            }
            supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.content, fragment)
                .commit()

            true
        }
    }
}
