package com.example.QuizzAI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;

import java.util.ArrayList;

public class AdminDashboardActivity extends AppCompatActivity {

    TextView txtTotalUser, txtTotalQuiz, txtTotalQuestion;
    LineChart chart;

    Button btnUser, btnQuestion, btnResult, btnSystem, btnRequest; //  NEW

    DatabaseHelper db;
    TextView txtBadge; //  NEW
    ImageView btnLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = new DatabaseHelper(this);
        txtBadge = findViewById(R.id.txtBadge);
        btnRequest = findViewById(R.id.btnRequest);
        btnLogout = findViewById(R.id.btnLogout);

        int count = db.getPendingRequestCount();

        if(count > 0){
            txtBadge.setVisibility(View.VISIBLE);
            txtBadge.setText(String.valueOf(count));
        }else{
            txtBadge.setVisibility(View.GONE);
        }
        // ===== VIEW =====
        txtTotalUser = findViewById(R.id.txtTotalUser);
        txtTotalQuiz = findViewById(R.id.txtTotalQuiz);
        txtTotalQuestion = findViewById(R.id.txtTotalQuestion);

        chart = findViewById(R.id.chart);
        txtBadge = findViewById(R.id.txtBadge); // NEW

        btnUser = findViewById(R.id.btnUser);
        btnQuestion = findViewById(R.id.btnQuestion);
        btnResult = findViewById(R.id.btnResult);
        btnSystem = findViewById(R.id.btnSystem);
        btnRequest = findViewById(R.id.btnRequest); // NEW

        // ===== LOAD DATA =====
        loadStats();
        loadChart();
        loadBadge(); // NEW

        // ===== CLICK =====
        btnUser.setOnClickListener(v ->
                startActivity(new Intent(this, UserManagementActivity.class)));

        btnQuestion.setOnClickListener(v ->
                startActivity(new Intent(this, QuestionManagementActivity.class)));

        btnResult.setOnClickListener(v ->
                startActivity(new Intent(this, AnalyticsActivity.class)));
        btnRequest.setOnClickListener(v ->
                startActivity(new Intent(this, RequestManagementActivity.class))); // NEW
        btnSystem.setOnClickListener(v ->
                startActivity(new Intent(this, SystemManagementActivity.class))); // NEW

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        getSharedPreferences("USER", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void loadStats() {

        int totalUser = db.getTotalUsers();
        int totalQuiz = db.getTotalQuizzes();
        int totalQuestion = db.getTotalQuestionsAdmin(); // ✅ FIX

        txtTotalUser.setText(String.valueOf(totalUser));
        txtTotalQuiz.setText(String.valueOf(totalQuiz));
        txtTotalQuestion.setText(String.valueOf(totalQuestion));
    }

    private void loadChart() {

        ArrayList<Integer> dataDB = db.getQuizLast7Days();

        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < dataDB.size(); i++) {
            entries.add(new Entry(i + 1, dataDB.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Số bài làm");
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawFilled(true);

        LineData data = new LineData(dataSet);

        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.animateX(1000);
        chart.invalidate();

    }

    // ===== BADGE  =====
    private void loadBadge() {
        int count = db.getPendingRequestCount();

        if (count > 0) {
            txtBadge.setText(String.valueOf(count));
            txtBadge.setVisibility(TextView.VISIBLE);
        } else {
            txtBadge.setVisibility(TextView.GONE);
        }
    }

}
