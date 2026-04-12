package com.example.QuizzAI;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserManagementActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText edtSearchUser;
    TextView txtUserCount;
    
    DatabaseHelper db;
    ArrayList<User> list;
    UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        db = new DatabaseHelper(this);
        
        recyclerView = findViewById(R.id.recyclerUser);
        edtSearchUser = findViewById(R.id.edtSearchUser);
        txtUserCount = findViewById(R.id.txtUserCount);
        
        list = new ArrayList<>();
        loadData(""); // Ban đầu tải tất cả

        adapter = new UserAdapter(this, list, db);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        updateUserCount();

        // Xử lý tìm kiếm
        edtSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadData(s.toString());
                adapter.notifyDataSetChanged();
                updateUserCount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public void updateUserCount() {
        txtUserCount.setText("Tổng số: " + list.size() + " người dùng");
    }

    private void loadData(String query) {
        Cursor cursor;
        if (query.isEmpty()) {
            cursor = db.getAllUsers();
        } else {
            // Tìm kiếm theo username hoặc email
            cursor = db.getReadableDatabase().rawQuery(
                    "SELECT user_id, username, email, status FROM users WHERE username LIKE ? OR email LIKE ?",
                    new String[]{"%" + query + "%", "%" + query + "%"}
            );
        }

        list.clear();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String username = cursor.getString(1);
            String email = cursor.getString(2);
            int status = cursor.getInt(3);

            list.add(new User(id, username, email, status));
        }
        cursor.close();
    }
}