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
import android.app.ListActivity;
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
import android.text.style.ForegroundColorSpan;
import android.graphics.Typeface;
import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class WhitakersWords extends ListActivity {
    ArrayList<String> results;

    public void copyFiles() throws IOException {
        for (String filename: getAssets().list("words")) {
            InputStream ins = getAssets().open("words/" + filename);
            byte[] buffer = new byte[4096];
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            for (int read = 0; read >= 0; read = ins.read(buffer)) {
                fos.write(buffer, 0, read);
            }
            ins.close();
            fos.close();
        }
        getFileStreamPath("words").setExecutable(true);
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
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            for (int read = 0; read >= 0; read = reader.read(buffer)) {
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
        EditText search_term = (EditText)findViewById(R.id.search_term);
        ToggleButton english_to_latin = (ToggleButton)findViewById(R.id.english_to_latin);
        String term = search_term.getText().toString();

	results.clear();

        String result = executeWords(term, english_to_latin.isChecked());
        SpannableStringBuilder processed_result = new SpannableStringBuilder();
	String prev_code = null;
        for (String line: result.split("\n")) {
            String[] words = line.split(" +");
            String handled_line = TextUtils.join(" ", words);
            if (words[0].equals("01") || words[0].equals("02")
                            || words[0].equals("03") || words[0].equals("04")) {
                handled_line = handled_line.substring(3);
                // Indent meanings
                if (words[0].equals("03")) {
                    handled_line = "  " + handled_line;
                }
            }

            if (words[0].equals("01") && prev_code != null && !prev_code.equals("01")) {
                results.add(processed_result.toString().trim());
                processed_result = new SpannableStringBuilder();
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
            // The [ thing is a HACK(?) for parsing output of searches like
            // "quod", which show shorter output for dictionary forms
            else if (words[0].equals("02") && !words[1].startsWith("[")) {
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
            // Not found
            else if (words[0].equals("04")) {
                processed_result.setSpan(
                                new ForegroundColorSpan(Color.RED),
                                startindex,
                                processed_result.length(),
                                0);
            }

	    prev_code = words[0];

        }
        results.add(processed_result.toString().trim());

        ArrayAdapter<String> itemsAdapter =
            new ArrayAdapter<String>(getApplicationContext(), R.layout.result, results);
        ListView result_list = (ListView)findViewById(android.R.id.list);
        result_list.setAdapter(itemsAdapter);
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
        search_term.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if ((actionId == EditorInfo.IME_ACTION_SEARCH) ||
                                (actionId==EditorInfo.IME_NULL &&
                                 event.getAction()==KeyEvent.ACTION_DOWN)) {
                    searchWord((View)v);
                    v.setText("");
                    handled = true;
                }
                return handled;
            }
        });

        results = new ArrayList<String>();

        if (savedInstanceState != null) {
            results = savedInstanceState.getStringArrayList("results");
            ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(getApplicationContext(), R.layout.result, results);
            ListView result_list = (ListView)findViewById(android.R.id.list);
            result_list.setAdapter(itemsAdapter);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList("results", results);
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
}
