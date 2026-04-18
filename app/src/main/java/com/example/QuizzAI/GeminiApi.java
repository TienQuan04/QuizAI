package com.example.QuizzAI;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GeminiApi {

    private static final String GEMINI_API_KEY = "AIzaSyDskBLGvRiUb5x6aLyKTA_Tb_Aog0ce56o";

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY;

    // ================= QUESTIONS =================
    public interface OnQuestionsReady {
        void onReady(List<Question> questions);
    }

    public static void generateQuestions(String subject, int count, OnQuestionsReady callback) {
        new AsyncTask<Void, Void, List<Question>>() {

            @Override
            protected List<Question> doInBackground(Void... voids) {
                try {
                    URL url = new URL(GEMINI_API_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    // ===== PROMPT =====
                    JSONObject promptObj = new JSONObject();
                    JSONArray contents = new JSONArray();

                    JSONObject text = new JSONObject();
                    text.put("text",
                            "Tạo " + count + " câu hỏi trắc nghiệm JSON về \"" + subject + "\".\n" +
                                    "Chỉ trả về JSON dạng:\n" +
                                    "[{\"question\":\"...\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"answer\":1}]\n" +
                                    "QUAN TRỌNG:\n" +
                                    "- answer PHẢI là số từ 0-3\n" +
                                    "- KHÔNG giải thích\n" +
                                    "- KHÔNG text ngoài JSON"
                    );

                    JSONObject part = new JSONObject();
                    part.put("parts", new JSONArray().put(text));
                    contents.put(part);
                    promptObj.put("contents", contents);

                    OutputStream os = conn.getOutputStream();
                    os.write(promptObj.toString().getBytes());
                    os.flush();

                    // ===== RESPONSE =====
                    int code = conn.getResponseCode();

                    if (code != 200) {
                        Log.e("GeminiError", "HTTP Error: " + code);
                        return getFallbackQuestions();
                    }

                    String response = new Scanner(conn.getInputStream()).useDelimiter("\\A").next();

                    JSONObject resObj = new JSONObject(response);
                    String json = resObj.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")
                            .trim();

                    // Clean markdown
                    json = json.replace("```json", "")
                            .replace("```", "")
                            .trim();

                    Log.d("GeminiAnswer", json);

                    JSONArray questionsArray = new JSONArray(json);
                    List<Question> questions = new ArrayList<>();

                    for (int i = 0; i < questionsArray.length(); i++) {

                        JSONObject q = questionsArray.getJSONObject(i);
                        Question question = new Question();

                        question.question = q.getString("question");

                        JSONArray opts = q.getJSONArray("options");
                        question.options = new ArrayList<>();

                        for (int j = 0; j < opts.length(); j++) {
                            question.options.add(opts.getString(j));
                        }

                        // ===== FIX ANSWER (QUAN TRỌNG) =====
                        try {
                            Object answerObj = q.get("answer");

                            if (answerObj instanceof Integer) {
                                question.answerIndex = (int) answerObj;
                            } else if (answerObj instanceof String) {
                                String ans = (String) answerObj;
                                int index = question.options.indexOf(ans);

                                question.answerIndex = (index != -1) ? index : 0;
                            } else {
                                question.answerIndex = 0;
                            }

                        } catch (Exception e) {
                            question.answerIndex = 0;
                        }

                        questions.add(question);
                    }

                    return questions;

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("GeminiCrash", e.getMessage());
                    return getFallbackQuestions();
                }
            }

            @Override
            protected void onPostExecute(List<Question> result) {
                if (result != null && !result.isEmpty()) {
                    callback.onReady(result);
                } else {
                    callback.onReady(getFallbackQuestions());
                }
            }
        }.execute();
    }

    // ================= FEEDBACK =================
    public interface OnFeedbackReady {
        void onReady(String feedback);
    }
    public static void generateFeedback(List<Question> questions, OnFeedbackReady callback) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(GEMINI_API_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    StringBuilder prompt = new StringBuilder();
                    prompt.append("Phân tích kết quả bài test:\n");

                    int correct = 0;

                    for (int i = 0; i < questions.size(); i++) {
                        Question q = questions.get(i);

                        boolean isCorrect = (q.userAnswer == q.answerIndex);
                        if (isCorrect) correct++;

                        prompt.append("Câu ").append(i+1).append(": ").append(q.question).append("\n");
                        prompt.append("Đúng: ").append(q.options.get(q.answerIndex)).append("\n");
                        prompt.append("Bạn chọn: ");

                        if (q.userAnswer >= 0)
                            prompt.append(q.options.get(q.userAnswer));
                        else
                            prompt.append("Không chọn");

                        prompt.append("\n\n");
                    }

                    prompt.append("Tổng điểm: ").append(correct).append("/").append(questions.size());

                    JSONObject payload = new JSONObject();
                    JSONArray contents = new JSONArray();

                    JSONObject content = new JSONObject();
                    content.put("parts", new JSONArray().put(
                            new JSONObject().put("text", prompt.toString())
                    ));

                    contents.put(content);
                    payload.put("contents", contents);

                    OutputStream os = conn.getOutputStream();
                    os.write(payload.toString().getBytes());
                    os.flush();

                    String response = new Scanner(conn.getInputStream()).useDelimiter("\\A").next();

                    JSONObject resObj = new JSONObject(response);

                    return resObj.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                } catch (Exception e) {
                    e.printStackTrace();
                    return "Không thể tạo nhận xét.";
                }
            }

            @Override
            protected void onPostExecute(String result) {
                callback.onReady(result);
            }
        }.execute();
    }

    // ================= FALLBACK =================
    private static List<Question> getFallbackQuestions() {
        List<Question> list = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Question q = new Question();
            q.question = "Câu hỏi mẫu " + (i + 1);

            q.options = new ArrayList<>();
            q.options.add("A");
            q.options.add("B");
            q.options.add("C");
            q.options.add("D");

            q.answerIndex = 0;
            list.add(q);
        }

        return list;
    }
}