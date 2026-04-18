package com.example.QuizzAI;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText edtFullName, edtBirth, edtUser, edtPass;
    Button btnCreate;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtFullName = findViewById(R.id.edtFullName);
        edtBirth = findViewById(R.id.edtBirth);
        edtUser = findViewById(R.id.edtUser);
        edtPass = findViewById(R.id.edtPass);
        btnCreate = findViewById(R.id.btnCreate);

        db = new DatabaseHelper(this);

        edtBirth.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog picker = new DatePickerDialog(
                    RegisterActivity.this,
                    (view, y, m, d) -> edtBirth.setText(getString(R.string.date_format, d, (m + 1), y)),
                    year, month, day
            );

            picker.show();
        });

        btnCreate.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String name = getText(edtFullName);
        String birth = getText(edtBirth);
        String user = getText(edtUser);
        String pass = getText(edtPass);

        if (name.isEmpty()) {
            edtFullName.setError("Nhập họ tên");
            return;
        }

        if (birth.isEmpty()) {
            edtBirth.setError("Chọn ngày sinh");
            return;
        }

        if (user.isEmpty()) {
            edtUser.setError("Nhập tài khoản");
            return;
        }

        if (user.length() < 4) {
            edtUser.setError("Ít nhất 4 ký tự");
            return;
        }

        if (pass.isEmpty()) {
            edtPass.setError("Nhập mật khẩu");
            return;
        }

        if (pass.length() < 6) {
            edtPass.setError("Mật khẩu ≥ 6 ký tự");
            return;
        }

        // ================= CHECK USER =================
        if (db.checkUserExists(user)) {
            edtUser.setError("Tài khoản đã tồn tại");
            return;
        }

        // ================= INSERT USER =================
        long result = db.insertUser(user, name, pass);

        if (result != -1) {
            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(TextInputEditText edt) {
        return edt.getText() != null ? edt.getText().toString().trim() : "";
    }
}