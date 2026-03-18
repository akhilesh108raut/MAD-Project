package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GameApp.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_GAME = "game_sessions";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // USERS TABLE
        db.execSQL("CREATE TABLE " + TABLE_USERS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT," +
                "email TEXT UNIQUE," +
                "password TEXT)");

        // GAME SESSIONS TABLE
        db.execSQL("CREATE TABLE " + TABLE_GAME + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT," +
                "game_name TEXT," +
                "score INTEGER," +
                "reaction_time INTEGER," +
                "date_played TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME);
        onCreate(db);
    }

    // ==============================
    // USER METHODS
    // ==============================

    public boolean registerUser(String username, String email, String password) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("password", password);

        long result = db.insert(TABLE_USERS, null, values);

        return result != -1;
    }

    public boolean checkLogin(String email, String password) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE email=? AND password=?",
                new String[]{email, password});

        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }

    public String getUsername(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT username FROM " + TABLE_USERS + " WHERE email=?",
                new String[]{email});

        String username = "Player";

        if (cursor.moveToFirst()) {
            username = cursor.getString(0);
        }

        cursor.close();
        return username;
    }

    // ==============================
    // GAME METHODS
    // ==============================

    public void saveGameResult(String email,
                               String gameName,
                               int score,
                               int reactionTime) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("game_name", gameName);
        values.put("score", score);
        values.put("reaction_time", reactionTime);
        values.put("date_played", System.currentTimeMillis());

        db.insert(TABLE_GAME, null, values);
    }

    public int getTotalGames(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_GAME + " WHERE email=?",
                new String[]{email});

        int total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }

        cursor.close();
        return total;
    }

    public Cursor getHighestScores(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT game_name, MAX(score) as high_score " +
                        "FROM " + TABLE_GAME +
                        " WHERE email=? GROUP BY game_name",
                new String[]{email});
    }

    public double getAverageReactionTime(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT AVG(reaction_time) FROM " + TABLE_GAME +
                        " WHERE email=?",
                new String[]{email});

        double avg = 0;

        if (cursor.moveToFirst()) {
            avg = cursor.getDouble(0);
        }

        cursor.close();
        return avg;
    }
}