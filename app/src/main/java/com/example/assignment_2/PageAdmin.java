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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PageAdmin extends AppCompatActivity {

    Button btnCreateQuiz;
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
    }

    private void getAllQuizzes(){

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://assignment-2-e308f-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference myref = database.getReference("Quiz");
        quizMap = new HashMap<>();

        // My top posts by number of stars
        myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot quiz: dataSnapshot.getChildren()) {
                    String key = quiz.getKey();
                    Quiz newQuiz = quiz.getValue(Quiz.class);
                    quizMap.put(key, newQuiz);
                }

                recyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });


    }

    private void recyclerView() {

        rvQuizzes = findViewById(R.id.admin_recyclerview);
        rvQuizzes.setLayoutManager(new LinearLayoutManager(this));
        rvQuizzes.setAdapter(new AdapterAdmin(this, quizMap));

    }

}
