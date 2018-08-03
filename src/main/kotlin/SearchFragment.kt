package com.ids1024.whitakerswords

import java.io.IOException
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.view.View 
import android.view.Menu
import android.view.MenuItem
import android.view.MenuInflater
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.v7.widget.SearchView
import android.support.v7.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.DividerItemDecoration

public class SearchFragment(english_to_latin: Boolean, focus: Boolean) : Fragment(), OnSharedPreferenceChangeListener {
    private var search_term: String = ""
    private lateinit var recycler_view: RecyclerView
    private lateinit var search_view: SearchView
    private var english_to_latin = english_to_latin
    private lateinit var preferences: SharedPreferences
    private lateinit var words: WordsWrapper
    private var focus = focus

    public override fun onCreateView(inflater: LayoutInflater,
                                     container: ViewGroup?,
                                     savedInstanceState: Bundle?): View {
        val name = javaClass.`package`.name + "_preferences"
        preferences = context!!.getSharedPreferences(name, Context.MODE_PRIVATE)

        words = WordsWrapper(context!!, preferences)

        var view = inflater.inflate(R.layout.search, container, false)

        preferences.registerOnSharedPreferenceChangeListener(this)

        recycler_view = view.findViewById(R.id.list)!!
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.addItemDecoration(DividerItemDecoration(recycler_view.context, DividerItemDecoration.VERTICAL))

        if (savedInstanceState != null) {
            search_term = savedInstanceState.getString("search_term")
            english_to_latin = savedInstanceState.getBoolean("english_to_latin")
            searchWord()
        }

        setHasOptionsMenu(true)
        return view
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("search_term", search_term)
        outState.putBoolean("english_to_latin", english_to_latin)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.main, menu)

        val menu_item = menu.findItem(R.id.action_search)
        search_view = menu_item.actionView!! as SearchView
        search_view.setIconifiedByDefault(false)
        if (english_to_latin) {
            search_view.queryHint = resources.getString(R.string.english_to_latin)
        } else {
            search_view.queryHint = resources.getString(R.string.latin_to_english)
        }

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

        if (focus) {
            menu_item.expandActionView()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           changed_key: String) {
        words.updateConfigFile()
    }

    private fun searchWord() {
        val result: String
        try {
            result = words.executeWords(search_term, english_to_latin)
        } catch (ex: IOException) {
            Toast.makeText(context, "Failed to execute words!", Toast.LENGTH_SHORT)
            return
        }

        val results = parse_words(result)
        recycler_view.adapter = SearchAdapter(results)
    }
}
