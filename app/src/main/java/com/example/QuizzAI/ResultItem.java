package com.example.QuizzAI;
public class ResultItem {

    public String subject;
    public int score;
    public int total;
    public String time;

    public ResultItem(String subject, int score, int total, String time) {
        this.subject = subject;
        this.score = score;
        this.total = total;
        this.time = time;
    }
}