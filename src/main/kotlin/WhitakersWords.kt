package com.ids1024.whitakerswords

import android.os.Bundle
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout

class WhitakersWords : AppCompatActivity() {
    private lateinit var drawer_layout: DrawerLayout

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main)
        supportFragmentManager.beginTransaction()
                              .replace(R.id.content, SearchFragment(false, false))
                              .commit()

        drawer_layout = findViewById(R.id.drawer_layout)!!

        val navigation_view = findViewById<NavigationView>(R.id.nav_view)
        navigation_view.inflateMenu(R.menu.navigation)
        navigation_view.setCheckedItem(R.id.action_latin_to_english)
        navigation_view.setNavigationItemSelectedListener { item ->
            drawer_layout.closeDrawers()
            val fragment = when (item.itemId) {
                R.id.action_latin_to_english -> {
                    SearchFragment(false, true)
                }
                R.id.action_english_to_latin -> {
                    SearchFragment(true, true)
                }
                R.id.action_settings -> {
                    WhitakersSettingsFragment()
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
