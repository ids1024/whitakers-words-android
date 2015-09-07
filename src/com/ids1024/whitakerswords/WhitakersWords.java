package com.ids1024.whitakerswords;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.RuntimeException;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.os.Bundle;

import android.util.Log;

public class WhitakersWords extends Activity
{
    /** Called when the activity is first created. */

    public void copyFiles() throws IOException {
        String[] filenames = {"ADDONS.LAT", "DICTFILE.GEN", "EWDSFILE.GEN",
                "INDXFILE.GEN", "INFLECTS.SEC", "STEMFILE.GEN",
                "UNIQUES.LAT", "words"};
        for (String filename: filenames) {
            // Note: Android does not accept upper case in resource names, so it is converted
            // And file extensions are stripped, so remove that
            String resourcename = filename.toLowerCase();
            if (resourcename.contains(".")) { 
                resourcename = resourcename.substring(0, filename.lastIndexOf('.'));
            }

            int identifier = getResources().getIdentifier(resourcename, "raw", getPackageName());
            InputStream ins = getResources().openRawResource(identifier);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            copyFiles();
        } catch(IOException e) {
                throw new RuntimeException("Copying data files failed.");
        }
        setContentView(R.layout.main);
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
            case R.id.action_search:
                // TODO
                return true;
            case R.id.action_settings:
		// TODO
                return true;
            default:
                return super.onOptionsItemSelected(item);
    }
}
}
