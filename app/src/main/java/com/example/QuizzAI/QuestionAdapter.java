package com.example.QuizzAI;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder>{

    Context context;
    Cursor cursor;
    DatabaseHelper db;

    public QuestionAdapter(Context c, Cursor cursor, DatabaseHelper db){
        this.context = c;
        this.cursor = cursor;
        this.db = db;
    }

    // ===== UPDATE CURSOR =====
    public void updateCursor(Cursor newCursor){

        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }

        cursor = newCursor;

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_question, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {

        cursor.moveToPosition(position);

        // ===== DATA =====
        final int id =
                cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                );

        final String q =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("question")
                );

        final String optionA =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("optionA")
                );

        final String optionB =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("optionB")
                );

        final String optionC =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("optionC")
                );

        final String optionD =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("optionD")
                );

        final int correct =
                cursor.getInt(
                        cursor.getColumnIndexOrThrow("correct")
                );

        final String subject =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("subject")
                ); // 🔥 THÊM SUBJECT

        final String source =
                cursor.getString(
                        cursor.getColumnIndexOrThrow("source")
                );

        final int status =
                cursor.getInt(
                        cursor.getColumnIndexOrThrow("status")
                );

        h.txtQuestion.setText("[" + source + "][" + subject + "] " + q); // 🔥 HIỂN THỊ SUBJECT

        if(status == 0){

            h.txtAnswer.setText(
                    "⏳ Chưa duyệt | Đáp án: " + correct
            );

        }else{

            h.txtAnswer.setText(
                    "✅ Đã duyệt | Đáp án: " + correct
            );
        }

        // =====================================================
        // DELETE
        // =====================================================

        h.btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(context)

                    .setTitle("Xóa câu hỏi")

                    .setMessage("Bạn chắc chắn muốn xóa?")

                    .setPositiveButton("Xóa", (dialog, which) -> {

                        boolean ok = db.deleteQuestion(id);

                        if(ok){

                            Toast.makeText(
                                    context,
                                    "Đã xóa câu hỏi",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // LOAD LẠI
                            updateCursor(db.getAllQuestions());
                        }else{

                            Toast.makeText(
                                    context,
                                    "Xóa thất bại",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    })

                    .setNegativeButton("Huỷ", null)

                    .show();
        });

        // =====================================================
        // APPROVE
        // =====================================================

        h.btnApprove.setOnClickListener(v -> {

            db.approveQuestion(id);

            Toast.makeText(
                    context,
                    "Đã duyệt câu hỏi",
                    Toast.LENGTH_SHORT
            ).show();

            // LOAD LẠI
            updateCursor(db.getAllQuestions());
        });

        // =====================================================
        // EDIT
        // =====================================================

        h.btnEdit.setOnClickListener(v -> {

            View view = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_add_question, null);

            EditText edtQuestion =
                    view.findViewById(R.id.edtQuestion);

            EditText edtSubject =
                    view.findViewById(R.id.edtSubject); // 🔥 THÊM

            EditText edtA =
                    view.findViewById(R.id.edtA);

            EditText edtB =
                    view.findViewById(R.id.edtB);

            EditText edtC =
                    view.findViewById(R.id.edtC);

            EditText edtD =
                    view.findViewById(R.id.edtD);

            EditText edtCorrect =
                    view.findViewById(R.id.edtCorrect);

            // ===== SET DATA =====

            edtQuestion.setText(q);

            edtSubject.setText(subject); // 🔥 THÊM

            edtA.setText(optionA);

            edtB.setText(optionB);

            edtC.setText(optionC);

            edtD.setText(optionD);

            edtCorrect.setText(String.valueOf(correct));

            new AlertDialog.Builder(context)

                    .setTitle("Sửa câu hỏi")

                    .setView(view)

                    .setPositiveButton("Lưu", (dialog, which) -> {

                        String questionText =
                                edtQuestion.getText()
                                        .toString()
                                        .trim();

                        String subjectText =
                                edtSubject.getText()
                                        .toString()
                                        .trim(); // 🔥 THÊM

                        String A =
                                edtA.getText()
                                        .toString()
                                        .trim();

                        String B =
                                edtB.getText()
                                        .toString()
                                        .trim();

                        String C =
                                edtC.getText()
                                        .toString()
                                        .trim();

                        String D =
                                edtD.getText()
                                        .toString()
                                        .trim();

                        String correctText =
                                edtCorrect.getText()
                                        .toString()
                                        .trim();

                        // ===== VALIDATE =====

                        if(questionText.isEmpty()
                                || subjectText.isEmpty() // 🔥 THÊM
                                || A.isEmpty()
                                || B.isEmpty()
                                || C.isEmpty()
                                || D.isEmpty()
                                || correctText.isEmpty()){

                            Toast.makeText(
                                    context,
                                    "Không được để trống",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        int correctAnswer;

                        try{

                            correctAnswer =
                                    Integer.parseInt(correctText);

                        }catch (Exception e){

                            Toast.makeText(
                                    context,
                                    "Đáp án đúng phải là số",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        // ===== CHECK 0 -> 3 =====

                        if(correctAnswer < 0 || correctAnswer > 3){

                            Toast.makeText(
                                    context,
                                    "Đáp án đúng phải từ 0 đến 3",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        boolean ok = db.updateQuestion(
                                id,
                                questionText,
                                A,
                                B,
                                C,
                                D,
                                correctAnswer,
                                subjectText // 🔥 THÊM SUBJECT
                        );

                        if(ok){

                            Toast.makeText(
                                    context,
                                    "Đã cập nhật",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // LOAD LẠI
                            updateCursor(db.getAllQuestions());

                        }else{

                            Toast.makeText(
                                    context,
                                    "Cập nhật thất bại",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    })

                    .setNegativeButton("Huỷ", null)

                    .show();
        });
    }

    @Override
    public int getItemCount() {

        if(cursor == null){
            return 0;
        }

        return cursor.getCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtQuestion, txtAnswer;

        Button btnEdit, btnDelete, btnApprove;

        public ViewHolder(View v){

            super(v);

            txtQuestion = v.findViewById(R.id.txtQuestion);

            txtAnswer = v.findViewById(R.id.txtAnswer);

            btnEdit = v.findViewById(R.id.btnEdit);

            btnDelete = v.findViewById(R.id.btnDelete);

            btnApprove = v.findViewById(R.id.btnApprove);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(
            RecyclerView recyclerView
    ) {

        super.onDetachedFromRecyclerView(recyclerView);

        if(cursor != null && !cursor.isClosed()){

            cursor.close();
        }
    }
}