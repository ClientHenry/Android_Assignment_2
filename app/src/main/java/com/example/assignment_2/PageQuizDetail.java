package com.example.assignment_2;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class PageQuizDetail extends AppCompatActivity {

    FloatingActionButton btnReturn;
    TextView txtName, txtDifficulty, txtCategory, txtLike, txtStartDate, txtEndDate;
    RecyclerView rvQuestions;
    String quizName;
    String viewType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_quiz_detail);

        quizName = getIntent().getStringExtra("quizName");
        viewType = getIntent().getStringExtra("viewType");


        initViews();
        getQuizDetail((isSuccess, questions) -> {
            if (isSuccess) {
                recyclerView((ArrayList<Quiz.QuestionBean>) questions);
            }
        });

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

    private void getQuizDetail(OnQuizDataListener listener) {

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://assignment2-fd51e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        Query query = database.getReference("Quiz").orderByChild("name").equalTo(quizName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    Toast.makeText(PageQuizDetail.this, "Quiz not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Quiz quiz = child.getValue(Quiz.class);
                    assert quiz != null;
                    txtName.setText(quiz.getName());
                    txtDifficulty.setText(quiz.getDifficulty());
                    txtCategory.setText(quiz.getCategory());
                    txtLike.setText(String.valueOf(quiz.getLikes()));
                    txtStartDate.setText(quiz.getStartDate().toString());
                    txtEndDate.setText(quiz.getEndDate().toString());
                    ArrayList<Quiz.QuestionBean> questions = (ArrayList<Quiz.QuestionBean>) quiz.getQuestions();
                    listener.onQuizDataReceived(true, questions);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "loadQuiz:onCancelled", databaseError.toException());
            }
        });

    }

    private void recyclerView(ArrayList<Quiz.QuestionBean> questions) {

        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        AdapterQuestion adapterQuestion = new AdapterQuestion(this, questions);
        rvQuestions.setAdapter(adapterQuestion);

    }

    public interface OnQuizDataListener {
        void onQuizDataReceived(boolean isSuccess, List<Quiz.QuestionBean> questions);
    }

}
