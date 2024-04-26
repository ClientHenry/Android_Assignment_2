package com.example.assignment_2;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PageQuizDetail extends AppCompatActivity {

    FloatingActionButton btnReturn;
    TextView txtName, txtDifficulty, txtCategory, txtLike, txtStartDate, txtEndDate;
    RecyclerView rvQuestions;
    List<Quiz.QuestionBean> questions;
    String quizKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_quiz_detail);

        quizKey = getIntent().getStringExtra("quizKey");

        initViews();
        getQuizDetail();

        btnReturn.setOnClickListener(v -> {
            finish();
        });

    }

    private void initViews() {

        txtName = findViewById(R.id.quiz_detail_txt_name);
        txtDifficulty = findViewById(R.id.quiz_detail_txt_difficulty);
        txtCategory = findViewById(R.id.quiz_detail_txt_category);
        txtLike = findViewById(R.id.quiz_detail_txt_like);
        txtStartDate = findViewById(R.id.quiz_detail_txt_start_date);
        txtEndDate = findViewById(R.id.quiz_detail_txt_end_date);
        rvQuestions = findViewById(R.id.quiz_detail_recycler);
        btnReturn = findViewById(R.id.quiz_detail_btn_return);
    }

    private void getQuizDetail() {

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://assignment-2-e308f-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference myref = database.getReference("Quiz").child(quizKey);
        ValueEventListener QuizListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Quiz quiz = dataSnapshot.getValue(Quiz.class);
                txtName.setText(quiz.getName());
                txtDifficulty.setText(quiz.getDifficulty());
                txtCategory.setText(quiz.getCategory());
                txtLike.setText(String.valueOf(quiz.getLikes()));
                txtStartDate.setText(quiz.getStartDate().toString());
                txtEndDate.setText(quiz.getEndDate().toString());
                questions = quiz.getQuestions();

                recyclerView(); //不应该放在这里，还是要接口回调


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.e(TAG, "loadQuiz:onCancelled", databaseError.toException());
            }
        };
        myref.addValueEventListener(QuizListener);

    }

    private void recyclerView() {

        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        AdapterQuestion adapterQuestion = new AdapterQuestion(this, questions);
        rvQuestions.setAdapter(adapterQuestion);

    }

}
