package com.ids1024.whitakerswords

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.StringWriter
import java.util.Locale

private val TAG = "words"
private val WORDS_EXECUTABLE = "words"

private fun emptyDirectory(f: File) {
    val directoryContents = f.listFiles()
    if (directoryContents != null) {
        for (subFile in directoryContents) {
            if (!subFile.deleteRecursively()) {
                Log.w(TAG, "Unable to delete ${f.path}")
            }
        }
    } else {
        Log.w(TAG, "Unable to clear ${f.path}")
    }
}

/**
 * Wraps the `words` binary. This handles extraction from the apk and execution.
 */
class WordsWrapper(context: Context) {
    // The version number of the APK as specified in the manifest.
    private val apkVersion: Int
    private val preferences: SharedPreferences
    private val context = context
    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        apkVersion = context.packageManager
                            .getPackageInfo(context.packageName, 0)
                            .versionCode
        copyFiles()
    }

    /** Deletes all files under the files directory.  */
    private fun deleteLegacyDataDirectoryContents() {
        emptyDirectory(context.filesDir)
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
        emptyDirectory(context.cacheDir)
        versionedCacheDir.mkdirs()
    }

    private fun getFile(filename: String): File {
        return File(context.cacheDir, "$apkVersion/$filename")
    }

    @Throws(IOException::class)
    private fun copyFiles() {
        deleteLegacyDataDirectoryContents()
        createAndCleanupCacheDirectories()

        for (filename in context.assets.list("words")!!) {
            copyFile(filename)
        }

        updateConfigFile()
        getFile(WORDS_EXECUTABLE).setExecutable(true)
    }

    @Throws(IOException::class)
    private fun copyFile(filename: String) {
        val outputFile = getFile(filename)
        // if the file already exists, don't copy it again
        if (outputFile.exists()) {
            return
        }

        context.assets.open("words/$filename").use { ins ->
            FileOutputStream(outputFile).use { fos ->
                ins.copyTo(fos)
            }
        }
    }

    // TODO(tcj): Execute this is another thread to prevent UI deadlocking
    /**
    * Executes `words`.
    */
    @Throws(IOException::class)
    fun executeWords(text: String, english_to_latin: Boolean): String {
        val wordspath = getFile(WORDS_EXECUTABLE).path
        val command = if (english_to_latin) {
            arrayOf<String>(wordspath, "~E", text)
        } else {
            arrayOf<String>(wordspath, text)
        }
        val process = Runtime.getRuntime().exec(command, null, getFile(""))

        val output = StringWriter()
        BufferedReader(InputStreamReader(process.inputStream)).use { ins ->
            ins.copyTo(output)
        }

        try {
            process.waitFor()

            val exitValue = process.exitValue()
            if (exitValue != 0) {
                Log.e(TAG, "words subprocess returned $exitValue")
            }
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException(ex)
        }

        return output.toString()
    }

    /**
    * Generates `WORDS.MOD` file from the app's preferences.
    */
    @Throws(IOException::class)
    fun updateConfigFile() {
        val file = getFile("WORD.MOD")
        val fos = FileOutputStream(file)
        for (setting in arrayOf("trim_output", "do_unknowns_only", "ignore_unknown_names", "ignore_unknown_caps", "do_compounds", "do_fixes", "do_dictionary_forms", "show_age", "show_frequency", "do_examples", "do_only_meanings", "do_stems_for_unknown")) {
            if (preferences.contains(setting)) {
                val value = if (preferences.getBoolean(setting, false)) "Y" else "N"
                val line = setting.toUpperCase(Locale.US) + " " + value + "\n"
                fos.write(line.toByteArray())
            }
        }
        fos.close()
    }
}
