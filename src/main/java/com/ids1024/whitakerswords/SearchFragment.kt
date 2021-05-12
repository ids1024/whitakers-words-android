package com.ids1024.whitakerswords

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import java.io.IOException
import com.ids1024.whitakerswords.databinding.SearchBinding

/**
* Fragment providing the search UI.
*/
class SearchFragment(english_to_latin: Boolean) : Fragment() {
    private var search_term: String = ""
    var english_to_latin = english_to_latin
    private lateinit var preferences: SharedPreferences
    private lateinit var words: WordsWrapper
    private lateinit var binding: SearchBinding

    constructor() : this(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        words = WordsWrapper(requireContext())
        preferences.registerOnSharedPreferenceChangeListener { _, _ ->
            words.updateConfigFile()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        binding.searchView.setOnQueryTextListener(null)
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL))

        if (english_to_latin) {
            binding.searchView.queryHint = resources.getString(R.string.english_to_latin)
        } else {
            binding.searchView.queryHint = resources.getString(R.string.latin_to_english)
        }

        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchWord(query)
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (preferences.getBoolean("search_on_keypress", true)) {
                    searchWord(query)
                }
                return true
            }
        })

        if (savedInstanceState != null) {
            english_to_latin = savedInstanceState.getBoolean("english_to_latin")
            searchWord(savedInstanceState.getString("search_term")!!)
        } else if (search_term != "") {
            searchWord(search_term)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("search_term", search_term)
        outState.putBoolean("english_to_latin", english_to_latin)
        super.onSaveInstanceState(outState)
    }

    private fun searchWord(search_term: String) {
        this.search_term = search_term

        val result: String
        try {
            result = words.executeWords(search_term, english_to_latin)
        } catch (ex: IOException) {
            val toast = Toast.makeText(context, "Failed to execute words!", Toast.LENGTH_SHORT)
            toast.show()
            return
        }

        val results = parse_words(result)
        binding.recyclerView.adapter = SearchAdapter(results)
    }
}
