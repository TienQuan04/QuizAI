package com.example.QuizzAI;

import android.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    Context context;
    ArrayList<User> list;
    DatabaseHelper db;

    public UserAdapter(Context context, ArrayList<User> list, DatabaseHelper db) {
        this.context = context;
        this.list = list;
        this.db = db;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUsername, txtEmail, txtStatusBadge, txtAvatarLetter;
        View btnDelete, btnBlock;

        public ViewHolder(View view) {
            super(view);
            txtUsername = view.findViewById(R.id.txtUsername);
            txtEmail = view.findViewById(R.id.txtEmail);
            txtStatusBadge = view.findViewById(R.id.txtStatusBadge);
            txtAvatarLetter = view.findViewById(R.id.txtAvatarLetter);
            btnDelete = view.findViewById(R.id.btnDelete);
            btnBlock = view.findViewById(R.id.btnBlock);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        User u = list.get(position);

        holder.txtUsername.setText(u.username);
        holder.txtEmail.setText(u.email);
        
        // Avatar letter
        if (u.username != null && !u.username.isEmpty()) {
            holder.txtAvatarLetter.setText(u.username.substring(0, 1).toUpperCase());
        }

        // Status Badge
        if (u.status == 1) {
            holder.txtStatusBadge.setText("Đang hoạt động");
            holder.txtStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF22C55E)); // Green
            ((ImageButton)holder.btnBlock).setImageResource(android.R.drawable.ic_lock_idle_lock);
        } else {
            holder.txtStatusBadge.setText("Bị khóa");
            holder.txtStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFEF4444)); // Red
            ((ImageButton)holder.btnBlock).setImageResource(android.R.drawable.ic_partial_secure);
        }

        // ❌ XÓA
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận")
                    .setMessage("Xóa người dùng '" + u.username + "'? Dữ liệu lịch sử cũng sẽ bị xóa.")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        db.deleteUser(u.id);
                        list.remove(position);
                        notifyDataSetChanged();
                        
                        // Update count if activity is UserManagementActivity
                        if (context instanceof UserManagementActivity) {
                            ((UserManagementActivity) context).updateUserCount();
                        }
                        
                        Toast.makeText(context, "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // 🔒 KHÓA / MỞ
        holder.btnBlock.setOnClickListener(v -> {
            String action = (u.status == 1) ? "khóa" : "mở khóa";
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc muốn " + action + " người dùng này?")
                    .setPositiveButton("Đồng ý", (dialog, which) -> {
                        if(u.status == 1){
                            db.blockUser(u.id);
                            u.status = 0;
                            Toast.makeText(context, "Đã khóa người dùng", Toast.LENGTH_SHORT).show();
                        } else {
                            db.unblockUser(u.id);
                            u.status = 1;
                            Toast.makeText(context, "Đã mở khóa người dùng", Toast.LENGTH_SHORT).show();
                        }
                        notifyDataSetChanged();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}