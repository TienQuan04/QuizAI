package com.example.QuizzAI;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "quiz.db";

    // 🔥 TĂNG VERSION KHI THAY ĐỔI DATABASE
    private static final int DB_VERSION = 17;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // ================= USERS =================
        db.execSQL("CREATE TABLE users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE," +
                "email TEXT," +
                "password TEXT," +
                "created_at INTEGER," +
                "status INTEGER DEFAULT 1)");

        // ================= QUIZ HISTORY =================
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

        // ================= QUIZ DETAIL =================
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

        // ================= REQUEST SYSTEM =================
        db.execSQL("CREATE TABLE unlock_requests (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "message TEXT," +
                "status INTEGER DEFAULT 0," +
                "admin_note TEXT," +
                "created_at INTEGER)");

        // ================= QUESTIONS =================
        db.execSQL("CREATE TABLE questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "question TEXT," +
                "optionA TEXT," +
                "optionB TEXT," +
                "optionC TEXT," +
                "optionD TEXT," +
                "correct INTEGER," +
                "subject TEXT DEFAULT 'Chung'," + // 🔥 THÊM CỘT SUBJECT
                "source TEXT DEFAULT 'AI'," +
                "status INTEGER DEFAULT 0," +
                "updated_by_admin INTEGER DEFAULT 0," +
                "deleted_by_admin INTEGER DEFAULT 0," +
                "updated_at INTEGER DEFAULT 0)");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if(oldVersion < 5){

            db.execSQL("CREATE TABLE IF NOT EXISTS unlock_requests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "message TEXT," +
                    "status INTEGER DEFAULT 0," +
                    "admin_note TEXT," +
                    "created_at INTEGER)");

            return;
        }

        db.execSQL("DROP TABLE IF EXISTS quiz_detail");
        db.execSQL("DROP TABLE IF EXISTS quiz_history");
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS unlock_requests");
        db.execSQL("DROP TABLE IF EXISTS questions");

        onCreate(db);
    }

    // ================= REGISTER =================

    public long insertUser(String username,
                           String email,
                           String password) {

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

        if(username.equals("admin") && password.equals("123456")){
            return 9999;
        }

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT user_id, status FROM users WHERE username=? AND password=?",
                new String[]{username, password}
        );

        if(cursor.moveToFirst()){

            int id = cursor.getInt(0);
            int status = cursor.getInt(1);

            cursor.close();

            if(status == 0){
                return -2;
            }

            return id;
        }

        cursor.close();

        return -1;
    }

    // ================= USER ADMIN =================

    public Cursor getAllUsers() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT user_id, username, email, status FROM users",
                null
        );
    }

    public void deleteUser(int userId) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(
                "users",
                "user_id=?",
                new String[]{String.valueOf(userId)}
        );
    }

    public void blockUser(int userId){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("status", 0);

        db.update(
                "users",
                cv,
                "user_id=?",
                new String[]{String.valueOf(userId)}
        );
    }

    public void unblockUser(int userId){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("status", 1);

        db.update(
                "users",
                cv,
                "user_id=?",
                new String[]{String.valueOf(userId)}
        );
    }

    // ================= HISTORY =================

    public Cursor getAllHistoryByUser(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM quiz_history WHERE user_id=? ORDER BY id DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    // ================= USER STAT =================

    public int getTotalQuiz(int userId) {

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM quiz_history WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );

        int total = 0;

        if(cursor.moveToFirst()){
            total = cursor.getInt(0);
        }

        cursor.close();

        return total;
    }

    public int getTotalCorrect(int userId) {

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT SUM(correct_count) FROM quiz_history WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );

        int total = 0;

        if(cursor.moveToFirst() && !cursor.isNull(0)){
            total = cursor.getInt(0);
        }

        cursor.close();

        return total;
    }

    public int getTotalQuestion(int userId) {

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT SUM(total) FROM quiz_history WHERE user_id=?",
                new String[]{String.valueOf(userId)}
        );

        int total = 0;

        if(cursor.moveToFirst() && !cursor.isNull(0)){
            total = cursor.getInt(0);
        }

        cursor.close();

        return total;
    }

    // ================= INSERT HISTORY =================

    public long insertHistory(int userId,
                              String subject,
                              int score,
                              int total,
                              int correct,
                              int wrong,
                              int skip){

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

    public void insertDetail(long historyId, Question q){

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

        int isCorrect =
                (q.userAnswer == q.answerIndex) ? 1 : 0;

        cv.put("is_correct", isCorrect);

        db.insert("quiz_detail", null, cv);
    }

    // ================= ADMIN DASHBOARD =================

    public int getTotalUsers() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM users",
                null
        );

        int total = 0;

        if(cursor.moveToFirst()){
            total = cursor.getInt(0);
        }

        cursor.close();

        return total;
    }

    public int getTotalQuizzes() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM quiz_history",
                null
        );

        int total = 0;

        if(cursor.moveToFirst()){
            total = cursor.getInt(0);
        }

        cursor.close();

        return total;
    }

    public int getTotalQuestionsAdmin() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM questions",
                null
        );

        int total = 0;

        if(cursor.moveToFirst()){
            total = cursor.getInt(0);
        }

        cursor.close();

        return total;
    }

    public ArrayList<Integer> getQuizLast7Days() {

        ArrayList<Integer> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        long now = System.currentTimeMillis();

        for (int i = 6; i >= 0; i--) {

            long start = now - (i * 86400000L);
            long end = start + 86400000L;

            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM quiz_history WHERE date BETWEEN ? AND ?",
                    new String[]{
                            String.valueOf(start),
                            String.valueOf(end)
                    }
            );

            int count = 0;

            if(cursor.moveToFirst()){
                count = cursor.getInt(0);
            }

            list.add(count);

            cursor.close();
        }

        return list;
    }

    // ================= REQUEST COUNT =================

    public int getPendingRequestCount(){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM unlock_requests WHERE status=0",
                null
        );

        int total = 0;

        if(c.moveToFirst()){
            total = c.getInt(0);
        }

        c.close();

        return total;
    }

    // ================= REQUEST SYSTEM =================

    public boolean sendUnlockRequest(int userId, String message){

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM unlock_requests WHERE user_id=? AND status=0",
                new String[]{String.valueOf(userId)}
        );

        if(c.moveToFirst() && c.getInt(0) > 0){

            c.close();

            return false;
        }

        c.close();

        ContentValues cv = new ContentValues();

        cv.put("user_id", userId);
        cv.put("message", message);
        cv.put("created_at", System.currentTimeMillis());

        getWritableDatabase().insert(
                "unlock_requests",
                null,
                cv
        );

        return true;
    }

    // ================= APPROVE REQUEST =================

    public void approveRequest(int id, int userId){

        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("status", 1);

        db.update(
                "unlock_requests",
                cv,
                "id=?",
                new String[]{String.valueOf(id)}
        );

        ContentValues user = new ContentValues();

        user.put("status", 1);

        db.update(
                "users",
                user,
                "user_id=?",
                new String[]{String.valueOf(userId)}
        );
    }

    // ================= REJECT REQUEST =================

    public void rejectRequest(int id, String reason){

        ContentValues cv = new ContentValues();

        cv.put("status", 2);
        cv.put("admin_note", reason);

        getWritableDatabase().update(
                "unlock_requests",
                cv,
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    // ================= GET LATEST REQUEST STATUS =================

    public Cursor getLatestRequest(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT status, admin_note FROM unlock_requests WHERE user_id=? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(userId)}
        );
    }

    public Cursor getAllRequests(){

        return getReadableDatabase().rawQuery(
                "SELECT r.id, u.username, r.message, r.status, r.user_id, r.admin_note " +
                        "FROM unlock_requests r " +
                        "JOIN users u ON r.user_id = u.user_id " +
                        "ORDER BY r.id DESC",
                null
        );
    }

    // ================= ADMIN RESULT MANAGEMENT =================

    public Cursor getAllResultsAdmin(String searchQuery, String subjectFilter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT h.id, u.username, h.subject, h.score, h.total, h.date " +
                "FROM quiz_history h " +
                "JOIN users u ON h.user_id = u.user_id WHERE 1=1";

        ArrayList<String> args = new ArrayList<>();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            query += " AND u.username LIKE ?";
            args.add("%" + searchQuery + "%");
        }

        if (subjectFilter != null && !subjectFilter.equals("Tất cả")) {
            query += " AND h.subject = ?";
            args.add(subjectFilter);
        }

        query += " ORDER BY h.id DESC";

        return db.rawQuery(query, args.toArray(new String[0]));
    }

    public void deleteHistory(int historyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("quiz_history", "id=?", new String[]{String.valueOf(historyId)});
    }

    public ArrayList<String> getAllSubjects() {
        ArrayList<String> subjects = new ArrayList<>();
        subjects.add("Tất cả");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT subject FROM quiz_history", null);
        while (c.moveToNext()) {
            subjects.add(c.getString(0));
        }
        c.close();
        return subjects;
    }

    // ================= SYSTEM MANAGEMENT =================

    public int getTableRowCount(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public void clearTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, null, null);
    }

    public void resetSystem() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("quiz_detail", null, null);
        db.delete("quiz_history", null, null);
        db.delete("unlock_requests", null, null);
        db.delete("questions", null, null);
        // Xóa user nhưng giữ lại admin nếu có cơ chế phân quyền (hiện tại xóa hết trừ logic check login admin)
        db.delete("users", null, null);
    }

    // ================= INSERT QUESTION =================

    public boolean insertQuestion(String q,
                                  String A,
                                  String B,
                                  String C,
                                  String D,
                                  int correct,
                                  String subject, // 🔥 THÊM SUBJECT
                                  String source){

        SQLiteDatabase db = getWritableDatabase();

        // CHECK TRÙNG
        Cursor check = db.rawQuery(
                "SELECT id FROM questions WHERE question=?",
                new String[]{q}
        );

        boolean exists = check.moveToFirst();

        check.close();

        if(exists){
            return false;
        }

        ContentValues cv = new ContentValues();

        cv.put("question", q);

        cv.put("optionA", A);
        cv.put("optionB", B);
        cv.put("optionC", C);
        cv.put("optionD", D);

        cv.put("correct", correct);

        cv.put("subject", subject); // 🔥 LƯU SUBJECT

        cv.put("source", source);

        cv.put("updated_at", System.currentTimeMillis());

        // 🔥 AUTO APPROVE
        cv.put("status", 1);

        long result = db.insert(
                "questions",
                null,
                cv
        );

        return result != -1;
    }

    // ================= GET ALL QUESTION =================

    public Cursor getAllQuestions(){

        return getReadableDatabase().rawQuery(
                "SELECT * FROM questions ORDER BY updated_at DESC",
                null
        );
    }

    // ================= WRONG QUESTION =================

    public Cursor getWrongQuestions(){

        return getReadableDatabase().rawQuery(

                "SELECT question, COUNT(*) as totalWrong " +
                        "FROM quiz_detail " +
                        "WHERE is_correct=0 " +
                        "GROUP BY question " +
                        "ORDER BY totalWrong DESC",

                null
        );
    }

    // ================= GET AI QUESTION =================

    public Cursor getAIQuestions(){

        return getReadableDatabase().rawQuery(
                "SELECT * FROM questions " +
                        "WHERE source='AI' " +
                        "AND deleted_by_admin=0 " +
                        "ORDER BY updated_at DESC",
                null
        );
    }

    // ================= APPROVE QUESTION =================

    public void approveQuestion(int id){

        ContentValues cv = new ContentValues();

        cv.put("status", 1);

        cv.put("updated_at", System.currentTimeMillis());

        getWritableDatabase().update(
                "questions",
                cv,
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    // ================= UPDATE QUESTION =================

    public boolean updateQuestion(int id,
                                  String q,
                                  String A,
                                  String B,
                                  String C,
                                  String D,
                                  int correct,
                                  String subject){ // 🔥 THÊM SUBJECT

        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("question", q);

        cv.put("optionA", A);
        cv.put("optionB", B);
        cv.put("optionC", C);
        cv.put("optionD", D);

        cv.put("correct", correct);

        cv.put("subject", subject); // 🔥 CẬP NHẬT SUBJECT

        cv.put("updated_by_admin", 1);

        cv.put("updated_at", System.currentTimeMillis());

        int row = db.update(
                "questions",
                cv,
                "id=?",
                new String[]{String.valueOf(id)}
        );

        return row > 0;
    }

    // ================= DELETE QUESTION =================

    public boolean deleteQuestion(int id){

        ContentValues cv = new ContentValues();

        cv.put("deleted_by_admin", 1);

        cv.put("updated_at", System.currentTimeMillis());

        int row = getWritableDatabase().update(
                "questions",
                cv,
                "id=?",
                new String[]{String.valueOf(id)}
        );

        return row > 0;
    }

    // ================= CHECK UPDATED =================

    public boolean isQuestionUpdated(String question){

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT updated_by_admin FROM questions WHERE question=?",
                new String[]{question}
        );

        boolean updated = false;

        if(c.moveToFirst()){
            updated = c.getInt(0) == 1;
        }

        c.close();

        return updated;
    }

    // 🔥 THÊM HÀM LẤY ĐÁP ÁN MỚI NHẤT CỦA ADMIN
    public int getLatestCorrectAnswer(String questionText) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT correct FROM questions WHERE question = ?", new String[]{questionText});
        int correct = -1;
        if (c.moveToFirst()) {
            correct = c.getInt(0);
        }
        c.close();
        return correct;
    }

    // ================= CHECK DELETED =================

    public boolean isQuestionDeleted(String question){

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT deleted_by_admin FROM questions WHERE question=?",
                new String[]{question}
        );

        boolean deleted = false;

        if(c.moveToFirst()){
            deleted = c.getInt(0) == 1;
        }

        c.close();

        return deleted;
    }
/////gemini xoa
/////gemini xoa
public void deleteAIQuestions() {

    SQLiteDatabase db =
            this.getWritableDatabase();

    db.delete(
            "questions",
            "source=?",
            new String[]{"AI"}
    );

    db.close();
}
    // ================= QUIZ DETAIL =================

    public Cursor getDetailByHistoryId(int historyId){

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM quiz_detail WHERE history_id=?",
                new String[]{String.valueOf(historyId)}
        );
    }

    // ================= USER LOAD QUESTION =================

    public ArrayList<Question> getApprovedQuestions(String subject){ // 🔥 LỌC THEO SUBJECT

        ArrayList<Question> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(

                "SELECT * FROM questions " +
                        "WHERE status = 1 " +
                        "AND deleted_by_admin = 0 " +
                        "AND subject LIKE ? " + // 🔥 LỌC LIKE ĐỂ TÌM TƯƠNG ĐỐI
                        "ORDER BY updated_at DESC",

                new String[]{"%" + subject + "%"}
        );

        while(c.moveToNext()){

            Question q = new Question();

            q.question = c.getString(
                    c.getColumnIndexOrThrow("question")
            );

            q.options = new ArrayList<>();

            q.options.add(
                    c.getString(
                            c.getColumnIndexOrThrow("optionA")
                    )
            );

            q.options.add(
                    c.getString(
                            c.getColumnIndexOrThrow("optionB")
                    )
            );

            q.options.add(
                    c.getString(
                            c.getColumnIndexOrThrow("optionC")
                    )
            );

            q.options.add(
                    c.getString(
                            c.getColumnIndexOrThrow("optionD")
                    )
            );

            q.answerIndex = c.getInt(
                    c.getColumnIndexOrThrow("correct")
            );

            list.add(q);
        }

        c.close();

        return list;
    }
}