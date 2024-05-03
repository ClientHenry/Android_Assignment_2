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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class PagePlayQuiz extends AppCompatActivity implements FragmentQuestion.OnNmeaMessageListener {

    ExtendedFloatingActionButton btnNext;
    List<Quiz.QuestionBean> questions;
    TextView txtTitle;
    private int currentIndex = 0;
    String quizName, userKey;
    FirebaseDatabase database;
    private String messageFromFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_play_quiz);

        database = FirebaseDatabase.getInstance("https://assignment2-fd51e-default-rtdb.asia-southeast1.firebasedatabase.app/");

        initViews();

        getQuestions((isSuccess, questionsRetrieved) -> {

            if (isSuccess) {
                questions = questionsRetrieved;
                btnNext.setEnabled(true);
                showFragment(questions.get(currentIndex).getQuestion());
                txtTitle.setText((currentIndex + 1) + "/" + questions.size());
            }
        });

        btnNext.setOnClickListener(v -> {

            String answer = messageFromFragment;
            String correctAnswer = questions.get(currentIndex).getCorrect_Answer();

            if (answer == null) {
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

                showFragment(questions.get(currentIndex).getQuestion());
                txtTitle.setText((currentIndex + 1) + "/" + questions.size());

            } else {

                updateUser();
                finish();
            }
        });
    }

    private void initViews() {

        quizName = getIntent().getStringExtra("quizName");
        userKey = getIntent().getStringExtra("userKey");
        btnNext = findViewById(R.id.play_quiz_btn_next);
        txtTitle = findViewById(R.id.play_quiz_txt_title);
    }

    // display questions in different pages by implementing the fragment function
    private void showFragment(String question) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FragmentQuestion fragment = FragmentQuestion.newInstance(question);

        fragmentTransaction.replace(R.id.play_quiz_quiz_container, fragment);
        fragmentTransaction.commit();
    }

    // get the message from the fragment
    @Override
    public void onNmeaMessage(String message) {

        this.messageFromFragment = message;
    }

    // get questions from the database by querying the quiz name which is passed from the previous activity
    private void getQuestions(OnQuizDataListener listener) {

        Query query = database.getReference("Quiz").orderByChild("name").equalTo(quizName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    Toast.makeText(PagePlayQuiz.this, "No Quiz found", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Quiz quiz = dataSnapshot.getValue(Quiz.class);
                    assert quiz != null;
                    List<Quiz.QuestionBean> questions = quiz.getQuestions();
                    listener.onQuizDataReceived(true, questions);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(PagePlayQuiz.this, "Failed to get Quiz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // update the user's quiz list after the user has completed the quiz
    private void updateUser() {
        DatabaseReference userRef = database.getReference("User").child(userKey);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;

                user.getQuiz().getNameList().add(quizName);
                userRef.setValue(user);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(PagePlayQuiz.this, "Failed to update user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // interface to get the questions from the database
    public interface OnQuizDataListener {
        void onQuizDataReceived(boolean isSuccess, List<Quiz.QuestionBean> questions);
    }
}
