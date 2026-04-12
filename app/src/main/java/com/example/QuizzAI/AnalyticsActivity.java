package com.example.QuizzAI;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalyticsActivity extends AppCompatActivity {

    private RecyclerView recyclerResults;
    private EditText edtSearchUser;
    private Spinner spinnerSubject;
    private TextView txtTotalQuizzes, txtAvgScore;
    private PieChart pieChart;
    private View btnAIConsultant;
    
    private int excellentCount = 0, goodCount = 0, averageCount = 0, weakCount = 0;
    
    private DatabaseHelper db;
    private AdminResultAdapter adapter;
    private Cursor currentCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        db = new DatabaseHelper(this);

        // Ánh xạ
        recyclerResults = findViewById(R.id.recyclerResults);
        edtSearchUser = findViewById(R.id.edtSearchUser);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        txtTotalQuizzes = findViewById(R.id.txtTotalQuizzes);
        txtAvgScore = findViewById(R.id.txtAvgScore);
        pieChart = findViewById(R.id.pieChart);
        btnAIConsultant = findViewById(R.id.btnAIConsultant);

        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        
        setupPieChart();
        setupSpinner();
        loadData();

        btnAIConsultant.setOnClickListener(v -> showAIConsultation());

        // Search listener
        edtSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter listener
        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSpinner() {
        ArrayList<String> subjects = db.getAllSubjects();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(spinnerAdapter);
    }

    public void loadData() {
        String search = edtSearchUser.getText().toString().trim();
        String subject = spinnerSubject.getSelectedItem() != null ? spinnerSubject.getSelectedItem().toString() : "Tất cả";

        if (currentCursor != null && !currentCursor.isClosed()) {
            currentCursor.close();
        }

        currentCursor = db.getAllResultsAdmin(search, subject);

        if (adapter == null) {
            adapter = new AdminResultAdapter(this, currentCursor, db);
            recyclerResults.setAdapter(adapter);
        } else {
            adapter.updateCursor(currentCursor);
        }

        updateStats(search, subject);
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(android.R.color.white);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setCenterText("Phân loại");
        pieChart.setCenterTextSize(16f);
    }

    private void updateStats(String search, String subject) {
        int totalCount = currentCursor.getCount();
        txtTotalQuizzes.setText(String.valueOf(totalCount));

        excellentCount = 0;
        goodCount = 0;
        averageCount = 0;
        weakCount = 0;

        if (totalCount > 0) {
            float totalPercentage = 0;
            currentCursor.moveToFirst();
            do {
                int score = currentCursor.getInt(currentCursor.getColumnIndexOrThrow("score"));
                int total = currentCursor.getInt(currentCursor.getColumnIndexOrThrow("total"));
                
                if (total > 0) {
                    float ratio = (float) score / total;
                    totalPercentage += ratio;

                    if (ratio >= 0.8) excellentCount++;
                    else if (ratio >= 0.65) goodCount++;
                    else if (ratio >= 0.5) averageCount++;
                    else weakCount++;
                }
            } while (currentCursor.moveToNext());

            float avg = (totalPercentage / totalCount) * 100;
            txtAvgScore.setText(String.format(Locale.getDefault(), "%.1f%%", avg));
            
            updatePieChart(excellentCount, goodCount, averageCount, weakCount);
        } else {
            txtAvgScore.setText("0%");
            pieChart.clear();
        }
    }

    private void showAIConsultation() {
        int total = currentCursor.getCount();
        if (total == 0) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Thông báo")
                    .setMessage("Không có dữ liệu để phân tích.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String statsData = String.format(Locale.getDefault(),
                "- Tổng số bài làm: %d\n" +
                "- Điểm trung bình: %s\n" +
                "- Xếp loại Giỏi: %d bài\n" +
                "- Xếp loại Khá: %d bài\n" +
                "- Xếp loại Trung bình: %d bài\n" +
                "- Xếp loại Yếu: %d bài\n" +
                "- Môn học hiện tại: %s",
                total, txtAvgScore.getText().toString(),
                excellentCount, goodCount, averageCount, weakCount,
                spinnerSubject.getSelectedItem().toString());

        android.app.ProgressDialog progress = new android.app.ProgressDialog(this);
        progress.setTitle("AI đang phân tích...");
        progress.setMessage("Vui lòng đợi trong giây lát...");
        progress.setCancelable(false);
        progress.show();

        GeminiApi.generateAdminConsultation(statsData, feedback -> {
            runOnUiThread(() -> {
                progress.dismiss();
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Trợ lý Quản trị AI tư vấn")
                        .setMessage(feedback)
                        .setPositiveButton("Cảm ơn AI", null)
                        .show();
            });
        });
    }

    private void updatePieChart(int excellent, int good, int average, int weak) {
        List<PieEntry> entries = new ArrayList<>();
        if (excellent > 0) entries.add(new PieEntry(excellent, "Giỏi"));
        if (good > 0) entries.add(new PieEntry(good, "Khá"));
        if (average > 0) entries.add(new PieEntry(average, "TB"));
        if (weak > 0) entries.add(new PieEntry(weak, "Yếu"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(android.R.color.black);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate(); // refresh
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentCursor != null && !currentCursor.isClosed()) {
            currentCursor.close();
        }
    }
}