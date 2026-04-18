package com.example.QuizzAI;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private ArrayList<HistoryItem> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HistoryItem item);
    }

    public HistoryAdapter(ArrayList<HistoryItem> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvScore, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // 🔥 chống crash
        if (list == null || list.size() == 0) return;

        HistoryItem item = list.get(position);
        if (item == null) return;

        // 📘 SUBJECT
        if (item.subject == null || item.subject.trim().isEmpty()) {
            holder.tvSubject.setText("📘 Không rõ chủ đề");
        } else {
            holder.tvSubject.setText("📘 " + item.subject);
        }

        // 🎯 SCORE + %
        int total = (item.total == 0) ? 1 : item.total; // tránh chia 0
        double percent = item.score * 100.0 / total;

        holder.tvScore.setText("🎯 " + item.score + "/" + item.total + "  •  " + (int) percent + "%");

        // 🎨 màu theo điểm
        if (percent >= 70) {
            holder.tvScore.setTextColor(Color.parseColor("#4CAF50")); // xanh
        } else if (percent >= 40) {
            holder.tvScore.setTextColor(Color.parseColor("#FF9800")); // cam
        } else {
            holder.tvScore.setTextColor(Color.parseColor("#F44336")); // đỏ
        }

        // 📅 DATE
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault());
            holder.tvDate.setText("🕒 " + sdf.format(new Date(item.date)));
        } catch (Exception e) {
            holder.tvDate.setText("🕒 Không rõ ngày");
        }

        // ✨ animation mượt
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(50);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(300)
                .start();

        // 🔥 CLICK AN TOÀN
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (list == null) ? 0 : list.size();
    }
}
