package com.cabatuan.dismathoid2014;

/**
 * Created by cobalt on 8/27/14.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class MyDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "DISMATH";
    private static final int DATABASE_VERSION = 1;

    // Questions table name.
    public static final String TABLE_QUESTIONS = "questions";

    // Questions table column names.
    public static final String KEY_ID = "_id";
    public static final String KEY_QUESTION = "question";
    public static final String KEY_CORRECT_OPTION = "correct_option";
    public static final String KEY_OPTION1 = "option1";
    public static final String KEY_OPTION2 = "option2";
    public static final String KEY_OPTION3 = "option3";
    public static final String KEY_CATEGORY = "category";

    private int maxQuestions;

    // Favorites table name.
    public static final String TABLE_FAVORITES = "favorites";
    // Favorites table column names.
    public static final String FAVORITES_KEY_ID = "fav_external_id";



    public MyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // you can use an alternate constructor to specify a database location
        // (such as a folder on the sd card)
        // you must ensure that this folder is available and you have permission
        // to write to it
        //super(context, DATABASE_NAME, context.getExternalFilesDir(null).getAbsolutePath(), null, DATABASE_VERSION);

        // Delete old on upgrade (not working? java 7 problems probably 9/20)
        setForcedUpgrade(DATABASE_VERSION);
    }

    public Cursor setCursor() {

        //Open database
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

         //Enter the string query. Here we want everything from our table
        String[] sqlSelect = {"0 _id", KEY_QUESTION, KEY_CORRECT_OPTION, KEY_OPTION1, KEY_OPTION2, KEY_OPTION3, KEY_CATEGORY};

        String sqlTables = TABLE_QUESTIONS;

        qb.setTables(sqlTables);
        Cursor c = qb.query(db, sqlSelect, null, null,
                null, null, null);

        c.moveToFirst();
        return c;
    }


    public Cursor setRandomCursor(String limit) {

        //Open database
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        //Enter the string query. Here we want everything from our table
        String[] sqlSelect = {"0 _id", KEY_QUESTION, KEY_CORRECT_OPTION, KEY_OPTION1, KEY_OPTION2, KEY_OPTION3, KEY_CATEGORY};

        String sqlTables = TABLE_QUESTIONS;

        qb.setTables(sqlTables);
        Cursor c = qb.query(db, sqlSelect, null, null,
                null, null, "RANDOM()", limit);

        c.moveToFirst();
        return c;
    }


    public Cursor setSearchCursor(String query, String limit) {

        //Open database
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        //Enter the string query. Here we want everything from our table
        String[] sqlSelect = {"0 _id", KEY_QUESTION, KEY_CORRECT_OPTION, KEY_OPTION1, KEY_OPTION2, KEY_OPTION3, KEY_CATEGORY};

        String escaped_query = DatabaseUtils.sqlEscapeString("%" + query + "%");
        String selectQuery = MyDatabase.KEY_QUESTION + " LIKE " + escaped_query + " OR ";
        selectQuery += MyDatabase.KEY_CATEGORY + " LIKE " + escaped_query + " OR ";
        selectQuery += MyDatabase.KEY_CORRECT_OPTION + " LIKE " + escaped_query + " OR ";
        selectQuery += MyDatabase.KEY_OPTION1 + " LIKE " + escaped_query + " OR ";
        selectQuery += MyDatabase.KEY_OPTION2 + " LIKE " + escaped_query + " OR ";
        selectQuery += MyDatabase.KEY_OPTION3 + " LIKE " + escaped_query + " ";
        String sqlTables = TABLE_QUESTIONS;

        qb.setTables(sqlTables);
        Cursor c = qb.query(db, sqlSelect, selectQuery, null,
                null, null, "RANDOM()", limit);
        c.moveToFirst();
        return c;
    }



    public int getUpgradeVersion() {

        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] sqlSelect = {"MAX (version)"};
        String sqlTables = "upgrades";

        qb.setTables(sqlTables);
        Cursor c = qb.query(db, sqlSelect, null, null,
                null, null, null);

        int v = 0;
        c.moveToFirst();
        if (!c.isAfterLast()) {
            v = c.getInt(0);
        }
        c.close();
        return v;
    }


    // Get number of events.
    public int getQuestionCount() {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor dataCount;
        if (db != null) {
            dataCount = db.rawQuery("select * from " + TABLE_QUESTIONS, null);
            count = dataCount.getCount();
            db.close();
        }
        return count;
    }



    // Get single event.
    public Question getQuestion(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        assert db != null;
        String selectQuery = "SELECT * FROM " + TABLE_QUESTIONS;
        selectQuery += " WHERE " + KEY_ID + " = " + id;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        assert cursor != null;
        Question q = new Question(
                cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4)
        );
        db.close();
        return q;
    }

}
