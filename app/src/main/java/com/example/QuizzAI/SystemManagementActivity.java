package com.example.QuizzAI;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SystemManagementActivity extends AppCompatActivity {

    TextView txtStatUsers, txtStatQuestions, txtStatHistory, txtStatRequests;
    Button btnClearAI, btnClearHistory, btnCheckAI, btnReset;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_management);

        db = new DatabaseHelper(this);

        // Ánh xạ
        txtStatUsers = findViewById(R.id.txtStatUsers);
        txtStatQuestions = findViewById(R.id.txtStatQuestions);
        txtStatHistory = findViewById(R.id.txtStatHistory);
        txtStatRequests = findViewById(R.id.txtStatRequests);

        btnClearAI = findViewById(R.id.btnClearAIQuestions);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        btnCheckAI = findViewById(R.id.btnCheckAI);
        btnReset = findViewById(R.id.btnResetSystem);

        loadStats();

        // XỬ LÝ SỰ KIỆN
        btnClearAI.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Dọn dẹp")
                    .setMessage("Xóa các câu hỏi AI chưa được admin duyệt?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        db.getWritableDatabase().delete("questions", "source='AI' AND status=0", null);
                        loadStats();
                        Toast.makeText(this, "Đã dọn dẹp", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        btnClearHistory.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Cảnh báo")
                    .setMessage("Xóa toàn bộ lịch sử thi của người dùng?")
                    .setPositiveButton("Xóa hết", (dialog, which) -> {
                        db.clearTable("quiz_history");
                        db.clearTable("quiz_detail");
                        loadStats();
                        Toast.makeText(this, "Đã xóa lịch sử", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        btnCheckAI.setOnClickListener(v -> checkAIStatus());

        btnReset.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("NGUY HIỂM")
                    .setMessage("Hành động này sẽ xóa SẠCH dữ liệu người dùng và câu hỏi. Bạn có chắc chắn không?")
                    .setPositiveButton("TÔI ĐỒNG Ý", (dialog, which) -> {
                        db.resetSystem();
                        loadStats();
                        Toast.makeText(this, "Hệ thống đã được reset", Toast.LENGTH_LONG).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void loadStats() {
        txtStatUsers.setText("Người dùng: " + db.getTableRowCount("users"));
        txtStatQuestions.setText("Câu hỏi trong kho: " + db.getTableRowCount("questions"));
        txtStatHistory.setText("Lượt làm bài: " + db.getTableRowCount("quiz_history"));
        txtStatRequests.setText("Yêu cầu mở khóa: " + db.getTableRowCount("unlock_requests"));
    }

    private void checkAIStatus() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang kết nối server Gemini...");
        pd.show();

        // Gửi một yêu cầu rất nhỏ để check ping
        GeminiApi.generateAdminConsultation("Ping test. Respond with OK", feedback -> {
            runOnUiThread(() -> {
                pd.dismiss();
                if (feedback != null && !feedback.contains("Lỗi")) {
                    new AlertDialog.Builder(this)
                            .setTitle("Kết nối AI")
                            .setMessage("✅ Server Gemini đang hoạt động tốt!\nPhản hồi: " + feedback)
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Kết nối AI")
                            .setMessage("❌ Lỗi kết nối AI. Kiểm tra Internet hoặc API Key.\n" + feedback)
                            .setPositiveButton("OK", null)
                            .show();
                }
            });
        });
    }
}