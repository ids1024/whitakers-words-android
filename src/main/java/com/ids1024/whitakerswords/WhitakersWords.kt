package com.ids1024.whitakerswords

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.ids1024.whitakerswords.databinding.MainBinding

class WhitakersWords : AppCompatActivity() {
    private var fragments = HashMap<Int, Fragment>()
    private lateinit var action_bar_drawer_toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val nightMode = if (preferences.getBoolean("light_theme", false)) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.content)

            val (title, item) = when (fragment) {
                is SearchFragment -> {
                    if (fragment.english_to_latin) {
                        Pair(R.string.app_name, R.id.action_english_to_latin)
                    } else {
                        Pair(R.string.app_name, R.id.action_latin_to_english)
                    }
                }
                is SettingsFragment ->
                    Pair(R.string.settings, R.id.action_settings)
                is AboutFragment ->
                    Pair(R.string.about_long_title, R.id.action_about)
                else -> throw RuntimeException() // Unreachable
            }

            supportActionBar!!.title = resources.getString(title)
            binding.navView.setCheckedItem(item)
        }

        var cur_fragment = R.id.action_latin_to_english
        if (savedInstanceState != null) {
            for (k in savedInstanceState.keySet()) {
                if (k.startsWith("fragment_")) {
                    val fragment = supportFragmentManager.getFragment(savedInstanceState, k)!!
                    fragments[k.substring(9).toInt()] = fragment
                }
            }
            cur_fragment = savedInstanceState.getInt("cur_fragment", cur_fragment)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, getFragment(cur_fragment))
            .addToBackStack(null)
            .commit()

        binding.navView.inflateMenu(R.menu.navigation)
        binding.navView.setNavigationItemSelectedListener { item ->
            binding.drawerLayout.closeDrawers()
            val fragment = getFragment(item.itemId)
            supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.content, fragment)
                .commit()

            true
        }

        action_bar_drawer_toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, R.string.open_drawer,
            R.string.close_drawer
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val cur_fragment = supportFragmentManager.findFragmentById(R.id.content)
        for ((k, v) in fragments) {
            if (cur_fragment == v) {
                outState.putInt("cur_fragment", k)
            }
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
        var fragment = fragments[id]
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

            fragments[id] = fragment
        }
        return fragment
    }
}
