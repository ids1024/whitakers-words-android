package com.ids1024.whitakerswords

import java.io.IOException
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
import android.support.v7.preference.PreferenceManager

import kotlinx.android.synthetic.main.search.recycler_view

/**
* Fragment providing the search UI.
*/
class SearchFragment(english_to_latin: Boolean) : Fragment() {
    private var search_term: String = ""
    private var search_view: SearchView? = null
    var english_to_latin = english_to_latin
    private lateinit var preferences: SharedPreferences
    private lateinit var words: WordsWrapper

    constructor() : this(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        words = WordsWrapper(context!!)
        preferences.registerOnSharedPreferenceChangeListener { _, _ ->
            words.updateConfigFile()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.search, container, false)
    }

    override fun onDestroyView() {
        search_view?.setOnQueryTextListener(null)
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.addItemDecoration(DividerItemDecoration(recycler_view.context, DividerItemDecoration.VERTICAL))

        if (savedInstanceState != null) {
            english_to_latin = savedInstanceState.getBoolean("english_to_latin")
            searchWord(savedInstanceState.getString("search_term"))
        } else if (search_term != "") {
            searchWord(search_term)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("search_term", search_term)
        outState.putBoolean("english_to_latin", english_to_latin)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)

        val menu_item = menu.findItem(R.id.action_search)
        search_view = menu_item.actionView!! as SearchView
        if (english_to_latin) {
            search_view!!.queryHint = resources.getString(R.string.english_to_latin)
        } else {
            search_view!!.queryHint = resources.getString(R.string.latin_to_english)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        search_view!!.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchWord(query)
                search_view!!.clearFocus()
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (preferences.getBoolean("search_on_keypress", true)) {
                    searchWord(query)
                }
                return true
            }
        })
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
