package com.example.QuizzAI;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private LinearLayout container;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        container = findViewById(R.id.container);
        db = new DatabaseHelper(this);

        int historyId = getIntent().getIntExtra("historyId", -1);
        loadDetail(historyId);
    }

    private void loadDetail(int historyId) {
        Cursor c = db.getDetailByHistoryId(historyId);

        int i = 1;

        while (c.moveToNext()) {

            String question = c.getString(c.getColumnIndexOrThrow("question"));
            String A = c.getString(c.getColumnIndexOrThrow("optionA"));
            String B = c.getString(c.getColumnIndexOrThrow("optionB"));
            String C = c.getString(c.getColumnIndexOrThrow("optionC"));
            String D = c.getString(c.getColumnIndexOrThrow("optionD"));

            int correct = c.getInt(c.getColumnIndexOrThrow("correct_answer"));
            int user = c.getInt(c.getColumnIndexOrThrow("user_answer"));

            // ✅ FIX: chỉ +1 cho correct
            correct += 1;

            // ❗ giữ nguyên -1 nếu không trả lời
            if (user != -1) {
                user += 1;
            }

            // ===== Câu hỏi =====
            TextView tvQ = new TextView(this);
            tvQ.setText("Câu " + i + ": " + question);
            tvQ.setTextSize(18);
            tvQ.setPadding(0, 30, 0, 20);
            container.addView(tvQ);

            // ===== Đáp án =====
            addOption("A", A, 1, correct, user);
            addOption("B", B, 2, correct, user);
            addOption("C", C, 3, correct, user);
            addOption("D", D, 4, correct, user);

            // 🔥 HIỂN THỊ TRẠNG THÁI KHÔNG TRẢ LỜI
            if (user == -1) {
                TextView tvStatus = new TextView(this);
                tvStatus.setText("⚠ Bạn chưa trả lời câu này");
                tvStatus.setTextColor(Color.GRAY);
                tvStatus.setPadding(20, 10, 0, 20);
                container.addView(tvStatus);
            }

            i++;
        }

        c.close();
    }

    private void addOption(String label, String text, int index, int correct, int user) {

        TextView tv = new TextView(this);
        tv.setText(label + ". " + text);
        tv.setTextSize(16);
        tv.setPadding(40, 25, 40, 25);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(0, 10, 0, 10);
        tv.setLayoutParams(params);

        // nền mặc định
        tv.setBackgroundResource(R.drawable.bg_option);

        // ✅ ĐÁP ÁN ĐÚNG
        if (index == correct) {
            tv.setBackgroundResource(R.drawable.bg_correct);
            tv.setText("✔ " + label + ". " + text);
        }

        // ❌ TRẢ LỜI SAI
        else if (user != -1 && index == user) {
            tv.setBackgroundResource(R.drawable.bg_wrong);
            tv.setText("✘ " + label + ". " + text);
        }

        // ⚠️ KHÔNG TRẢ LỜI
        else if (user == -1) {
            tv.setBackgroundResource(R.drawable.bg_option_gray);
        }

        container.addView(tv);
    }
}
