package com.ids1024.whitakerswords

import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.File
import java.io.IOException
import android.util.Log
import android.content.Context 
import android.content.SharedPreferences

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

public class WordsWrapper(context: Context, preferences: SharedPreferences) {
    // The version number of the APK as specified in the manifest.
    private val apkVersion: Int;
    private val preferences = preferences;
    private val context = context
    init {
        apkVersion = context.packageManager
                            .getPackageInfo(context.packageName, 0)
                            .versionCode
        copyFiles()
    }

    /** Deletes all files under the files directory.  */
    private fun deleteLegacyDataDirectoryContents() {
        deleteFile(context.filesDir, false)
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
        deleteFile(context.cacheDir, false)
        versionedCacheDir.mkdirs()
    }

    private fun getFile(filename: String): File {
        return File(context.cacheDir, String.format("%d/%s", apkVersion, filename))
    }

    @Throws(IOException::class)
    private fun copyFiles() {
        deleteLegacyDataDirectoryContents()
        createAndCleanupCacheDirectories()

        val buffer = ByteArray(32 * 1024)
        for (filename in context.assets.list("words")) {
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
            ins = context.assets.open("words/$filename")
            fos = FileOutputStream(outputFile)
            var read = ins!!.read(buffer)
            while (read > 0) {
                fos.write(buffer, 0, read)
                read = ins!!.read(buffer)
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
    public fun executeWords(text: String, english_to_latin: Boolean): String {
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

    @Throws(IOException::class)
    public fun updateConfigFile() {
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
