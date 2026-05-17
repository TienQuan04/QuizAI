package com.example.QuizzAI;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class QuestionManagementActivity extends AppCompatActivity {

    RecyclerView recycler;
    DatabaseHelper db;
    QuestionAdapter adapter;

    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_management);

        recycler = findViewById(R.id.recycler);

        db = new DatabaseHelper(this);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        loadData();

        findViewById(R.id.btnAdd).setOnClickListener(v -> showAddDialog());
    }

    private void loadData(){

        // FIX CURSOR LEAK
        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }

        cursor = db.getAllQuestions();

        if(adapter == null){

            adapter = new QuestionAdapter(this, cursor, db);

            recycler.setAdapter(adapter);

        }else{

            adapter.updateCursor(cursor);
        }
    }

    private void showAddDialog(){

        View view = getLayoutInflater().inflate(R.layout.dialog_add_question, null);

        EditText q = view.findViewById(R.id.edtQuestion);
        EditText subject = view.findViewById(R.id.edtSubject); //  THÊM
        EditText A = view.findViewById(R.id.edtA);
        EditText B = view.findViewById(R.id.edtB);
        EditText C = view.findViewById(R.id.edtC);
        EditText D = view.findViewById(R.id.edtD);
        EditText correct = view.findViewById(R.id.edtCorrect);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm câu hỏi")
                .setView(view)
                .setPositiveButton("OK", null)
                .setNegativeButton("Huỷ", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {

                    String correctStr =
                            correct.getText().toString().trim();

                    if(correctStr.isEmpty()){

                        correct.setError("Nhập đáp án đúng");
                        return;
                    }

                    boolean ok = db.insertQuestion(
                            q.getText().toString().trim(),
                            A.getText().toString().trim(),
                            B.getText().toString().trim(),
                            C.getText().toString().trim(),
                            D.getText().toString().trim(),
                            Integer.parseInt(correctStr),
                            subject.getText().toString().trim(), //  THÊM SUBJECT
                            "ADMIN"
                    );

                    if(ok){

                        Toast.makeText(
                                this,
                                "Đã thêm",
                                Toast.LENGTH_SHORT
                        ).show();

                        // KHÔNG recreate()
                        loadData();

                        dialog.dismiss();

                    }else{

                        Toast.makeText(
                                this,
                                "Thêm thất bại",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }
    }
}