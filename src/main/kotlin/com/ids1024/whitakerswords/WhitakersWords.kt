package com.ids1024.whitakerswords

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.main.drawer_layout
import kotlinx.android.synthetic.main.main.nav_view

class WhitakersWords : AppCompatActivity() {
    var fragments = HashMap<Int, Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main)

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.content)
            val item = when (fragment) {
                is SearchFragment -> {
                    if (fragment.english_to_latin) {
                        R.id.action_english_to_latin
                    } else {
                        R.id.action_latin_to_english
                    }
                }
                is SettingsFragment -> R.id.action_settings
                is AboutFragment -> R.id.action_about
                else -> null
            }

            if (item != null) {
                nav_view.setCheckedItem(item)
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.content, getFragment(R.id.action_latin_to_english))
                .addToBackStack(null)
                .commit()
        }

        nav_view.inflateMenu(R.menu.navigation)
        nav_view.setNavigationItemSelectedListener { item ->
            drawer_layout.closeDrawers()
            val fragment = getFragment(item.itemId)
            supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.content, fragment)
                .commit()

            true
        }
    }

    private fun getFragment(id: Int): Fragment {
        var fragment = fragments.get(id)
        if (fragment == null) {
            fragment = when (id) {
                R.id.action_latin_to_english ->
                    SearchFragment(false)
                R.id.action_english_to_latin ->
                    SearchFragment(true)
                R.id.action_settings ->
                    SettingsFragment()
                R.id.action_about ->
                    AboutFragment()
                else ->
                    throw RuntimeException() // Unreachable
            }

            fragments.put(id, fragment)
        }
        return fragment
    }
}
