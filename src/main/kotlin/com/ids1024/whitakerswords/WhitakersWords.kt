package com.ids1024.whitakerswords

import android.os.Bundle
import android.view.MenuItem
import android.content.res.Configuration
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.ActionBarDrawerToggle

import kotlinx.android.synthetic.main.main.drawer_layout
import kotlinx.android.synthetic.main.main.nav_view

class WhitakersWords : AppCompatActivity() {
    var fragments = HashMap<Int, Fragment>()
    private lateinit var action_bar_drawer_toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.content)

            val (title, item) = when (fragment) {
                is SearchFragment -> {
                    if (fragment.english_to_latin) {
                        Pair(R.string.english_to_latin, R.id.action_english_to_latin)
                    } else {
                        Pair(R.string.latin_to_english, R.id.action_latin_to_english)
                    }
                }
                is SettingsFragment ->
                    Pair(R.string.settings, R.id.action_settings)
                is AboutFragment ->
                    Pair(R.string.about_long_title, R.id.action_about)
                else -> throw RuntimeException() // Unreachable
            }

            supportActionBar!!.title = resources.getString(title)
            nav_view.setCheckedItem(item)
        }

        if (savedInstanceState != null) {
            for (k in savedInstanceState.keySet()) {
                if (k.startsWith("fragment_")) {
                    val fragment = supportFragmentManager.getFragment(savedInstanceState, k)
                    fragments.put(k.substring(9).toInt(), fragment)
                }
            }
        } else {
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

        action_bar_drawer_toggle = ActionBarDrawerToggle(
            this, drawer_layout, R.string.open_drawer,
            R.string.close_drawer)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        for ((k, v) in fragments) {
            supportFragmentManager.putFragment(outState, "fragment_$k", v)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return action_bar_drawer_toggle.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
	super.onConfigurationChanged(newConfig)
        action_bar_drawer_toggle.onConfigurationChanged(newConfig)
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
