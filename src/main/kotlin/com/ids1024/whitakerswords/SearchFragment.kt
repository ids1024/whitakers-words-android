package com.ids1024.whitakerswords

import java.io.IOException
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.Menu
import android.view.MenuInflater
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.v7.widget.SearchView
import android.support.v7.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.DividerItemDecoration

import kotlinx.android.synthetic.main.search.recycler_view

public class SearchFragment(english_to_latin: Boolean, focus: Boolean) : Fragment() {
    private var search_term: String = ""
    public var english_to_latin = english_to_latin
    private lateinit var preferences: SharedPreferences
    private lateinit var words: WordsWrapper
    private val focus = focus

    constructor() : this(false, false)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        val name = javaClass.`package`.name + "_preferences"
        preferences = context!!.getSharedPreferences(name, Context.MODE_PRIVATE)
        words = WordsWrapper(context!!, preferences)
        preferences.registerOnSharedPreferenceChangeListener { _, _ ->
            words.updateConfigFile()
        }
    }

    public override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.addItemDecoration(DividerItemDecoration(recycler_view.context, DividerItemDecoration.VERTICAL))

        if (savedInstanceState != null) {
            english_to_latin = savedInstanceState.getBoolean("english_to_latin")
            searchWord(savedInstanceState.getString("search_term"))
        }
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
        val search_view = menu_item.actionView!! as SearchView
        if (english_to_latin) {
            search_view.queryHint = resources.getString(R.string.english_to_latin)
        } else {
            search_view.queryHint = resources.getString(R.string.latin_to_english)
        }

        search_view.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchWord(query)
                search_view.clearFocus()
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (preferences.getBoolean("search_on_keypress", true)) {
                    searchWord(query)
                }
                return true
            }
        })

        if (focus) {
            menu_item.expandActionView()
        }
    }

    private fun searchWord(search_term: String) {
        this.search_term = search_term

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
