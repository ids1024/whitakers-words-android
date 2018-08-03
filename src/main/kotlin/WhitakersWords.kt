package com.ids1024.whitakerswords

import java.util.ArrayList
import java.io.IOException
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.view.View 
import android.view.Menu
import android.view.MenuItem
import android.view.MenuInflater
import android.support.v7.widget.SearchView
import android.support.v7.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import android.os.Bundle
import android.content.Intent
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.StyleSpan
import android.text.style.ForegroundColorSpan
import android.graphics.Typeface
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.support.design.widget.NavigationView
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar

class WhitakersWords : AppCompatActivity(), OnSharedPreferenceChangeListener {
    private var search_term: String = ""
    private lateinit var recycler_view: RecyclerView
    private lateinit var search_view: SearchView
    private lateinit var drawer_layout: DrawerLayout
    private var english_to_latin: Boolean = false
    private lateinit var preferences: SharedPreferences;
    private lateinit var words: WordsWrapper;

    private fun searchWord() {
        val results = ArrayList<SpannableStringBuilder>()

        val result: String
        try {
            result = words.executeWords(search_term, english_to_latin)
        } catch (ex: IOException) {
            Toast.makeText(this, "Failed to execute words!", Toast.LENGTH_SHORT)
            return
        }

        var processed_result = SpannableStringBuilder()
        for (line in result.split("\n".toRegex())) {
            val words = line.split(" +".toRegex())
            var handled_line = TextUtils.join(" ", words)
            var pearse_code = 0
            if (words.size >= 1 && words[0].length == 2) {
                try {
                    pearse_code = Integer.parseInt(words[0])
                    handled_line = handled_line.substring(3)
                } catch (e: NumberFormatException) {
                }

            }
            // Indent meanings
            if (pearse_code == 3) {
                handled_line = "  $handled_line"
            }

            if (line.isEmpty() || line == "*") {
                if (line == "*") {
                    processed_result.append("*")
                }
                val finalresult = processed_result.toString().trim { it <= ' ' }
                if (!finalresult.isEmpty()) {
                    results.add(processed_result)
                }
                processed_result = SpannableStringBuilder()
                continue
            }

            val startindex = processed_result.length
            processed_result.append(handled_line + "\n")

            var span: Any? = null
            var endindex = processed_result.length
            when (pearse_code) {
            // Forms
                1 -> {
                    span = StyleSpan(Typeface.BOLD)
                    endindex = startindex + words[1].length
                }
            // Dictionary forms
                2 -> {
                    // A HACK(?) for parsing output of searches like
                    // "quod", which show shorter output for dictionary forms
                    if (!words[1].startsWith("[")) {
                        var index = 1
                        endindex = startindex
                        do {
                            endindex += words[index].length + 1
                            index += 1
                        } while (words[index - 1].endsWith(","))

                        span = StyleSpan(Typeface.BOLD)
	    	    }
                }
            // Meaning
                3 -> span = StyleSpan(Typeface.ITALIC)
            // Not found
                4 -> span = ForegroundColorSpan(Color.RED)
            // Addons
                5 -> {
                }
            // Tricks/syncope/addons?
                6 -> {
                }
            }
            processed_result.setSpan(span, startindex, endindex, 0)
        }
        val finalresult = processed_result.toString().trim { it <= ' ' }
        if (!finalresult.isEmpty()) {
            results.add(processed_result)
        }

        recycler_view.adapter = SearchAdapter(results)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = javaClass.`package`.name + "_preferences"
        preferences = getSharedPreferences(name, Context.MODE_PRIVATE)

        words = WordsWrapper(this, preferences)

        setContentView(R.layout.main)

        preferences.registerOnSharedPreferenceChangeListener(this)

        recycler_view = findViewById(R.id.list)!!
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addItemDecoration(DividerItemDecoration(recycler_view.context, DividerItemDecoration.VERTICAL))

        drawer_layout = findViewById(R.id.drawer_layout)!!

        val navigation_view = findViewById<NavigationView>(R.id.nav_view)
        navigation_view.inflateMenu(R.menu.navigation)
        navigation_view.setCheckedItem(R.id.action_latin_to_english)
        val activity = this
        val action_bar = supportActionBar!!
        navigation_view.setNavigationItemSelectedListener { item ->
            drawer_layout.closeDrawers()
            when (item.itemId) {
                R.id.action_latin_to_english -> {
                    english_to_latin = false
                    setSearchQueryHint()
                    // https://stackoverflow.com/questions/10089993/android-how-to-focus-actionbar-searchview
                    action_bar.customView = search_view
                    action_bar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
                    search_view.isFocusable = true
                    search_view.isIconified = false
                    search_view.requestFocusFromTouch()
                    true
                }
                R.id.action_english_to_latin -> {
                    english_to_latin = true
                    setSearchQueryHint()
                    action_bar.customView = search_view
                    action_bar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
                    search_view.isFocusable = true
                    search_view.isIconified = false
                    search_view.requestFocusFromTouch()
                    true
                }
                R.id.action_settings -> {
                    val intent = Intent(activity, WhitakersSettings::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_about -> {
                    val intent = Intent(activity, WhitakersAbout::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState != null) {
            search_term = savedInstanceState.getString("search_term")
            english_to_latin = savedInstanceState.getBoolean("english_to_latin")
            searchWord()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("search_term", search_term)
        outState.putBoolean("english_to_latin", english_to_latin)
        super.onSaveInstanceState(outState)
    }

    // TODO: Replace method with more elegant solution
    private fun setSearchQueryHint() {
        if (english_to_latin) {
            search_view.queryHint = resources.getString(R.string.english_to_latin)
        } else {
            search_view.queryHint = resources.getString(R.string.latin_to_english)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu items for use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)

        search_view = menu.findItem(R.id.action_search).actionView!! as SearchView
        setSearchQueryHint()
        search_view.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                search_term = query
                searchWord()
                search_view.clearFocus()
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (preferences.getBoolean("search_on_keypress", true)) {
                    search_term = query
                    searchWord()
                }
                return true
            }
        })


        return super.onCreateOptionsMenu(menu)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           changed_key: String) {
        words.updateConfigFile()
    }
}
