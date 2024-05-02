package com.example.assignment_2;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class PagePlayQuiz extends AppCompatActivity implements FragmentQuestion.OnNmeaMessageListener {

    ExtendedFloatingActionButton btnNext;
    List<Quiz.QuestionBean> questions;

    TextView txtTitle;
    private int currentIndex = 0;
    String quizKey, userKey;
    FirebaseDatabase database;

    private String messageFromFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_play_quiz);

        database = FirebaseDatabase.getInstance("https://assignment2-fd51e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        quizKey = getIntent().getStringExtra("quizKey");
        userKey = getIntent().getStringExtra("userKey");
        btnNext = findViewById(R.id.play_quiz_btn_next);
        txtTitle = findViewById(R.id.play_quiz_txt_title);


        getQuestions((isSuccess, questionsRetrieved) -> {
            if (isSuccess) {

                questions = questionsRetrieved;
                btnNext.setEnabled(true);
                showFragmentBoolean(questions.get(currentIndex).getQuestion());
                txtTitle.setText((currentIndex + 1) + "/" + questions.size());
            }
        });

        btnNext.setOnClickListener(v -> {

            String answer = messageFromFragment;
            String correctAnswer = questions.get(currentIndex).getCorrect_answer();

            if(answer ==null){
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            if (answer.equals(correctAnswer)) {
               Toast.makeText(this, "Correct" + correctAnswer, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Incorrect" + correctAnswer, Toast.LENGTH_SHORT).show();
            }


            currentIndex++;


            if (currentIndex < questions.size()) {

                    showFragmentBoolean(questions.get(currentIndex).getQuestion());
                    txtTitle.setText((currentIndex + 1) + "/" + questions.size());

/*

                DatabaseReference myref = database.getReference("User").child(userKey).child("quizzes");
                myref.setValue(quizKey);
                finish();*/

            }

            else {
                DatabaseReference myref = database.getReference("User").child(userKey).child("quizKeyMap");
                myref.push().setValue(quizKey);


                finish();
            }
        });

    }


    private void showFragmentBoolean(String question) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FragmentQuestion fragment = FragmentQuestion.newInstance(question);

        fragmentTransaction.replace(R.id.play_quiz_quiz_container, fragment);
        fragmentTransaction.commit();
    }

    private void getQuestions(OnQuizDataListener listener) {

        DatabaseReference myRef = database.getReference("Quiz").child(quizKey);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Quiz quiz = snapshot.getValue(Quiz.class);
                assert quiz != null;
                List<Quiz.QuestionBean> questions = quiz.getQuestions();
                listener.onQuizDataReceived(true, questions);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(PagePlayQuiz.this, "Failed to get questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface OnQuizDataListener {
        void onQuizDataReceived(boolean isSuccess, List<Quiz.QuestionBean> questions);
    }

    @Override
    public void onNmeaMessage(String message) {
        this.messageFromFragment = message;
    }
}
