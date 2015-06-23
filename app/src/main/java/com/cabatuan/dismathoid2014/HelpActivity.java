package com.cabatuan.dismathoid2014;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HelpActivity extends QuizActivity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        centerActionBarTitle();
        setContentView(R.layout.help);

        // Read raw file into string and populate TextView
        InputStream iFile = getResources().openRawResource(R.raw.quizhelp);
        try {
            TextView helpText = (TextView) findViewById(R.id.TextView_HelpText);
            String strFile = inputStreamToString(iFile);
            helpText.setText(strFile);
        } catch (Exception e) {

        }
    }

    /**
     * Converts an input stream to a string
     *
     * @param is The {@code InputStream} object to read from
     * @return A {@code String} object representing the string for of the input
     * @throws IOException Thrown on read failure from the input
     */
    public String inputStreamToString(InputStream is) throws IOException {
        StringBuffer sBuffer = new StringBuffer();
        DataInputStream dataIO = new DataInputStream(is);
        String strLine = null;

        while ((strLine = dataIO.readLine()) != null) {
            sBuffer.append(strLine + "\n");
        }

        dataIO.close();
        is.close();

        return sBuffer.toString();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_play) {
            startActivity(new Intent(HelpActivity.this,
                    MainActivity.class));
        }

        if (id == R.id.action_settings) {
            startActivity(new Intent(HelpActivity.this,
                    SettingsActivity.class));
        }
        if (id == R.id.action_help) {
            startActivity(new Intent(HelpActivity.this,
                    HelpActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
