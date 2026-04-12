package com.example.QuizzAI;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BlockedActivity extends AppCompatActivity {

    EditText edtReason;
    Button btnSend;
    TextView txtAdminNote;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked);

        edtReason = findViewById(R.id.edtReason);
        btnSend = findViewById(R.id.btnSend);
        txtAdminNote = findViewById(R.id.txtAdminNote);

        db = new DatabaseHelper(this);

        checkRequestStatus();

        btnSend.setOnClickListener(v -> {
            String reason = edtReason.getText().toString().trim();

            if(reason.isEmpty()){
                edtReason.setError("Nhập lý do mở khóa");
                return;
            }

            SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
            int userId = sp.getInt("userId", -1);

            if(userId == -1){
                Toast.makeText(this, "Lỗi user", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = db.sendUnlockRequest(userId, reason);

            if(ok){
                Toast.makeText(this, "Đã gửi yêu cầu", Toast.LENGTH_SHORT).show();
                checkRequestStatus(); // Load lại trạng thái sau khi gửi
            }else{
                Toast.makeText(this, "Bạn đã gửi rồi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkRequestStatus() {
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        int userId = sp.getInt("userId", -1);

        if (userId != -1) {
            Cursor c = db.getLatestRequest(userId);
            if (c.moveToFirst()) {
                int status = c.getInt(0);
                String adminNote = c.getString(1);

                if (status == 2) { // Bị từ chối
                    txtAdminNote.setVisibility(View.VISIBLE);
                    if (adminNote != null && !adminNote.isEmpty()) {
                        txtAdminNote.setText("Lý do bị Admin từ chối: " + adminNote);
                    } else {
                        txtAdminNote.setText("Yêu cầu của bạn đã bị từ chối.");
                    }
                } else if (status == 0) {
                    txtAdminNote.setVisibility(View.VISIBLE);
                    txtAdminNote.setText("Yêu cầu mở khóa của bạn đang chờ duyệt...");
                    txtAdminNote.setTextColor(0xFFF59E0B); // Màu cam
                }
            }
            c.close();
        }
    }
}