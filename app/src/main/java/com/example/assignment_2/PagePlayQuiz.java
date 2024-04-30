package com.example.assignment_2;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PagePlayQuiz extends AppCompatActivity {

    ExtendedFloatingActionButton btnNext;
    ArrayList<Quiz.QuestionBean> question = new ArrayList<>();
    private int currentIndex = 0;
    List<Quiz.QuestionBean> questions;
    String quizKey;
    String quizType;
    String userKey;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_play_quiz);

        database = FirebaseDatabase.getInstance("https://assignment-2-e308f-default-rtdb.asia-southeast1.firebasedatabase.app/");

        quizKey = getIntent().getStringExtra("quizKey");
        userKey = getIntent().getStringExtra("userKey");


        getQuestions();



        btnNext = findViewById(R.id.play_quiz_btn_next);

        btnNext.setOnClickListener(v -> {

            currentIndex++;


            if (currentIndex < questions.size()) {

                    showFragmentBoolean(questions.get(currentIndex).getQuestion());


                DatabaseReference myref = database.getReference("User").child(userKey).child("quizzes");
                myref.setValue(quizKey);
                finish();

            }
        });




    }

    private void showFragmentMultiple(String txt1, String txt2, String txt3, String txt4, String txt5) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            FragmentQuestionMultiple fragment = FragmentQuestionMultiple.newInstance(txt1, txt2, txt3, txt4, txt5);

            fragmentTransaction.replace(R.id.play_quiz_quiz_container, fragment);
            fragmentTransaction.commit();

    }

    private void showFragmentBoolean(String question) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            FragmentQuestionBoolean fragment = FragmentQuestionBoolean.newInstance(question);

            fragmentTransaction.replace(R.id.play_quiz_quiz_container, fragment);
            fragmentTransaction.commit();

    }

    private void getQuestions() {

        DatabaseReference myref = database.getReference("Quiz").child(quizKey);
        ValueEventListener QuestionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Quiz quiz = dataSnapshot.getValue(Quiz.class);
                questions = quiz.getQuestions();
/*这里还是回调的问题
               showFragment(questions.get(0).getQuestion(), question.get(0).getCorrect_answer(), questions.get(0).getIncorrect_answers().get(0), questions.get(0).getIncorrect_answers().get(1),
                        questions.get(0).getIncorrect_answers().get(2));*/


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.e(TAG, "loadQuiz:onCancelled", databaseError.toException());
            }
        };
        myref.addValueEventListener(QuestionListener);


    }

}
