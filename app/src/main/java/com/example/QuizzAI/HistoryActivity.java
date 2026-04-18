package com.example.QuizzAI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyLayout;
    private DatabaseHelper db;
    private ArrayList<HistoryItem> list;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.historyRecycler);
        emptyLayout = findViewById(R.id.emptyLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = new DatabaseHelper(this);
        list = new ArrayList<>();

        adapter = new HistoryAdapter(list, item -> {
            Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
            intent.putExtra("historyId", item.id);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        list.clear();

        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        int userId = sp.getInt("userId", -1);

        Cursor c = db.getAllHistoryByUser(userId);

        if (c != null && c.moveToFirst()) {
            do {
                HistoryItem item = new HistoryItem();

                item.id = c.getInt(c.getColumnIndexOrThrow("id"));
                item.subject = c.getString(c.getColumnIndexOrThrow("subject")); // 🔥
                item.score = c.getInt(c.getColumnIndexOrThrow("score"));
                item.total = c.getInt(c.getColumnIndexOrThrow("total"));
                item.date = c.getLong(c.getColumnIndexOrThrow("date"));

                list.add(item);

            } while (c.moveToNext());

            c.close();
        }

        if (list.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }
}
