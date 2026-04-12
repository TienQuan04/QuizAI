package com.example.QuizzAI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminResultAdapter extends RecyclerView.Adapter<AdminResultAdapter.ViewHolder> {

    private Context context;
    private Cursor cursor;
    private DatabaseHelper db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public AdminResultAdapter(Context context, Cursor cursor, DatabaseHelper db) {
        this.context = context;
        this.cursor = cursor;
        this.db = db;
    }

    public void updateCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) return;

        final int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
        String subject = cursor.getString(cursor.getColumnIndexOrThrow("subject"));
        int score = cursor.getInt(cursor.getColumnIndexOrThrow("score"));
        int total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
        long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

        holder.txtUser.setText("User: " + username);
        holder.txtSubject.setText("Môn: " + subject);
        holder.txtScore.setText("Điểm: " + score + "/" + total);
        holder.txtDate.setText("Ngày: " + dateFormat.format(new Date(dateMillis)));

        holder.btnViewDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("historyId", id);
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc muốn xóa lịch sử này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        db.deleteHistory(id);
                        Toast.makeText(context, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                        // Trigger reload in activity
                        if (context instanceof AnalyticsActivity) {
                            ((AnalyticsActivity) context).loadData();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return (cursor == null) ? 0 : cursor.getCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUser, txtSubject, txtScore, txtDate;
        Button btnViewDetail, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            txtUser = itemView.findViewById(R.id.txtUser);
            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtScore = itemView.findViewById(R.id.txtScore);
            txtDate = itemView.findViewById(R.id.txtDate);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}