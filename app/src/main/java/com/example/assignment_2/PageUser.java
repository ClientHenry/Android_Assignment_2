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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PageUser extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton btnReturn;
    Button btnSignOut, btnEdit;
    TextView txtName;
    String userKey;
    FirebaseDatabase database;
    TabLayout tab;
    User.Quiz quizNameList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_user);

        userKey = getIntent().getStringExtra("userKey");
        database = FirebaseDatabase.getInstance("https://assignment2-fd51e-default-rtdb.asia-southeast1.firebasedatabase.app/");

        initViews();
        getUser(new OnUserDataListener() {
            @Override
            public void onUserDataReceived(boolean isReceived) {
                if (isReceived) {

                    getOngoingQuizzes(quizNameList);
                }
            }
        });
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        getOngoingQuizzes(quizNameList);
                        break;
                    case 1:
                        getUpcomingQuizzes();
                        break;
                    case 2:
                        getParticipatedQuizzes(quizNameList);
                        break;
                    case 3:
                        getPastQuizzes(quizNameList);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselect
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselect
            }
        });

        btnSignOut.setOnClickListener(v -> {

            Intent intent = new Intent(PageUser.this, PageLogin.class);
            startActivity(intent);
        });

        btnReturn.setOnClickListener(v -> {
            finish();
        });

        btnEdit.setOnClickListener(v -> {

            Intent intent = new Intent(PageUser.this, PageEditUser.class);
            intent.putExtra("userKey", userKey);
            startActivity(intent);
        });

    }

    private void initViews() {

        recyclerView = findViewById(R.id.user_recycler);
        btnReturn = findViewById(R.id.user_btn_return);
        btnSignOut = findViewById(R.id.user_btn_sign_out);
        btnEdit = findViewById(R.id.user_btn_edit);
        txtName = findViewById(R.id.user_txt_name);
        tab = findViewById(R.id.user_tab);
    }

    // retrieve all quizzes from the database for further processing
    private void getAllQuizzes(OnQuizDataListener listener) {

        DatabaseReference quizRef = database.getReference("Quiz");
        quizRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<Quiz> quizList = new ArrayList<>();

                for (DataSnapshot quiz : dataSnapshot.getChildren()) {
                    Quiz newQuiz = quiz.getValue(Quiz.class);
                    quizList.add(newQuiz);
                }
                listener.onQuizDataReceived(true, quizList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(PageUser.this, "Failed to get quizzes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // set the recycle view with the selected quizzes and the view type
    private void setRecycleView(ArrayList<Quiz> quizListSelected, String viewType) {

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AdapterQuiz(this, quizListSelected, viewType, userKey));
    }

    // retrieve the user data from the database by the user key
    private void getUser(OnUserDataListener listener) {

        DatabaseReference myrRef = database.getReference("User").child(userKey);
        myrRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                User user = dataSnapshot.getValue(User.class);

                if (user == null) {
                    Toast.makeText(PageUser.this, "Fail to retrieve user data", Toast.LENGTH_SHORT).show();
                    return;
                }

                String name = user.getName();
                if (name.isEmpty()) {
                    name = "there";
                }
                txtName.setText("Hi " + name);
                quizNameList = user.getQuiz();
                listener.onUserDataReceived(true);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(PageUser.this, "Failed to get user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // get the quizzes that the user has participated in
    private void getParticipatedQuizzes(User.Quiz quizNameList) {
        getAllQuizzes(new OnQuizDataListener() {
            @Override
            public void onQuizDataReceived(boolean isReceived, ArrayList<Quiz> quizList) {
                if (isReceived) {

                    if (quizNameList == null) {
                        Toast.makeText(PageUser.this, "Fail to retrieve data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ArrayList<Quiz> quizListParticipated = new ArrayList<>();
                    for (Quiz quiz : quizList) {
                        if (quizNameList.getNameList().contains(quiz.getName())) {
                            Toast.makeText(PageUser.this, "Participated: " + quiz.getName(), Toast.LENGTH_SHORT).show();
                            quizListParticipated.add(quiz);
                        }
                    }

                    setRecycleView(quizListParticipated, "detail");
                }
            }
        });
    }

    // get the quizzes past the end date
    private void getPastQuizzes(User.Quiz quizNameList) {
        getAllQuizzes(new OnQuizDataListener() {
            @Override
            public void onQuizDataReceived(boolean isReceived, ArrayList<Quiz> quizList) {
                if (isReceived) {

                    if (quizList == null) {
                        Toast.makeText(PageUser.this, "Fail to retrieve data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ArrayList<Quiz> quizListPast = new ArrayList<>();
                    for (Quiz quiz : quizList) {
                        if (!quizNameList.getNameList().contains(quiz.getName()) && quiz.getEndDate().before(new Date())) {
                            quizListPast.add(quiz);
                        }
                    }
                    setRecycleView(quizListPast, "detail");
                }
            }
        });
    }

    // get the quizzes that are ongoing but the user has not participated in
    private void getOngoingQuizzes(User.Quiz quizNameList) {
        getAllQuizzes(new OnQuizDataListener() {
            @Override
            public void onQuizDataReceived(boolean isReceived, ArrayList<Quiz> quizList) {
                if (isReceived) {

                    if (quizList == null) {
                        Toast.makeText(PageUser.this, "Fail to retrieve data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ArrayList<Quiz> quizListOngoing = new ArrayList<>();
                    for (Quiz quiz : quizList) {
                        if (!quizNameList.getNameList().contains(quiz.getName()) && quiz.getEndDate().after(new Date()) &&
                                quiz.getStartDate().before(new Date())) {
                            quizListOngoing.add(quiz);
                        }
                    }
                    setRecycleView(quizListOngoing, "play");
                }
            }
        });
    }

    // get the quizzes that are upcoming
    private void getUpcomingQuizzes() {
        getAllQuizzes(new OnQuizDataListener() {
            @Override
            public void onQuizDataReceived(boolean isReceived, ArrayList<Quiz> quizList) {
                if (isReceived) {

                    ArrayList<Quiz> quizListUpcoming = new ArrayList<>();
                    for (Quiz quiz : quizList) {
                        if (quiz.getStartDate().after(new Date())) {
                            quizListUpcoming.add(quiz);
                        }
                    }
                    setRecycleView(quizListUpcoming, "");
                }
            }
        });
    }

    // interface for the quiz data listener
    public interface OnQuizDataListener {
        void onQuizDataReceived(boolean isReceived, ArrayList<Quiz> quizList);
    }

    // interface for the user data listener
    public interface OnUserDataListener {
        void onUserDataReceived(boolean isReceived);
    }
}
