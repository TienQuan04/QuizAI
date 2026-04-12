package com.example.QuizzAI;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.recyclerview.widget.RecyclerView;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    Context context;
    Cursor cursor;
    DatabaseHelper db;

    public RequestAdapter(Context context, Cursor cursor, DatabaseHelper db) {
        this.context = context;
        this.cursor = cursor;
        this.db = db;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {

        cursor.moveToPosition(position);

        int id = cursor.getInt(0);
        String username = cursor.getString(1);
        String message = cursor.getString(2);
        int status = cursor.getInt(3);
        int userId = cursor.getInt(4);

        h.txtUser.setText("👤 " + username);
        h.txtReason.setText(message);

        h.btnApprove.setEnabled(true);
        h.btnReject.setEnabled(true);

        if(status == 0){
            h.txtStatus.setText("⏳ Đang chờ");
            h.txtStatus.setTextColor(0xFFF59E0B);
        }
        else if(status == 1){
            h.txtStatus.setText("✅ Đã duyệt");
            h.txtStatus.setTextColor(0xFF16A34A);

            h.btnApprove.setEnabled(false);
            h.btnReject.setEnabled(false);
        }
        else if(status == 2){
            h.txtStatus.setText("❌ Bị từ chối");
            h.txtStatus.setTextColor(0xFFDC2626);

            String note = cursor.getString(5);

            if(note == null || note.isEmpty()){
                h.txtReason.setText(message + "\n(Admin chưa ghi lý do)");
            }else{
                h.txtReason.setText(message + "\nLý do admin: " + note);
            }

            h.btnApprove.setEnabled(false);
            h.btnReject.setEnabled(false);
        }
        // APPROVE
        h.btnApprove.setOnClickListener(v -> {
            db.approveRequest(id, userId);
            Toast.makeText(context, "Đã duyệt", Toast.LENGTH_SHORT).show();
            refresh();
        });

        // REJECT (có nhập lý do)
        h.btnReject.setOnClickListener(v -> {

            EditText input = new EditText(context);
            input.setHint("Nhập lý do từ chối...");

            new AlertDialog.Builder(context)
                    .setTitle("Từ chối yêu cầu")
                    .setView(input)
                    .setPositiveButton("OK", (dialog, which) -> {
                        String reason = input.getText().toString();
                        db.rejectRequest(id, reason);
                        Toast.makeText(context, "Đã từ chối", Toast.LENGTH_SHORT).show();
                        refresh();
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtUser, txtReason, txtStatus;
        Button btnApprove, btnReject;

        public ViewHolder(View itemView) {
            super(itemView);

            txtUser = itemView.findViewById(R.id.txtUser);
            txtReason = itemView.findViewById(R.id.txtReason);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }

    private void refresh(){
        ((RequestManagementActivity) context).recreate();
    }
}