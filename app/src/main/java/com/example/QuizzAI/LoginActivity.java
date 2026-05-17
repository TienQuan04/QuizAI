package com.example.QuizzAI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor; // ✅ THIẾU IMPORT
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    Button btnLogin, btnRegister;

    DatabaseHelper db;

    public static final String PREF_NAME = "USER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        db = new DatabaseHelper(this);

        btnLogin.setOnClickListener(v -> {

            String user = edtUsername.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();

            if (user.isEmpty()) {
                edtUsername.setError("Nhập tài khoản");
                return;
            }

            if (pass.isEmpty()) {
                edtPassword.setError("Nhập mật khẩu");
                return;
            }

            int userId = db.checkLogin(user, pass);

            //  ADMIN
            if (userId == 9999) {
                Toast.makeText(this, "Đăng nhập Admin", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AdminDashboardActivity.class));
                finish();
                return;
            }

            //  BỊ KHÓA (FIX CHUẨN)
            else if (userId == -2) {

                // lấy user_id thật
                Cursor c = db.getReadableDatabase().rawQuery(
                        "SELECT user_id FROM users WHERE username=?",
                        new String[]{user}
                );

                int realId = -1;
                if (c.moveToFirst()) {
                    realId = c.getInt(0);
                }
                c.close();

                SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                sp.edit()
                        .putInt("userId", realId)
                        .putString("username", user)
                        .apply();

                Toast.makeText(this, "Tài khoản đã bị khóa", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(this, BlockedActivity.class));
                return;
            }

            // 👤 USER
            else if (userId != -1) {

                SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                sp.edit()
                        .putBoolean("isLogin", true)
                        .putInt("userId", userId)
                        .putString("username", user)
                        .apply();

                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            //  FAIL
            else {
                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}