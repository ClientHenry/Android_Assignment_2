package com.example.assignment_2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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

public class PagePlayQuiz extends AppCompatActivity implements FragmentQuestion.OnNmeaMessageListener {

    Button btnNext;
    FloatingActionButton btnReturn;
    List<Quiz.QuestionBean> questions;
    Quiz quiz;
    TextView txtTitle;
    private int currentIndex = 0;
    private int score = 0;
    String quizKey, userKey;
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
            }
        });

        btnNext.setOnClickListener(v -> {

            String answer = messageFromFragment;
            String correctAnswer = questions.get(currentIndex).getCorrect_Answer();

            if (answer == null) {

                txtTitle.setText("Select an answer!");
                txtTitle.setTextColor(getResources().getColor(R.color.quiz_no_input));
                return;
            }
            if (answer.equals(correctAnswer)) {

                txtTitle.setText("Correct!");
                txtTitle.setTextColor(getResources().getColor(R.color.quiz_correct));
                score++;

            } else {

                txtTitle.setText("Incorrect!\n The correct answer is " + correctAnswer);
                txtTitle.setTextColor(getResources().getColor(R.color.quiz_incorrect));
            }
            messageFromFragment = null;

            currentIndex++;

            if (currentIndex < questions.size()) {

                showFragment(questions.get(currentIndex).getQuestion());

            } else {
                btnNext.setEnabled(false);
                updateUser();
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setTitle("You score: " + score + " out of 10").setMessage("Like this quiz?").
                        setPositiveButton("Yes", (dialog, which) -> {
                            updateQuiz();
                            finish();
                        }).setNegativeButton("No", (dialog, which) -> {
                            finish();
                        }).setOnCancelListener(dialog -> {
                            finish();
                        }).show();
            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }

    private void initViews() {

        quizKey = getIntent().getStringExtra("quizKey");
        userKey = getIntent().getStringExtra("userKey");
        btnNext = findViewById(R.id.play_quiz_btn_next);
        txtTitle = findViewById(R.id.play_quiz_txt_title);
        btnReturn = findViewById(R.id.play_quiz_btn_return);
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

        DatabaseReference myRef = database.getReference("Quiz").child(quizKey);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    Toast.makeText(PagePlayQuiz.this, "No Quiz found", Toast.LENGTH_SHORT).show();
                    return;
                }
                quiz = snapshot.getValue(Quiz.class);
                assert quiz != null;
                List<Quiz.QuestionBean> questions = quiz.getQuestions();
                listener.onQuizDataReceived(true, questions);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(PagePlayQuiz.this, "Failed to get Quiz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQuiz() {

        quiz.setLikes(quiz.getLikes() + 1);
        database.getReference("Quiz").child(quizKey).setValue(quiz);
        Toast.makeText(this, "Quiz updated", Toast.LENGTH_SHORT).show();
    }

    // update the user's quiz list after the user has completed the quiz
    private void updateUser() {
        DatabaseReference userRef = database.getReference("User").child(userKey);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;

                user.getQuiz().getKeyList().add(quizKey);
                userRef.setValue(user);
                Toast.makeText(PagePlayQuiz.this, "User updated", Toast.LENGTH_SHORT).show();
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
