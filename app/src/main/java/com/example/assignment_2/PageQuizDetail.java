package com.example.assignment_2;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PageQuizDetail extends AppCompatActivity {

    // quizName is used to get the quiz detail from the database
    // viewType is used to determine the view type of the activity(admin or user)
    FloatingActionButton btnReturn;
    TextView txtName, txtDifficulty, txtCategory, txtLike, txtStartDate, txtEndDate;
    Button btnEdit;
    RecyclerView rvQuestions;
    String quizKey, viewType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_quiz_detail);

        quizKey = getIntent().getStringExtra("quizKey");
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

        btnEdit.setOnClickListener(v -> {

            if (btnEdit.getVisibility() == Button.VISIBLE) {

                Intent intent = new Intent(PageQuizDetail.this, PageEditQuiz.class);
                intent.putExtra("quizKey", quizKey);
                startActivity(intent);
            }
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

        btnEdit = findViewById(R.id.quiz_detail_btn_edit);
        if (viewType.equals("admin")) {
            btnEdit.setVisibility(Button.VISIBLE);
        } else {
            btnEdit.setVisibility(Button.GONE);
        }
    }

    // get the quiz detail from the database
    private void getQuizDetail(OnQuizDataListener listener) {

        DatabaseReference myRef = FirebaseDatabase.getInstance("https://assignment2-fd51e-default-rtdb.asia-southeast1.firebasedatabase.app/").
                getReference("Quiz").child(quizKey);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {

                    finish();
                    return;
                }
                Quiz quiz = dataSnapshot.getValue(Quiz.class);
                assert quiz != null;
                txtName.setText(quiz.getName());
                txtCategory.setText(quiz.getCategory());
                txtDifficulty.setText("Difficulty: " + quiz.getDifficulty());
                txtLike.setText(String.valueOf(quiz.getLikes()));
                txtStartDate.setText("Start Date: " + new SimpleDateFormat("MMM. d", Locale.ENGLISH).format(quiz.getStartDate()));
                txtEndDate.setText("End Date: " + new SimpleDateFormat("MMM. d", Locale.ENGLISH).format(quiz.getEndDate()));
                ArrayList<Quiz.QuestionBean> questions = (ArrayList<Quiz.QuestionBean>) quiz.getQuestions();
                listener.onQuizDataReceived(true, questions);

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
