package com.example.assignment_2;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class PageAdmin extends AppCompatActivity {
    Button btnCreateQuiz, btnSignOut;
    FloatingActionButton btnReturn;
    RecyclerView rvQuizzes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_admin);

        getAllQuizzes((success, quizList) -> {

            if (success) {
                recyclerView(quizList);
            }
        });

        btnCreateQuiz = (Button) findViewById(R.id.admin_btn_create_quiz);
        btnCreateQuiz.setOnClickListener(v -> {

            Intent intent = new Intent(PageAdmin.this, PageCreateQuiz.class);
            startActivity(intent);
        });

        btnSignOut = (Button) findViewById(R.id.admin_btn_sign_out);
        btnSignOut.setOnClickListener(v -> {

            Intent intent = new Intent(PageAdmin.this, PageLogin.class);
            startActivity(intent);
            finish();
        });

        btnReturn = (FloatingActionButton) findViewById(R.id.admin_btn_return);
        btnReturn.setOnClickListener(v -> {

           finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllQuizzes((success, quizList) -> {

            if (success) {
                recyclerView(quizList);
            }
        });
    }
    private void getAllQuizzes(OnDataListener listener) {

        DatabaseReference myRef = FirebaseDatabase
                .getInstance("https://assignment2-fd51e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Quiz");

        HashMap<String, Quiz> quizList = new HashMap<>();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot quiz : snapshot.getChildren()) {

                    Quiz newQuiz = quiz.getValue(Quiz.class);
                    quizList.put(quiz.getKey(), newQuiz);
                }

                listener.onDataRetrieved(true, quizList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PageAdmin.this, "Error retrieving data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recyclerView(HashMap<String, Quiz> quizList) {

        rvQuizzes = findViewById(R.id.admin_recyclerview);
        rvQuizzes.setLayoutManager(new LinearLayoutManager(this));
        rvQuizzes.setAdapter(new AdapterQuiz(this, quizList, "admin", null));
    }

    public interface OnDataListener {
        void onDataRetrieved(boolean success, HashMap<String, Quiz> quizList);
    }
}
