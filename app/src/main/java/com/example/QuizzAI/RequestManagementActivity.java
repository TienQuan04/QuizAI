package com.example.QuizzAI;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RequestManagementActivity extends AppCompatActivity {

    RecyclerView recycler;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        recycler = findViewById(R.id.recycler);
        db = new DatabaseHelper(this);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        loadData();
    }

    private void loadData(){

        Cursor cursor = db.getAllRequests();

        RequestAdapter adapter = new RequestAdapter(this, cursor, db); // ✅ truyền db
        recycler.setAdapter(adapter);
    }
}