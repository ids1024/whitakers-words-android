package com.ids1024.whitakerswords

import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.Locale
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
import android.util.Log
import android.support.v7.app.AppCompatActivity
import android.support.design.widget.NavigationView
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar

private val TAG = "words"
private val WORDS_EXECUTABLE = "words"

private fun deleteFile(f: File, actuallyDelete: Boolean) {
    if (f.isDirectory) {
        val directoryContents = f.listFiles()
        if (directoryContents != null) {
            for (subFile in directoryContents) {
                deleteFile(subFile, true)
            }
        }
    }

    if (actuallyDelete) {
        Log.d(TAG, String.format("Deleting %s", f.path))
        if (!f.delete()) {
            Log.w(TAG, String.format("Unable to delete %s", f.path))
        }
    }
}

class WhitakersWords : AppCompatActivity(), OnSharedPreferenceChangeListener {

    private var search_term: String = ""
    private lateinit var recycler_view: RecyclerView
    private var search_view: SearchView? = null
    private lateinit var drawer_layout: DrawerLayout
    private var apkVersion = -1
    private var english_to_latin: Boolean = false

    /** Returns the version number of the APK as specified in the manifest.  */
    private// should never happen, since this code can't run without the package
    // being installed
    val version: Int
        get() {
            if (apkVersion < 0) {
                try {
                    val pInfo = packageManager.getPackageInfo(packageName, 0)
                    apkVersion = pInfo.versionCode
                } catch (e: NameNotFoundException) {
                    throw RuntimeException(e)
                }

            }
            return apkVersion
        }

    private val preferences: SharedPreferences
        get() {
            val name = javaClass.`package`.name + "_preferences"
            return getSharedPreferences(name, Context.MODE_PRIVATE)
        }

    /** Deletes all files under the files directory.  */
    private fun deleteLegacyDataDirectoryContents() {
        deleteFile(filesDir, false)
    }

    /** Ensures the appropriate versioned cache directory is created. The
     * version number is derived from the APK version code.
     *
     *
     * Older directories and their contents from a prior APK version will be
     * removed automatically.
     */
    private fun createAndCleanupCacheDirectories() {
        val versionedCacheDir = getFile("")

        if (versionedCacheDir.exists()) {
            return
        }

        // delete the entire contents of cache and then create the versioned directory
        deleteFile(cacheDir, false)
        versionedCacheDir.mkdirs()
    }

    private fun getFile(filename: String): File {
        return File(cacheDir, String.format("%d/%s", version, filename))
    }

    @Throws(IOException::class)
    private fun copyFiles() {
        deleteLegacyDataDirectoryContents()
        createAndCleanupCacheDirectories()

        val buffer = ByteArray(32 * 1024)
        for (filename in assets.list("words")) {
            copyFile(filename, buffer)
        }

        updateConfigFile()
        getFile(WORDS_EXECUTABLE).setExecutable(true)
    }

    @Throws(IOException::class)
    private fun copyFile(filename: String, buffer: ByteArray) {
        var ins: InputStream? = null
        var fos: FileOutputStream? = null
        val outputFile = getFile(filename)
        // if the file already exists, don't copy it again
        if (outputFile.exists()) {
            return
        }

        try {
            ins = assets.open("words/$filename")
            fos = FileOutputStream(outputFile)
            var read = ins.read(buffer)
            while (read > 0) {
                fos.write(buffer, 0, read)
                read = ins.read(buffer)
            }
        } finally {
            if (ins != null) {
                ins.close()
            }
            if (fos != null) {
                fos.close()
            }
        }
    }

    // TODO(tcj): Execute this is another thread to prevent UI deadlocking
    @Throws(IOException::class)
    private fun executeWords(text: String): String {
        val wordspath = getFile(WORDS_EXECUTABLE).path
        val process: Process
        val command: Array<String>
        if (english_to_latin) {
            command = arrayOf<String>(wordspath, "~E", text)
        } else {
            command = arrayOf<String>(wordspath, text)
        }
        process = Runtime.getRuntime().exec(command, null, getFile(""))

        var reader: BufferedReader? = null
        val output = StringBuffer()
        try {
            reader = BufferedReader(
                    InputStreamReader(process.inputStream))
            val buffer = CharArray(4096)
            var read = reader.read(buffer)
            while (read > 0) {
                output.append(buffer, 0, read)
                read = reader.read(buffer)
            }
        } finally {
            if (reader != null) {
                reader.close()
            }
        }

        try {
            process.waitFor()

            val exitValue = process.exitValue()
            if (exitValue != 0) {
                Log.e(TAG, String.format("words subprocess returned %d", exitValue))
            }
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException(ex)
        }

        return output.toString()
    }

    private fun searchWord() {
        val results = ArrayList<SpannableStringBuilder>()

        val result: String
        try {
            result = executeWords(search_term)
        } catch (ex: IOException) {
            Toast.makeText(this, "Failed to execute words!", Toast.LENGTH_SHORT)
            return
        }

        var processed_result = SpannableStringBuilder()
        var prev_code = 0
        for (line in result.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val words = line.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var handled_line = TextUtils.join(" ", words)
            var pearse_code = 0
            if (words[0].length == 2) {
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

            prev_code = pearse_code

        }
        val finalresult = processed_result.toString().trim { it <= ' ' }
        if (!finalresult.isEmpty()) {
            results.add(processed_result)
        }

        recycler_view!!.adapter = SearchAdapter(results)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            copyFiles()
        } catch (e: IOException) {
            throw RuntimeException(e.message)
        }

        setContentView(R.layout.main)

        preferences.registerOnSharedPreferenceChangeListener(this)

        recycler_view = findViewById<View>(R.id.list)!! as RecyclerView
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addItemDecoration(DividerItemDecoration(recycler_view.context, DividerItemDecoration.VERTICAL))

        drawer_layout = findViewById<View>(R.id.drawer_layout)!! as DrawerLayout

        val navigation_view = findViewById<View>(R.id.nav_view) as NavigationView
        navigation_view.inflateMenu(R.menu.navigation)
        navigation_view.setCheckedItem(R.id.action_latin_to_english)
        val activity = this
        val action_bar = supportActionBar!!
        navigation_view.setNavigationItemSelectedListener { item ->
            val intent: Intent
            drawer_layout.closeDrawers()
            when (item.itemId) {
                R.id.action_latin_to_english -> {
                    english_to_latin = false
                    setSearchQueryHint()
                    // https://stackoverflow.com/questions/10089993/android-how-to-focus-actionbar-searchview
                    action_bar.customView = search_view
                    action_bar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
                    search_view!!.isFocusable = true
                    search_view!!.isIconified = false
                    search_view!!.requestFocusFromTouch()
                    true
                }
                R.id.action_english_to_latin -> {
                    english_to_latin = true
                    setSearchQueryHint()
                    action_bar.customView = search_view
                    action_bar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
                    search_view!!.isFocusable = true
                    search_view!!.isIconified = false
                    search_view!!.requestFocusFromTouch()
                    true
                }
                R.id.action_settings -> {
                    intent = Intent(activity, WhitakersSettings::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_about -> {
                    intent = Intent(activity, WhitakersAbout::class.java)
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
            search_view!!.queryHint = resources.getString(R.string.english_to_latin)
        } else {
            search_view!!.queryHint = resources.getString(R.string.latin_to_english)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu items for use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)

        search_view = menu.findItem(R.id.action_search).actionView as SearchView
        setSearchQueryHint()
        search_view!!.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                search_term = query
                searchWord()
                search_view!!.clearFocus()
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

    @Throws(IOException::class)
    private fun updateConfigFile() {
        val sharedPreferences = preferences

        val file = getFile("WORD.MOD")
        val fos = FileOutputStream(file)
        for (setting in arrayOf("trim_output", "do_unknowns_only", "ignore_unknown_names", "ignore_unknown_caps", "do_compounds", "do_fixes", "do_dictionary_forms", "show_age", "show_frequency", "do_examples", "do_only_meanings", "do_stems_for_unknown")) {
            if (sharedPreferences.contains(setting)) {
                val value = if (sharedPreferences.getBoolean(setting, false)) "Y" else "N"
                val line = setting.toUpperCase(Locale.US) + " " + value + "\n"
                fos.write(line.toByteArray())
            }
        }
        fos.close()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           changed_key: String) {
        try {
            updateConfigFile()
        } catch (e: IOException) {
            throw RuntimeException(e.message)
        }

    }
}
