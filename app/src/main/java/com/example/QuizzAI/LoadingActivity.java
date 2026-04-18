package com.example.QuizzAI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class LoadingActivity extends AppCompatActivity {

    ProgressBar progressBar;
    TextView loadingText;

    String subject;
    int count, time;

    boolean isGeneratingFeedback = false;
    int score, total;
    ArrayList<Question> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        progressBar = findViewById(R.id.progressBar);
        loadingText = findViewById(R.id.loadingText);

        // 🔥 kiểm tra mode
        isGeneratingFeedback = getIntent().getBooleanExtra("isGeneratingFeedback", false);

        if (isGeneratingFeedback) {

            score = getIntent().getIntExtra("score", 0);
            total = getIntent().getIntExtra("total", 0);
            questions = getIntent().getParcelableArrayListExtra("questions");

            // 🔥 FIX: lấy lại subject nếu có
            subject = getIntent().getStringExtra("subject");

            loadingText.setText("Đang phân tích kết quả bài kiểm tra...");

            GeminiApi.generateFeedback(questions, new GeminiApi.OnFeedbackReady() {
                @Override
                public void onReady(String feedback) {

                    Intent intent = new Intent(LoadingActivity.this, ResultActivity.class);

                    intent.putExtra("score", score);
                    intent.putExtra("total", total);
                    intent.putParcelableArrayListExtra("questions", questions);
                    intent.putExtra("feedback", feedback);

                    // 🔥 QUAN TRỌNG: truyền subject tiếp
                    intent.putExtra("subject", subject);

                    startActivity(intent);
                    finish();
                }
            });

        } else {

            // 🔥 NHẬN DATA TỪ MAIN
            subject = getIntent().getStringExtra("subject");
            count = getIntent().getIntExtra("count", 5);
            time = getIntent().getIntExtra("time", 10);

            // 🔥 DEBUG
            Log.d("LOADING_SUBJECT", "Subject = " + subject);

            loadingText.setText("Đang tạo câu hỏi cho bạn...");

            GeminiApi.generateQuestions(subject, count, new GeminiApi.OnQuestionsReady() {
                @Override
                public void onReady(List<Question> questions) {

                    Intent intent = new Intent(LoadingActivity.this, QuizActivity.class);

                    // 🔥 QUAN TRỌNG NHẤT
                    intent.putExtra("subject", subject);
                    intent.putExtra("count", count);
                    intent.putExtra("time", time);

                    // 🔥 DEBUG
                    Log.d("SEND_SUBJECT", "Send = " + subject);

                    QuizDataHolder.questions = questions;

                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}
