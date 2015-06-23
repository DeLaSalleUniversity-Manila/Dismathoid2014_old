/****************************************************************************************/
/*
        The MIT License (MIT)

        Copyright (c) Melvin Cabatuan

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.

/****************************************************************************************/

package com.cabatuan.dismathoid2014;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends QuizActivity {

    /// SETTINGS: Retrieve the shared preferences
    SharedPreferences mySettings;
    private boolean isShowAnswer = false;   //get answer key
    private boolean isShowHelper = true;    //current answer helper
    private boolean isPlaySound = true;    //play sound
    private boolean isShowFeedback = false; //Cheat mode
    private String limit = "12"; // Default

    /// DATABASE: Database cursor
    private Cursor cursor;
    private MyDatabase db;

    /// EXPANDABLE LIST VIEW
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    private HashMap<String, List<String>> listDataChildKey;
    private List<Question> questionList = new ArrayList<Question>();
    private List<Integer> answerKey = new ArrayList<Integer>();
    private List<String> answers = new ArrayList<String>();

    /// QUIZ variables
    private int numberOfQuestions;
    private final String DEFAULT_MAX_QUESTIONS = "100"; /// 100 questions if Max is exceeded
    private int maxQuestions;
    private int score[];


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /// 1. Initialize layout and actionbar/title bar on top
        centerActionBarTitle(); /// centers the title bar
        ActionBar actionBar = getActionBar();
        //actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        setContentView(R.layout.activity_main);

        /// 2. Initialize my settings
        mySettings = getSharedPreferences(GAME_PREFERENCES, Context.MODE_PRIVATE);
        isShowHelper = mySettings.getBoolean(GAME_PREFERENCES_HELPER, true);
        isPlaySound = mySettings.getBoolean(GAME_PREFERENCES_SOUND, true);
        /// Store the edit text string to temp and parse its integer value for the number of questions
        limit = mySettings.getString(GAME_PREFERENCES_QCOUNT, "12");

        if (limit == "") {
            numberOfQuestions = Integer.parseInt("12"); //Default
            limit = "12"; // String value of numberOfQuestions
        } else {
            numberOfQuestions = Integer.parseInt(limit);
        }


        ///  Acquire questions from the database
        db = new MyDatabase(this);
        handleIntent(getIntent()); /// Handle Search else
        acquireAllQuestionsFromDB();

        /// 3. Initialize quiz variables
        initQuiz();

        /// 4. Expandable list view
        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        prepareListData();
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);

        /// 5. Handle click events
        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + listDataHeader.get(groupPosition),
                // Toast.LENGTH_SHORT).show();

                return false;
            }
        });

        if (isShowHelper) {
            // Listview Group expanded listener
            expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                @Override
                public void onGroupExpand(int groupPosition) {


                    if (isPlaySound && !isShowAnswer) {
                        //playClick();
                        playSound("Bird.mp3");
                    }

                    // Log.d("MELVIN: Expand", "groupPosition = " + groupPosition);

                    if (!isShowAnswer) {
                        String msg;
                        if (answers.get(groupPosition) != null) {
                            msg = (groupPosition + 1) + ". " + answers.get(groupPosition);
                        } else {
                            msg = (groupPosition + 1) + ". ?";
                        }
                        showCurrentAnswer(msg);
                    }
                }
            });

            // Listview Group collapsed listener
            expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

                @Override
                public void onGroupCollapse(int groupPosition) {

                    if (isPlaySound && !isShowAnswer) {
                        //playClick();
                        playSound("Bird.mp3");
                    }


                    // Log.d("MELVIN: Collapse", "groupPosition = " + groupPosition);

                    if (!isShowAnswer) {
                        String msg;
                        if (answers.get(groupPosition) != null) {
                            msg = (groupPosition + 1) + ". " + answers.get(groupPosition);
                        } else {
                            msg = (groupPosition + 1) + ". ??";
                        }
                        showCurrentAnswer(msg);
                    }
                }
            });

        } /// EndOfShowHelper


        /// Limit to one choice at a time
        expListView.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                /// TODO Auto-generated method stub
                //Log.d("MELVIN:ChildClick", "groupPosition = " + groupPosition + ", childPosition = " + childPosition);

                if (isPlaySound) {
                    playSound("ClickSound.mp3");
                }

                ///Set the current answers
                answers.set(groupPosition, listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition));

                //v.setSelected(true);

                /// Selection stays highlighted
                int index = parent.getFlatListPosition(ExpandableListView
                        .getPackedPositionForChild(groupPosition, childPosition));
                parent.setItemChecked(index, !parent.isItemChecked(index));

                String msg;

                // Check Answer
                if (answerKey.get(groupPosition) == childPosition) {
                    msg = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition) + " is correct!";
                    score[groupPosition] = 1; /// 1 if correct
                } else {
                    msg = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition) + " is wrong!";
                }

                isShowFeedback = mySettings.getBoolean(GAME_PREFERENCES_CHEAT, false);

                /// Do not show feedback when showing the answer key
                if (isShowFeedback && !isShowAnswer) {
                    showToast(msg);
                } else {
                    if (!isShowAnswer && answers.get(groupPosition) != null) {
                        msg = (groupPosition + 1) + ". " + answers.get(groupPosition);
                        showCurrentAnswer(msg);
                    }
                }
                return true;
            }
        });
    }


    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        clearData();
        handleIntent(intent);
    }

    private void clearData() {
        listDataHeader.clear();
        questionList.clear();
        listDataChild.clear();
        listDataChildKey.clear();
        questionList.clear();
        answerKey.clear();
        answers.clear();
    }


    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            /// Search database
            cursor = db.setSearchCursor(query, limit);
        } else {
            cursor = db.setRandomCursor(limit);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursor.close(); /// closing the cursor
        db.close(); /// closing the database
        stopService(new Intent(this, BackgroundSound.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        cursor.close(); /// closing the cursor
        db.close(); /// closing the database
        stopService(new Intent(this, BackgroundSound.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        cursor.close(); /// closing the cursor
        db.close(); /// closing the database
        stopService(new Intent(this, BackgroundSound.class));
    }

    private void playSound(String fileName) {
        final MediaPlayer mp = new MediaPlayer();

        if (mp.isPlaying()) {
            mp.stop();
            mp.reset();
        }
        try {

            AssetFileDescriptor afd;
            afd = getAssets().openFd(fileName);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        // Inflate the Layout
        View layout = inflater.inflate(R.layout.my_toast,
                (ViewGroup) findViewById(R.id.custom_toast_layout));

        TextView text = (TextView) layout.findViewById(R.id.textToShow);

        // Set the Text to show in TextView
        text.setText(message);
        text.setBackgroundColor(Color.BLACK);

        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }


    private void showCurrentAnswer(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.answer_toast,
                (ViewGroup) findViewById(R.id.custom_answer_layout));

        TextView text = (TextView) layout.findViewById(R.id.answerToShow);

        // Set the Text to show in TextView
        // text.setText(Html.fromHtml(Html.fromHtml(your_html_text).toString()));// This works
        text.setText(Html.fromHtml(message).toString());
        text.setBackgroundColor(Color.BLACK);

        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);

        toast.setView(layout);
        toast.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // I do this in onCreateOptionsMenu
        menu.findItem(R.id.action_search).collapseActionView();

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            /// new edit to be observed
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            return true;
            //finish();
        }

        if (id == R.id.action_play) {
            if (!isShowAnswer) {
                showResultDialog();
            } else {
                playAgain();
            }
            return true;
            //finish();
        }

        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this,
                    SettingsActivity.class));
            return true;
            //finish();
        }


        if (id == R.id.action_share) {
            Bitmap bitmap = takeScreenshot();
            shareIt(saveBitmap(bitmap));
            return true;
            //finish();
        }

        if (id == R.id.action_help) {
            startActivity(new Intent(MainActivity.this,
                    HelpActivity.class));
            return true;
            //finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public Bitmap takeScreenshot() {
        View rootView = findViewById(android.R.id.content).getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }

    public File saveBitmap(Bitmap bitmap) {

        File picDir  = new File(Environment.getExternalStorageDirectory()+ "/Pictures/DISMATH");
        if (!picDir.exists())
        {
            picDir.mkdir();
        }

        File imagePath = new File(picDir + "/" + "screenshot" + ".png");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            // Error
        } catch (IOException e) {
            // Error
        }
        return imagePath;
    }

    private void shareIt(File file) {
        //sharing implementation here
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sharingIntent.setType("image/*");
        Uri uri = Uri.fromFile(file);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
        file.deleteOnExit();
    }





    /*
     * Preparing the list data
	 */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        listDataChildKey = new HashMap<String, List<String>>();
        ArrayList<String> q[] = new ArrayList[numberOfQuestions];
        ArrayList<String> k[] = new ArrayList[numberOfQuestions];

        for (int i = 0; i < numberOfQuestions; i++) {

            listDataHeader.add((i + 1) + ". " + questionList.get(i).getQUESTION());

            q[i] = new ArrayList();
            q[i].add(questionList.get(i).getCorrectAnswer());
            q[i].add(questionList.get(i).getOPTA());
            q[i].add(questionList.get(i).getOPTB());
            q[i].add(questionList.get(i).getOPTC());


            Collections.shuffle(q[i]);

            // generate answer key
            for (int j = 0; j < 4; j++) {
                if (q[i].get(j) == questionList.get(i).getCorrectAnswer())
                    answerKey.add(j);
            }

            // append letters
            q[i].set(0, "A. " + q[i].get(0));
            q[i].set(1, "B. " + q[i].get(1));
            q[i].set(2, "C. " + q[i].get(2));
            q[i].set(3, "D. " + q[i].get(3));

            listDataChild.put(listDataHeader.get(i), q[i]); // Header, Child data

            /// For showAnswers() method
            k[i] = new ArrayList();
            k[i].add(q[i].get(answerKey.get(i)));
            listDataChildKey.put(listDataHeader.get(i), k[i]);
        }
    }

    // Get All Questions
    private void acquireAllQuestionsFromDB() {
        maxQuestions = 0;
        if (cursor.getCount() > 0) {


            do {
                Question quest = new Question(); // create a new Question object
                quest.setID(cursor.getInt(0));//get the question id for the cursor and set it to the object
                quest.setQUESTION(cursor.getString(1));//same here for the question and the answers
                quest.setCorrectAnswer(cursor.getString(2));
                quest.setOPTA(cursor.getString(3));
                quest.setOPTB(cursor.getString(4));
                quest.setOPTC(cursor.getString(5));
                questionList.add(quest);//finally add the question to the list
                maxQuestions++;
            } while (cursor.moveToNext()); //move to next question until you finish with all of them

            cursor.close(); /// closing the cursor
            db.close(); /// closing the database

            /// Shuffle the question list
            Collections.shuffle(questionList);

        } else {
            showToast("SORRY, NO RESULTS FOUND!");
        }
    }


    public void initQuiz() {
        if (numberOfQuestions > maxQuestions) {
            numberOfQuestions = maxQuestions;
        }
        score = new int[numberOfQuestions];
        /// Initialize answers list
        for (int i = 0; i < numberOfQuestions; i++) {
            answers.add(null);
        }
    }

    public void playAgain() {
        clearData();
        startActivity(new Intent(MainActivity.this,
                MainActivity.class));
    }


    public void showAnswers() {
        isShowAnswer = true;

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChildKey, score, numberOfQuestions);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        for (int i = 0; i < numberOfQuestions; i++) {
            expListView.expandGroup(i, true);
        }

    }

    private int computeScore() {
        int sum = 0;
        for (int i = 0; i < numberOfQuestions; i++) {
            sum = sum + score[i];
        }
        return sum;
    }

    private void showResultDialog() {

        String title, message, nickname;
        int icon;

        nickname = mySettings.getString(GAME_PREFERENCES_NICKNAME, "Guest");

        title = String.format("SCORE: \n  \t  %d/%d", computeScore(), numberOfQuestions);

        double rawGrade = (double) computeScore() / numberOfQuestions;

        if (rawGrade >= 0.95) {
            message = "Excellent " + nickname + "! \n Your grade is 4.0.";
            icon = R.drawable.splash1;
        } else if (rawGrade >= 0.90) {
            message = "Very Good " + nickname + "! \n Your grade is 3.5.";
            icon = R.drawable.splash1;
        } else if (rawGrade >= 0.85) {
            message = "Very nice " + nickname + "! \n Your grade is 3.0.";
            icon = R.drawable.splash2;
        } else if (rawGrade >= 0.80) {
            message = "Cool " + nickname + "! \n Your grade is 2.5.";
            icon = R.drawable.splash2;
        } else if (rawGrade >= 0.75) {
            message = "Good " + nickname + ", \n Your grade is 2.0.";
            icon = R.drawable.splash3;
        } else if (rawGrade >= 0.70) {
            message = "Average " + nickname + ", \n Your grade is 1.5.";
            icon = R.drawable.splash3;
        } else if (rawGrade >= 0.65) {
            message = "Mediocre " + nickname + ", \n Your grade is 1.0.";
            icon = R.drawable.splash4;
        } else {
            message = "Please try again " + nickname + ", \n YOU FAILED DISMATH!";
            icon = R.drawable.splash4;
        }

        if (isShowFeedback) {
            message = " This examination is invalidated since you cheated! Your final grade is 0.0!";
        }

        DialogFragment newFragment = ScoreDialogFragment
                .newInstance(title, message.toUpperCase(), icon);
        newFragment.show(getFragmentManager(), "dialog");
    }


//    private String unEscape(String description) {
//        return description.replaceAll("\\\\n", "\\\n");
//    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}

