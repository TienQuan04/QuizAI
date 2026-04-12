package com.example.QuizzAI;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    TextView questionText, timerText, progressText;

    RadioButton[] optionButtons = new RadioButton[4];

    ProgressBar progressBar, circularProgress;

    Button nextButton;

    List<Question> questions;

    int currentIndex = 0;
    int score = 0;
    int timePerQuestion;

    int selectedAnswerIndex = -1;

    boolean answered = false;

    CountDownTimer timer;

    ImageView[] optionIcons = new ImageView[4];

    MaterialCardView[] optionCards = new MaterialCardView[4];

    String subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quiz);

        subject = getIntent().getStringExtra("subject");

        if (subject == null || subject.trim().isEmpty()) {
            subject = "General Quiz";
        }

        // ===== VIEW =====
        questionText = findViewById(R.id.questionText);
        timerText = findViewById(R.id.timerText);
        progressText = findViewById(R.id.progressText);

        progressBar = findViewById(R.id.progressBar);
        circularProgress = findViewById(R.id.circularProgress);

        nextButton = findViewById(R.id.nextButton);

        ImageView closeButton = findViewById(R.id.closeButton);

        closeButton.setOnClickListener(v -> {

            if (timer != null) {
                timer.cancel();
            }

            Intent intent = new Intent(
                    QuizActivity.this,
                    MainActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);

            finish();
        });

        // ===== OPTION BUTTONS =====
        optionButtons[0] = findViewById(R.id.option1);
        optionButtons[1] = findViewById(R.id.option2);
        optionButtons[2] = findViewById(R.id.option3);
        optionButtons[3] = findViewById(R.id.option4);

        // ===== ICONS =====
        optionIcons[0] = findViewById(R.id.icon1);
        optionIcons[1] = findViewById(R.id.icon2);
        optionIcons[2] = findViewById(R.id.icon3);
        optionIcons[3] = findViewById(R.id.icon4);

        // ===== CARDS =====
        optionCards[0] = findViewById(R.id.card1);
        optionCards[1] = findViewById(R.id.card2);
        optionCards[2] = findViewById(R.id.card3);
        optionCards[3] = findViewById(R.id.card4);

        timePerQuestion = getIntent().getIntExtra("time", 10);

        // ===== LOAD QUESTION FROM DATABASE =====

        DatabaseHelper db = new DatabaseHelper(this);

        questions = new ArrayList<>();

        questions.clear();

        questions = db.getApprovedQuestions(subject); // 🔥 TRUYỀN SUBJECT VÀO ĐÂY

        if(questions.size() == 0){

            GeminiApi.generateQuestions(
                    subject,
                    10,
                    result -> {

                        questions.clear();
                        questions.addAll(result);

                        runOnUiThread(() -> showQuestion());
                    });

            return;
        }        // ===== KHÔNG CÓ CÂU HỎI =====
        if (questions == null || questions.size() == 0) {

            questionText.setText("Không có câu hỏi");

            nextButton.setEnabled(false);

            return;
        }

        // ===== NEXT BUTTON =====
        nextButton.setOnClickListener(v -> {

            if (!answered) return;

            if (selectedAnswerIndex ==
                    questions.get(currentIndex).answerIndex) {

                score++;
            }

            currentIndex++;

            showQuestion();
        });

        showQuestion();
    }

    void showQuestion() {

        if (timer != null) {
            timer.cancel();
        }

        if (currentIndex >= questions.size()) {

            Intent intent = new Intent(
                    QuizActivity.this,
                    ResultActivity.class
            );

            intent.putExtra("score", score);

            intent.putExtra("total", questions.size());

            intent.putExtra("subject", subject);

            intent.putParcelableArrayListExtra(
                    "questions",
                    new ArrayList<>(questions)
            );

            startActivity(intent);

            finish();

            return;
        }

        answered = false;

        selectedAnswerIndex = -1;

        nextButton.setEnabled(false);

        nextButton.setAlpha(0.5f);

        timerText.setTextColor(
                getResources().getColor(android.R.color.black)
        );

        Question q = questions.get(currentIndex);

        questionText.setText(q.question);

        for (int i = 0; i < 4; i++) {

            int index = i;

            optionButtons[i].setText(q.options.get(i));

            optionButtons[i].setChecked(false);

            optionButtons[i].setEnabled(true);

            optionCards[i].setCardBackgroundColor(
                    getResources().getColor(android.R.color.white)
            );

            optionIcons[i].setVisibility(View.GONE);

            optionButtons[i].setOnClickListener(v -> {

                if (!answered) {

                    answered = true;

                    selectedAnswerIndex = index;

                    questions.get(currentIndex).userAnswer = index;

                    highlightAnswer(
                            q.answerIndex,
                            selectedAnswerIndex
                    );

                    enableNextButton();

                    if (timer != null) {
                        timer.cancel();
                    }
                }
            });
        }

        int currentQuestionNumber = currentIndex + 1;

        progressBar.setMax(questions.size());

        progressBar.setProgress(currentQuestionNumber);

        progressText.setText(
                currentQuestionNumber + "/" + questions.size()
        );

        nextButton.setText(
                currentIndex == questions.size() - 1
                        ? "Xem kết quả"
                        : "Câu tiếp theo"
        );

        startTimer();
    }

    void highlightAnswer(int correct, int selected) {

        for (int i = 0; i < 4; i++) {

            optionIcons[i].setVisibility(View.GONE);

            optionCards[i].setCardBackgroundColor(
                    getResources().getColor(android.R.color.white)
            );

            // ===== ĐÁP ÁN ĐÚNG =====
            if (i == correct) {

                optionCards[i].setCardBackgroundColor(
                        getResources().getColor(
                                android.R.color.holo_green_light
                        )
                );

                optionIcons[i].setImageResource(R.drawable.ic_true);

                optionIcons[i].setVisibility(View.VISIBLE);
            }

            // ===== CHỌN SAI =====
            else if (i == selected) {

                optionCards[i].setCardBackgroundColor(
                        getResources().getColor(
                                android.R.color.holo_red_light
                        )
                );

                optionIcons[i].setImageResource(R.drawable.ic_false);

                optionIcons[i].setVisibility(View.VISIBLE);
            }

            // ===== HẾT GIỜ =====
            else if (selected == -1) {

                optionCards[i].setCardBackgroundColor(
                        getResources().getColor(
                                android.R.color.darker_gray
                        )
                );
            }
        }
    }

    void enableNextButton() {

        nextButton.setEnabled(true);

        nextButton.setAlpha(1.0f);
    }

    void startTimer() {

        circularProgress.setMax(timePerQuestion);

        circularProgress.setProgress(timePerQuestion);

        timer = new CountDownTimer(
                timePerQuestion * 1000L,
                1000
        ) {

            @Override
            public void onTick(long millisUntilFinished) {

                int secondsLeft =
                        (int) (millisUntilFinished / 1000);

                timerText.setText(String.valueOf(secondsLeft));

                circularProgress.setProgress(secondsLeft);
            }

            @Override
            public void onFinish() {

                answered = true;

                int correct =
                        questions.get(currentIndex).answerIndex;

                questions.get(currentIndex).userAnswer = -1;

                highlightAnswer(correct, -1);

                timerText.setText("Hết giờ!");

                timerText.setTextColor(
                        getResources().getColor(
                                android.R.color.holo_red_dark
                        )
                );

                enableNextButton();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }
    }
}