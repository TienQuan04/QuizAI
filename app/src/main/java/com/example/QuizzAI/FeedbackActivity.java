package com.example.QuizzAI;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class FeedbackActivity extends AppCompatActivity {

    TextView tvFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        tvFeedback = findViewById(R.id.tvFeedback);

        List<Question> questions = QuizManager.questions;

        GeminiApi.generateFeedback(questions, result -> {
            tvFeedback.setText(Html.fromHtml(formatFeedback(result)));
        });
    }

    private String formatFeedback(String text) {
        text = text.replace("📊 ĐÁNH GIÁ CHUNG:", "<font color='#2196F3'><b>📊 ĐÁNH GIÁ CHUNG:</b></font><br>");
        text = text.replace("✅ ĐIỂM MẠNH:", "<font color='#4CAF50'><b>✅ ĐIỂM MẠNH:</b></font><br>");
        text = text.replace("❌ ĐIỂM YẾU:", "<font color='#F44336'><b>❌ ĐIỂM YẾU:</b></font><br>");
        text = text.replace("📚 CẦN ÔN TẬP:", "<font color='#FF9800'><b>📚 CẦN ÔN TẬP:</b></font><br>");
        text = text.replace("💡 GỢI Ý CẢI THIỆN:", "<font color='#9C27B0'><b>💡 GỢI Ý CẢI THIỆN:</b></font><br>");
        text = text.replace("🎯 KẾT LUẬN:", "<font color='#009688'><b>🎯 KẾT LUẬN:</b></font><br>");

        text = text.replace("\n", "<br>");
        return text;
    }

}
