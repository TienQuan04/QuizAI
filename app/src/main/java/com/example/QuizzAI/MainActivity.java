package com.example.QuizzAI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText subjectInput;
    TextView questionCountText, timerInput;
    SeekBar questionCountSeekBar, timerSeekBar;
    Button generateBtn, historyBtn, profileBtn;

    public static final String PREF_NAME = "USER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ CHECK LOGIN (ĐÃ FIX)
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLogin = pref.getBoolean("isLogin", false);

        if (!isLogin) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Ánh xạ view
        subjectInput = findViewById(R.id.subjectInput);
        questionCountText = findViewById(R.id.questionCountText);
        timerInput = findViewById(R.id.timerInput);
        questionCountSeekBar = findViewById(R.id.questionCountSeekBar);
        timerSeekBar = findViewById(R.id.timerSeekBar);
        generateBtn = findViewById(R.id.generateBtn);
        historyBtn = findViewById(R.id.historyBtn);
        profileBtn = findViewById(R.id.profileBtn);

        // ================= PROFILE =================
        profileBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        // ================= HISTORY =================
        historyBtn.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );

        // ================= QUESTION =================
        questionCountSeekBar.setMax(27);
        questionCountSeekBar.setProgress(7);
        questionCountText.setText("10 câu hỏi");

        questionCountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int count = progress + 3;
                questionCountText.setText(count + " câu hỏi");
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // ================= TIMER =================
        timerSeekBar.setMax(60);
        timerSeekBar.setProgress(30);
        timerInput.setText("30 giây");

        timerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int time = Math.max(progress, 5);
                timerInput.setText(time + " giây");
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // ================= GENERATE =================
        generateBtn.setOnClickListener(v -> {

            String subject = subjectInput.getText().toString().trim();

            if (subject.isEmpty()) {
                subjectInput.setError("Nhập chủ đề");
                return;
            }

            int count = questionCountSeekBar.getProgress() + 3;
            int time = Math.max(timerSeekBar.getProgress(), 5);

            Intent intent = new Intent(this, LoadingActivity.class);
            intent.putExtra("subject", subject);
            intent.putExtra("count", count);
            intent.putExtra("time", time);

            startActivity(intent);
        });
    }
}
