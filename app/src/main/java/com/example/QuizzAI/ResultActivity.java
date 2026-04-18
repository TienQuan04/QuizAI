package com.example.QuizzAI;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private ImageView star1, star2, star3;
    private List<Question> questions;

    String subject; // 🔥 THÊM

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 1);
        questions = getIntent().getParcelableArrayListExtra("questions");
        String feedback = getIntent().getStringExtra("feedback");

        // 🔥 LẤY SUBJECT
        subject = getIntent().getStringExtra("subject");

        TextView tvScore = findViewById(R.id.tvScore);
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        ImageButton closeButton = findViewById(R.id.closeButton);
        TextView tvResult = findViewById(R.id.tvResult);

        if (total == 0) total = 1;
        tvScore.setText(getString(R.string.score_format, score, total));

        float percentage = (float) score / total;
        if (percentage == 1f) {
            setStars(R.drawable.win_star, R.drawable.win_star, R.drawable.win_star);
            tvResult.setText(R.string.result_perfect);
        } else if (percentage >= 0.5f) {
            setStars(R.drawable.win_star, R.drawable.win_star, R.drawable.lose_star);
            tvResult.setText(R.string.result_good);
        } else if (percentage >= 0.25f) {
            setStars(R.drawable.win_star, R.drawable.lose_star, R.drawable.lose_star);
            tvResult.setText(R.string.result_medium);
        } else {
            setStars(R.drawable.lose_star, R.drawable.lose_star, R.drawable.lose_star);
            tvResult.setText(R.string.result_low);
        }

        // ================= DATABASE =================
        saveHistory(subject, score, total); // 🔥 FIX

        // ================= BUTTON =================
        findViewById(R.id.btnPlayAgain).setOnClickListener(v -> goToMain());
        closeButton.setOnClickListener(v -> goToMain());

        // ================= FEEDBACK =================
        findViewById(R.id.viewFeedbackBtn).setOnClickListener(v -> showFeedback(feedback));
    }

    private void setStars(int s1, int s2, int s3) {
        star1.setImageResource(s1);
        star2.setImageResource(s2);
        star3.setImageResource(s3);
    }

    private void goToMain() {
        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showFeedbackDialog(String feedback) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.feedback_title)
                .setMessage(feedback)
                .setPositiveButton("OK", null)
                .show();
    }

    // 🔥 FIX CHUẨN Ở ĐÂY
    private void saveHistory(String subject, int score, int total) {
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
            int userId = sp.getInt("userId", -1);

            if (userId == -1) {
                Log.e("DB", "❌ userId = -1 (chưa login)");
                return;
            }

            // 🔥 TRÁNH NULL
            if (subject == null || subject.trim().isEmpty()) {
                subject = "Unknown";
            }

            // Calculate correct, wrong, skip
            int wrong = total - score;
            int skip = 0;

            long historyId = db.insertHistory(userId, subject, score, total, score, wrong, skip);

            if (historyId != -1 && questions != null) {
                for (Question q : questions) {
                    if (q.userAnswer < 0) q.userAnswer = -1;
                    db.insertDetail(historyId, q);
                }
                Log.d("DB", "✅ Save history + detail OK");
            } else {
                Log.d("DB", "❌ Save history failed or questions null");
            }

        } catch (Exception e) {
            Log.e("DB_ERROR", "Insert DB error: " + e.getMessage());
        }
    }

    private void showFeedback(String feedback) {
        if (feedback != null && !feedback.isEmpty()) {
            showFeedbackDialog(feedback);
        } else if (questions != null && !questions.isEmpty()) {

            AlertDialog progressDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.feedback_generating)
                    .setCancelable(false)
                    .show();

            GeminiApi.generateFeedback(questions, generatedFeedback ->
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showFeedbackDialog(generatedFeedback);
                }));

        } else {
            showFeedbackDialog(getString(R.string.feedback_no_questions));
        }
    }
}
