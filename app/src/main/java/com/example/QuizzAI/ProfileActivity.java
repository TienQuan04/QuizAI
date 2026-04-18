package com.example.QuizzAI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView totalQuizText, correctAnswersText, accuracyText, usernameText;
    private ProgressBar accuracyProgressBar;
    private Button viewHistoryBtn, logoutBtn;
    private DatabaseHelper dbHelper;

    public static final String PREF_NAME = "USER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        totalQuizText = findViewById(R.id.totalQuizText);
        correctAnswersText = findViewById(R.id.correctAnswersText);
        accuracyText = findViewById(R.id.accuracyText);
        usernameText = findViewById(R.id.usernameText);
        accuracyProgressBar = findViewById(R.id.accuracyProgressBar);
        viewHistoryBtn = findViewById(R.id.viewHistoryBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Get current user ID from SharedPreferences
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int userId = pref.getInt("userId", -1);

        if (userId != -1) {
            loadUserStatistics(userId);
            loadUsername(pref);
        }

        // View History Button
        viewHistoryBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });

        // Logout Button
        logoutBtn.setOnClickListener(v -> {
            logout(pref);
        });
    }

    private void loadUserStatistics(int userId) {
        try {
            // Get statistics from database
            int totalQuiz = dbHelper.getTotalQuiz(userId);
            int totalCorrect = dbHelper.getTotalCorrect(userId);
            int totalQuestion = dbHelper.getTotalQuestion(userId);

            // Calculate accuracy
            double accuracy = 0.0;
            if (totalQuestion > 0) {
                accuracy = (double) totalCorrect / totalQuestion * 100;
            }

            // Update UI
            totalQuizText.setText(String.valueOf(totalQuiz));
            correctAnswersText.setText(String.valueOf(totalCorrect));
            accuracyText.setText(String.format("%.2f%%", accuracy));
            accuracyProgressBar.setProgress((int) accuracy);

        } catch (Exception e) {
            e.printStackTrace();
            // Set default values in case of error
            totalQuizText.setText("0");
            correctAnswersText.setText("0");
            accuracyText.setText("0.00%");
            accuracyProgressBar.setProgress(0);
        }
    }

    private void loadUsername(SharedPreferences pref) {
        String username = pref.getString("username", "User");
        usernameText.setText(username);
    }

    private void logout(SharedPreferences pref) {
        // Clear login data
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLogin", false);
        editor.putInt("userId", -1);
        editor.putString("username", "");
        editor.apply();

        // Navigate to Login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}


