package com.ids1024.whitakerswords;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.AssetManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.ForegroundColorSpan;
import android.graphics.Typeface;
import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.util.Log;

public class WhitakersWords extends ListActivity
                            implements OnSharedPreferenceChangeListener {
    private static final String TAG = "words";

    private static final String WORDS_EXECUTABLE = "words";

    private String search_term;
    private final ArrayList<SpannableStringBuilder> results = new ArrayList<>();
    private ListView list_view;
    private EditText search_term_view;
    private ToggleButton english_to_latin_view;
    private int apkVersion = -1;

    /** Returns the version number of the APK as specified in the manifest. */
    private int getVersion() {
        if (apkVersion < 0) {
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                apkVersion = pInfo.versionCode;
            } catch (NameNotFoundException e) {
                // should never happen, since this code can't run without the package
                // being installed
                throw new RuntimeException(e);
            }
        }
        return apkVersion;
    }

    private static void deleteFile(File f, boolean actuallyDelete) {
        if (f.isDirectory()) {
            File[] directoryContents = f.listFiles();
            if (directoryContents != null) {
                for (File subFile : directoryContents) {
                    deleteFile(subFile, true);
                }
            }
        }

        if (actuallyDelete) {
            Log.d(TAG, String.format("Deleting %s", f.getPath()));
            if (!f.delete()) {
                Log.w(TAG, String.format("Unable to delete %s", f.getPath()));
            }
        }
    }

    /** Deletes all files under the files directory. */
    private void deleteLegacyDataDirectoryContents() {
        deleteFile(getFilesDir(), false);
    }

    /** Ensures the appropriate versioned cache directory is created. The
     * version number is derived from the APK version code.
     *
     * <p>Older directories and their contents from a prior APK version will be
     * removed automatically.
     */
    private void createAndCleanupCacheDirectories() {
      File versionedCacheDir = getFile("");

      if (versionedCacheDir.exists()) {
        return;
      }

      // delete the entire contents of cache and then create the versioned directory
      deleteFile(getCacheDir(), false);
      versionedCacheDir.mkdirs();
    }

    private File getFile(String filename) {
        return new File(getCacheDir(), String.format("%d/%s", getVersion(), filename));
    }

    private void copyFiles() throws IOException {
        deleteLegacyDataDirectoryContents();
        createAndCleanupCacheDirectories();

        byte[] buffer = new byte[32*1024];
        for (String filename : getAssets().list("words")) {
            copyFile(filename, buffer);
        }

        updateConfigFile();
        getFile(WORDS_EXECUTABLE).setExecutable(true);
    }

    private void copyFile(String filename, byte[] buffer) throws IOException {
        InputStream ins = null;
        FileOutputStream fos = null;
        File outputFile = getFile(filename);
        // if the file already exists, don't copy it again 
        if (outputFile.exists()) {
          return;
        }

        try {
            ins = getAssets().open("words/" + filename);
            fos = new FileOutputStream(outputFile);
            int read;
            while ((read = ins.read(buffer)) > 0) {
                fos.write(buffer, 0, read);
            }
        } finally {
            if (ins != null) {
              ins.close();
            }
            if (fos != null) {
              fos.close();
            }
        }
    }

    // TODO(tcj): Execute this is another thread to prevent UI deadlocking
    private String executeWords(String text, boolean fromenglish) throws IOException {
        String wordspath = getFile(WORDS_EXECUTABLE).getPath();
        Process process;
        String[] command;
        if (fromenglish) {
            command = new String[] {wordspath, "~E", text};
        } else {
            command = new String[] {wordspath, text};
        }
        process = Runtime.getRuntime().exec(command, null, getFile(""));

        BufferedReader reader = null;
        StringBuffer output = new StringBuffer();
        try {
            reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }    
        
        try {
          process.waitFor();

          int exitValue = process.exitValue();
          if (exitValue != 0) {
              Log.e(TAG, String.format("words subprocess returned %d", exitValue));
          }
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(ex);
        }

        return output.toString();
    }

    private void searchWord() {
	      results.clear();

        String result;
        try {
           result = executeWords(search_term, english_to_latin_view.isChecked());
        } catch (IOException ex) {
          Toast.makeText(this, "Failed to execute words!", Toast.LENGTH_SHORT);
          return;
        }

        SpannableStringBuilder processed_result = new SpannableStringBuilder();
	int prev_code = 0;
        for (String line: result.split("\n")) {
            String[] words = line.split(" +");
            String handled_line = TextUtils.join(" ", words);
	    int pearse_code = 0;
	    if (words[0].length() == 2) {
            try {
                pearse_code = Integer.parseInt(words[0]);
                handled_line = handled_line.substring(3);
            } catch (NumberFormatException e) {}
	    }
            // Indent meanings
            if (pearse_code == 3) {
                handled_line = "  " + handled_line;
            }

            if (line.isEmpty() || line.equals("*")) {
		if (line.equals("*")) {
                    processed_result.append("*");
		}
                String finalresult = processed_result.toString().trim();
                if (!finalresult.isEmpty()) {
                    results.add(processed_result);
                }
                processed_result = new SpannableStringBuilder();
		continue;
	        }

            int startindex = processed_result.length();
            processed_result.append(handled_line + "\n");

            Object span = null;
            int endindex = processed_result.length();
            switch (pearse_code) {
                // Forms
                case 1:
                    span = new StyleSpan(Typeface.BOLD);
                    endindex = startindex + words[1].length();
                    break;
                // Dictionary forms
                case 2:
                    // A HACK(?) for parsing output of searches like
                    // "quod", which show shorter output for dictionary forms
                    if (words[1].startsWith("[")) {
                        break;
                    }
                    int index = 1;
                    endindex = startindex;
                    do {
                        endindex += words[index].length() + 1;
                        index += 1;
                    } while (words[index-1].endsWith(","));

                    span = new StyleSpan(Typeface.BOLD);
                    break;
                // Meaning
                case 3:
                    span = new StyleSpan(Typeface.ITALIC);
                    break;
                // Not found
                case 4:
                    span = new ForegroundColorSpan(Color.RED);
                    break;
		// Addons
		case 5:
		    break;
		// Tricks/syncope/addons?
		case 6:
		    break;
            }
            processed_result.setSpan(span, startindex, endindex, 0);

	    prev_code = pearse_code;

        }
        String finalresult = processed_result.toString().trim();
        if (!finalresult.isEmpty()) {
            results.add(processed_result);
        }

        ArrayAdapter<SpannableStringBuilder> itemsAdapter =
            new ArrayAdapter<SpannableStringBuilder>(getApplicationContext(), R.layout.result, results);
        list_view.setAdapter(itemsAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            copyFiles();
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        setContentView(R.layout.main);

        getPreferences().registerOnSharedPreferenceChangeListener(this);

        list_view = (ListView)findViewById(android.R.id.list);
        search_term_view = (EditText)findViewById(R.id.search_term);
        english_to_latin_view = (ToggleButton)findViewById(R.id.english_to_latin);

        search_term_view.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if ((actionId == EditorInfo.IME_ACTION_SEARCH) ||
                                (actionId==EditorInfo.IME_NULL &&
                                 event.getAction()==KeyEvent.ACTION_DOWN)) {

                    search_term = search_term_view.getText().toString();

                    searchWord();
                    v.setText("");
                    handled = true;
                }
                return handled;
            }
        });

        if (savedInstanceState != null) {
            search_term = savedInstanceState.getString("search_term");
            english_to_latin_view.setChecked(
                savedInstanceState.getBoolean("english_to_latin"));
            searchWord();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("search_term", search_term);
        outState.putBoolean("english_to_latin", english_to_latin_view.isChecked());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, WhitakersSettings.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private SharedPreferences getPreferences() {
        String name = getClass().getPackage().getName() + "_preferences";
        return getSharedPreferences(name, MODE_PRIVATE);
    }

    private void updateConfigFile() throws IOException {
        SharedPreferences sharedPreferences = getPreferences();

        File file = getFile("WORD.MOD");
        FileOutputStream fos = new FileOutputStream(file);
        for (String setting: new String[] {"trim_output",
                "do_unknowns_only", "ignore_unknown_names",
                "ignore_unknown_caps", "do_compounds", "do_fixes",
                "do_dictionary_forms", "show_age", "show_frequency",
                "do_examples", "do_only_meanings",
                "do_stems_for_unknown"}) {
            if (sharedPreferences.contains(setting)) {
                String value = sharedPreferences.getBoolean(setting, false) ? "Y" : "N";
                String line = setting.toUpperCase(Locale.US) + " " + value + "\n";
                fos.write(line.getBytes());
            }
        }
        fos.close();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
    String changed_key) {
        try {
            updateConfigFile();
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
