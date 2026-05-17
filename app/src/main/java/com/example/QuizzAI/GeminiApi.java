package com.example.QuizzAI;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GeminiApi {

    //  API key - HÃY THAY KEY MỚI CỦA BẠN VÀO ĐÂY
    private static final String GEMINI_API_KEY =
            "KEY CUA BAN";

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                    + GEMINI_API_KEY;

    // ================= QUESTIONS CALLBACK =================

    public interface OnQuestionsReady {
        void onReady(List<Question> questions);
    }

    // ================= GENERATE QUESTIONS =================

    public static void generateQuestions(
            String subject,
            int count,
            OnQuestionsReady callback
    ) {

        new AsyncTask<Void, Void, List<Question>>() {

            @Override
            protected List<Question> doInBackground(Void... voids) {

                HttpURLConnection conn = null;

                try {

                    URL url = new URL(GEMINI_API_URL);

                    conn =
                            (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");

                    conn.setRequestProperty(
                            "Content-Type",
                            "application/json"
                    );

                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);

                    conn.setDoOutput(true);

                    // ================= PROMPT =================

                    JSONObject promptObj = new JSONObject();

                    JSONArray contents = new JSONArray();

                    JSONObject text = new JSONObject();

                    text.put(
                            "text",

                            "Hãy tạo CHÍNH XÁC "
                                    + count
                                    + " câu hỏi trắc nghiệm bằng tiếng Việt "
                                    + "CHỈ liên quan đến chủ đề sau:\n\n"

                                    + "CHỦ ĐỀ: " + subject + "\n\n"

                                    + "QUY TẮC BẮT BUỘC:\n"
                                    + "- Tất cả câu hỏi phải đúng chủ đề\n"
                                    + "- Không được tạo câu hỏi thuộc môn khác\n"
                                    + "- Không mở rộng sang lĩnh vực khác\n"
                                    + "- Mỗi câu phải có đúng 4 đáp án\n"
                                    + "- Chỉ có 1 đáp án đúng\n"
                                    + "- Không markdown\n"
                                    + "- Không giải thích\n"
                                    + "- Không thêm chữ ngoài JSON\n"
                                    + "- Chỉ trả về JSON hợp lệ\n\n"

                                    + "FORMAT:\n"
                                    + "- answer phải từ 0 đến 3\n"
                                    + "[\n"
                                    + "  {\n"
                                    + "    \"question\":\"...\",\n"
                                    + "    \"options\":[\"A\",\"B\",\"C\",\"D\"],\n"
                                    + "    \"answer\":0\n"
                                    + "  }\n"
                                    + "]"
                    );

                    JSONObject part = new JSONObject();

                    part.put(
                            "parts",
                            new JSONArray().put(text)
                    );

                    contents.put(part);

                    promptObj.put("contents", contents);

                    // ================= SEND REQUEST =================

                    OutputStream os =
                            conn.getOutputStream();

                    os.write(
                            promptObj.toString()
                                    .getBytes("UTF-8")
                    );

                    os.flush();
                    os.close();

                    // ================= RESPONSE CODE =================

                    int code = conn.getResponseCode();

                    Log.d(
                            "HTTP_CODE",
                            String.valueOf(code)
                    );

                    InputStream is;

                    if (code >= 400) {

                        is = conn.getErrorStream();

                    } else {

                        is = conn.getInputStream();
                    }

                    if (is == null) {

                        Log.d(
                                "GeminiError",
                                "InputStream NULL"
                        );

                        return null;
                    }

                    Scanner scanner =
                            new Scanner(is)
                                    .useDelimiter("\\A");

                    String response =
                            scanner.hasNext()
                                    ? scanner.next()
                                    : "";

                    scanner.close();
                    is.close();

                    Log.d(
                            "GEMINI_RESPONSE",
                            response
                    );

                    // ================= RESPONSE EMPTY =================

                    if (response.isEmpty()) {

                        Log.d(
                                "GeminiError",
                                "Response EMPTY"
                        );

                        return null;
                    }

                    // ================= HANDLE ERROR =================

                    if (code != 200) {

                        Log.d(
                                "GEMINI_ERROR_FULL",
                                response
                        );

                        return null;
                    }

                    JSONObject resObj =
                            new JSONObject(response);

                    // ================= CHECK CANDIDATES =================

                    if (!resObj.has("candidates")) {

                        Log.d(
                                "GEMINI_ERROR_FULL",
                                response
                        );

                        return null;
                    }

                    // ================= GET TEXT =================

                    String json =
                            resObj
                                    .getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text")
                                    .trim();

                    // ================= REMOVE ```json =================

                    json = json
                            .replace("```json", "")
                            .replace("```", "")
                            .trim();

                    Log.d(
                            "JSON_RESULT",
                            json
                    );

                    // ================= PARSE JSON =================

                    JSONArray questionsArray =
                            new JSONArray(json);

                    List<Question> questions =
                            new ArrayList<>();

                    DatabaseHelper db =
                            new DatabaseHelper(
                                    App.getContext()
                            );

// 🔥 XÓA CÂU AI CŨ
                   /// db.deleteAIQuestions();
                    for (int i = 0;
                         i < questionsArray.length();
                         i++) {

                        JSONObject q =
                                questionsArray.getJSONObject(i);

                        Question question =
                                new Question();

                        question.question =
                                q.getString("question");

                        JSONArray opts =
                                q.getJSONArray("options");

                        question.options =
                                new ArrayList<>();

                        for (int j = 0;
                             j < opts.length();
                             j++) {

                            question.options.add(
                                    opts.getString(j)
                            );
                        }

                        question.answerIndex =
                                q.getInt("answer");

                        if(question.answerIndex > 3){
                            question.answerIndex =
                                    question.answerIndex - 1;
                        }

                        questions.add(question);

                        // ================= SAVE SQLITE =================

                        if (question.options.size() >= 4) {

                            db.insertQuestion(

                                    question.question,

                                    question.options.get(0),

                                    question.options.get(1),

                                    question.options.get(2),

                                    question.options.get(3),

                                    question.answerIndex,

                                    subject, // 🔥 LƯU SUBJECT VÀO ĐÂY

                                    "AI"
                            );
                        }
                    }

                    return questions;

                } catch (Exception e) {

                    Log.e(
                            "GeminiException",
                            e.toString()
                    );

                    e.printStackTrace();

                    return null;

                } finally {

                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(
                    List<Question> result
            ) {

                if (result != null
                        && !result.isEmpty()) {

                    callback.onReady(result);

                } else {

                    Log.d(
                            "GeminiError",
                            "Không có câu hỏi nào được tạo!"
                    );
                }
            }

        }.execute();
    }

    // ================= FEEDBACK CALLBACK =================

    public interface OnFeedbackReady {
        void onReady(String feedback);
    }

    // ================= GENERATE FEEDBACK =================

    public static void generateFeedback(
            List<Question> questions,
            OnFeedbackReady callback
    ) {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {

                HttpURLConnection conn = null;

                try {

                    URL url = new URL(GEMINI_API_URL);

                    conn =
                            (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");

                    conn.setRequestProperty(
                            "Content-Type",
                            "application/json"
                    );

                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);

                    conn.setDoOutput(true);

                    StringBuilder promptBuilder =
                            new StringBuilder();

                    promptBuilder.append(
                            "Phân tích bài kiểm tra sau:\n\n"
                    );

                    int correctCount = 0;

                    for (int i = 0;
                         i < questions.size();
                         i++) {

                        Question q = questions.get(i);

                        boolean isCorrect =
                                (q.userAnswer
                                        == q.answerIndex);

                        if (isCorrect) {
                            correctCount++;
                        }

                        promptBuilder
                                .append("Câu ")
                                .append(i + 1)
                                .append(": ")
                                .append(q.question)
                                .append("\n");

                        promptBuilder
                                .append("Đúng: ")
                                .append(
                                        q.options.get(
                                                q.answerIndex
                                        )
                                )
                                .append("\n");

                        promptBuilder
                                .append("Người dùng: ");

                        if (q.userAnswer >= 0
                                && q.userAnswer < q.options.size()) {

                            promptBuilder.append(
                                    q.options.get(
                                            q.userAnswer
                                    )
                            );

                            promptBuilder.append(
                                    isCorrect
                                            ? " (ĐÚNG)"
                                            : " (SAI)"
                            );

                        } else {

                            promptBuilder.append(
                                    "Không chọn"
                            );
                        }

                        promptBuilder.append("\n\n");
                    }

                    promptBuilder
                            .append("Tổng điểm: ")
                            .append(correctCount)
                            .append("/")
                            .append(questions.size());

                    JSONObject payload =
                            new JSONObject();

                    JSONArray contents =
                            new JSONArray();

                    JSONObject content =
                            new JSONObject();

                    content.put(
                            "parts",
                            new JSONArray().put(
                                    new JSONObject().put(
                                            "text",
                                            promptBuilder.toString()
                                    )
                            )
                    );

                    contents.put(content);

                    payload.put("contents", contents);

                    // ================= SEND REQUEST =================

                    OutputStream os =
                            conn.getOutputStream();

                    os.write(
                            payload.toString()
                                    .getBytes("UTF-8")
                    );

                    os.flush();
                    os.close();

                    // ================= RESPONSE =================

                    InputStream is =
                            conn.getInputStream();

                    Scanner scanner =
                            new Scanner(is)
                                    .useDelimiter("\\A");

                    String response =
                            scanner.hasNext()
                                    ? scanner.next()
                                    : "";

                    scanner.close();
                    is.close();

                    // ================= EMPTY RESPONSE =================

                    if (response.isEmpty()) {

                        return "Không nhận được phản hồi từ AI!";
                    }

                    JSONObject resObj =
                            new JSONObject(response);

                    return resObj
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")
                            .trim();

                } catch (Exception e) {

                    e.printStackTrace();

                    return "Lỗi tạo nhận xét!";

                } finally {

                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(
                    String result
            ) {

                callback.onReady(result);
            }

        }.execute();
    }

    // ================= ADMIN CONSULTATION =================

    public static void generateAdminConsultation(
            String statsData,
            OnFeedbackReady callback
    ) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection conn = null;
                try {
                    Log.d("GeminiConsultant", "Bắt đầu kết nối...");
                    URL url = new URL(GEMINI_API_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setConnectTimeout(20000); // Giảm xuống 20s để báo lỗi nhanh hơn
                    conn.setReadTimeout(20000);
                    conn.setDoOutput(true);

                    // ... (phần tạo prompt giữ nguyên)
                    String prompt = "Bạn là một chuyên gia phân tích dữ liệu giáo dục cho ứng dụng QuizzAI. " +
                            "Dựa trên số liệu thống kê sau đây từ hệ thống, hãy đưa ra một bản nhận xét ngắn gọn, " +
                            "chuyên nghiệp và các lời khuyên để cải thiện chất lượng học tập của người dùng:\n\n" +
                            statsData + "\n\n" +
                            "Yêu cầu:\n" +
                            "- Phân tích về điểm trung bình và tỉ lệ xếp loại.\n" +
                            "- Đưa ra ít nhất 2 lời khuyên cụ thể cho người quản trị.\n" +
                            "- Trả lời bằng tiếng Việt, súc tích, chuyên nghiệp.";

                    JSONObject payload = new JSONObject();
                    JSONArray contents = new JSONArray();
                    JSONObject content = new JSONObject();
                    content.put("parts", new JSONArray().put(new JSONObject().put("text", prompt)));
                    contents.put(content);
                    payload.put("contents", contents);

                    Log.d("GeminiConsultant", "Đang gửi dữ liệu...");
                    OutputStream os = conn.getOutputStream();
                    os.write(payload.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int code = conn.getResponseCode();
                    Log.d("GeminiConsultant", "HTTP Code: " + code);

                    InputStream is;
                    if (code >= 400) {
                        is = conn.getErrorStream();
                    } else {
                        is = conn.getInputStream();
                    }

                    if (is == null) return "Không thể kết nối luồng dữ liệu";

                    Scanner scanner = new Scanner(is).useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    scanner.close();
                    is.close();

                    if (response.isEmpty()) return "Không có phản hồi từ Server!";

                    if (code != 200) {
                        Log.e("GeminiConsultant", "Lỗi từ Server: " + response);
                        return "Lỗi Server AI (Mã " + code + ")";
                    }

                    JSONObject resObj = new JSONObject(response);
                    return resObj.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text").trim();

                } catch (Exception e) {
                    Log.e("GeminiConsultant", "Ngoại lệ: " + e.getMessage());
                    return "Lỗi kết nối: " + e.getMessage();
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                callback.onReady(result);
            }
        }.execute();
    }
}
