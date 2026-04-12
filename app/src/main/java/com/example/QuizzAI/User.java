package com.example.QuizzAI;

public class User {
    public int id;
    public String username;
    public String email;
    public int status; // 1: active, 0: blocked

    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // 👉 thêm constructor mới (không xóa cái cũ)
    public User(int id, String username, String email, int status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
    }
}