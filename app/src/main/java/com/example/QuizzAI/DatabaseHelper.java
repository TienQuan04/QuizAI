package com.example.QuizzAI;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "quiz.db";
    private static final int DB_VERSION = 3; // ⚠️ tăng version

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // ===== USERS =====
        db.execSQL("CREATE TABLE users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE," +
                "email TEXT," +
                "password TEXT," +
                "created_at INTEGER)");

        // ===== HISTORY =====
        db.execSQL("CREATE TABLE quiz_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "subject TEXT," +
                "score INTEGER," +
                "total INTEGER," +
                "correct_count INTEGER," +
                "wrong_count INTEGER," +
                "skip_count INTEGER," +
                "date INTEGER," +
                "FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE)");

        // ===== DETAIL =====
        db.execSQL("CREATE TABLE quiz_detail (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "history_id INTEGER," +
                "question TEXT," +
                "optionA TEXT," +
                "optionB TEXT," +
                "optionC TEXT," +
                "optionD TEXT," +
                "correct_answer INTEGER," +
                "user_answer INTEGER," +
                "is_correct INTEGER," +
                "FOREIGN KEY(history_id) REFERENCES quiz_history(id) ON DELETE CASCADE)");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS quiz_detail");
        db.execSQL("DROP TABLE IF EXISTS quiz_history");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // ================= REGISTER =================
    public long insertUser(String username, String email, String password) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("email", email);
        cv.put("password", password);
        cv.put("created_at", System.currentTimeMillis());

        return db.insert("users", null, cv);
    }

    // ================= CHECK USER =================
    public boolean checkUserExists(String username) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT user_id FROM users WHERE username=?",
                new String[]{username}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // ================= LOGIN =================
    public int checkLogin(String username, String password) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT user_id FROM users WHERE username=? AND password=?",
                new String[]{username, password}
        );

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }

        cursor.close();
        return -1;
    }

    // ================= INSERT HISTORY =================
    public long insertHistory(int userId, String subject,
                              int score, int total,
                              int correct, int wrong, int skip) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("subject", subject);
        cv.put("score", score);
        cv.put("total", total);
        cv.put("correct_count", correct);
        cv.put("wrong_count", wrong);
        cv.put("skip_count", skip);
        cv.put("date", System.currentTimeMillis());

        return db.insert("quiz_history", null, cv);
    }

    // ================= INSERT DETAIL =================
    public void insertDetail(long historyId, Question q) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("history_id", historyId);
        cv.put("question", q.question);

        cv.put("optionA", q.options.get(0));
        cv.put("optionB", q.options.get(1));
        cv.put("optionC", q.options.get(2));
        cv.put("optionD", q.options.get(3));

        cv.put("correct_answer", q.answerIndex);
        cv.put("user_answer", q.userAnswer);

        // ✔ tự động check đúng sai
        int isCorrect = (q.userAnswer == q.answerIndex) ? 1 : 0;
        cv.put("is_correct", isCorrect);

        db.insert("quiz_detail", null, cv);
    }

    // ================= GET HISTORY =================
    public Cursor getAllHistoryByUser(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM quiz_history WHERE user_id=? ORDER BY id DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    // ================= GET DETAIL =================
    public Cursor getDetailByHistoryId(int historyId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM quiz_detail WHERE history_id=?",
                new String[]{String.valueOf(historyId)}
        );
    }

    // ================= STATISTICS =================
    // Get total number of quizzes for current user
    public int getTotalQuiz(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM quiz_history WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    // Get total correct answers for current user
    public int getTotalCorrect(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.rawQuery(
                "SELECT SUM(correct_count) FROM quiz_history WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        
        int total = 0;
        if (cursor.moveToFirst() && cursor.getInt(0) != 0) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    // Get total questions for current user
    public int getTotalQuestion(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.rawQuery(
                "SELECT SUM(total) FROM quiz_history WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );
        
        int total = 0;
        if (cursor.moveToFirst() && cursor.getInt(0) != 0) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }
}
