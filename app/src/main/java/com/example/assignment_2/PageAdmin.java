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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class PageAdmin extends AppCompatActivity {
    Button btnCreateQuiz, btnSignOut;
    FloatingActionButton btnReturn;
    RecyclerView rvQuizzes;
    HashMap<String, Quiz> quizMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_admin);

        getAllQuizzes();

        btnCreateQuiz = (Button) findViewById(R.id.admin_btn_create_quiz);
        btnCreateQuiz.setOnClickListener(v -> {

            Intent intent = new Intent(PageAdmin.this, PageCreateQuiz.class);
            startActivity(intent);
        });

        btnSignOut = (Button) findViewById(R.id.admin_btn_sign_out);
        btnSignOut.setOnClickListener(v -> {

            Intent intent = new Intent(PageAdmin.this, PageLogin.class);
            startActivity(intent);
        });

        btnReturn = (FloatingActionButton) findViewById(R.id.admin_btn_return);
        btnReturn.setOnClickListener(v -> {

           finish();
        });
    }

    private void getAllQuizzes() {

        DatabaseReference myRef = FirebaseDatabase
                .getInstance("https://assignment2-fd51e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Quiz");

        quizMap = new HashMap<>();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot quiz : dataSnapshot.getChildren()) {

                    String key = quiz.getKey();
                    Quiz newQuiz = quiz.getValue(Quiz.class);
                    quizMap.put(key, newQuiz);
                }
                recyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(PageAdmin.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recyclerView() {

        rvQuizzes = findViewById(R.id.admin_recyclerview);
        rvQuizzes.setLayoutManager(new LinearLayoutManager(this));
        rvQuizzes.setAdapter(new AdapterQuiz(this, quizMap, "detail", null));
    }
}
