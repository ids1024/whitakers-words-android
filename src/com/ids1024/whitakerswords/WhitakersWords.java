package com.ids1024.whitakerswords;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import android.app.Activity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;
import android.widget.ToggleButton;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.AssetManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

public class WhitakersWords extends Activity
                            implements OnEditorActionListener {
    /** Called when the activity is first created. */

    public void copyFiles() throws IOException {
        for (String filename: getAssets().list("words")) {
            InputStream ins = getAssets().open("words/" + filename);
            byte[] buffer = new byte[ins.available()];
            ins.read(buffer);
            ins.close();
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            fos.write(buffer);
            fos.close();
        }
        File wordsbin = getFileStreamPath("words");
        wordsbin.setExecutable(true);
    }

    public String executeWords(String text, boolean fromenglish) {
        String wordspath = getFilesDir().getPath() + "/words";
        Process process;
        try {
            String[] command;
            if (fromenglish) {
                command = new String[] {wordspath, "~E", text};
            } else {
                command = new String[] {wordspath, text};
            }
            process = Runtime.getRuntime().exec(command, null, getFilesDir());

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            
            reader.close();
            process.waitFor();

            return output.toString();

        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        } catch(InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void searchWord(View view) {
        TextView result_text = (TextView)findViewById(R.id.result_text);
        EditText search_term = (EditText)findViewById(R.id.search_term);
        ToggleButton english_to_latin = (ToggleButton)findViewById(R.id.english_to_latin);
        String term = search_term.getText().toString();
	
        String result = executeWords(term, english_to_latin.isChecked());
        SpannableStringBuilder processed_result = new SpannableStringBuilder();
        for (String line: result.split("\n")) {
            String[] words = line.split(" +");
            String handled_line = TextUtils.join(" ", words);
            if (words[0].equals("01") || words[0].equals("02")
                            || words[0].equals("03")) {
                handled_line = handled_line.substring(3);
                            }
            int startindex = processed_result.length();
            processed_result.append(handled_line + "\n");
            // Forms
            if (words[0].equals("01")) {
                processed_result.setSpan(
                                new StyleSpan(Typeface.BOLD),
                                startindex,
                                startindex + words[1].length(),
                                0);
            }
            // Dictionary forms
            else if (words[0].equals("02")) {
                int index = 1;
                int endindex = startindex;
                do {
                    endindex += words[index].length() + 1;
                    index += 1;
                } while (words[index-1].endsWith(","));

                processed_result.setSpan(
                                new StyleSpan(Typeface.BOLD),
                                startindex,
                                endindex,
                                0);
            }
            // Meaning
            else if (words[0].equals("03")) {
                processed_result.setSpan(
                                new StyleSpan(Typeface.ITALIC),
                                startindex,
                                processed_result.length(),
                                0);
            }
        }
        result_text.setText((CharSequence)processed_result);
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            searchWord((View)v);
            handled = true;
        }
        return handled;
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

        EditText search_term = (EditText)findViewById(R.id.search_term);
        search_term.setOnEditorActionListener(this);
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
}
