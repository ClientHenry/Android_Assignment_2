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
    User.Quiz quizKeyList;


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

                    getOngoingQuizzes(quizKeyList);
                }
            }
        });
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        getOngoingQuizzes(quizKeyList);
                        break;
                    case 1:
                        getUpcomingQuizzes();
                        break;
                    case 2:
                        getParticipatedQuizzes(quizKeyList);
                        break;
                    case 3:
                        getPastQuizzes(quizKeyList);
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

                HashMap<String, Quiz> quizList = new HashMap<>();

                for (DataSnapshot quiz : dataSnapshot.getChildren()) {
                    Quiz quizData = quiz.getValue(Quiz.class);
                    quizList.put(quiz.getKey(), quizData);
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
    private void setRecycleView(HashMap<String, Quiz> quizListSelected, String viewType) {

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
                quizKeyList = user.getQuiz();
                listener.onUserDataReceived(true);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(PageUser.this, "Failed to get user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // get the quizzes that the user has participated in
    private void getParticipatedQuizzes(User.Quiz quizKeyList) {
        getAllQuizzes(new OnQuizDataListener() {
            @Override
            public void onQuizDataReceived(boolean isReceived, HashMap<String, Quiz> quizList) {
                if (isReceived) {

                    if (quizKeyList == null) {
                        Toast.makeText(PageUser.this, "Fail to retrieve data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    HashMap<String, Quiz> quizListParticipated = new HashMap<>();
                    for (Map.Entry<String, Quiz> entry : quizList.entrySet()) {
                        if (quizKeyList.getKeyList().contains(entry.getKey())) {
                            quizListParticipated.put(entry.getKey(), entry.getValue());
                        }
                    }

                    setRecycleView(quizListParticipated, "detail");
                }
            }
        });
    }

    // get the quizzes past the end date
    private void getPastQuizzes(User.Quiz quizKeyList) {
        getAllQuizzes(new OnQuizDataListener() {
            @Override
            public void onQuizDataReceived(boolean isReceived, HashMap<String, Quiz> quizList) {
                if (isReceived) {

                    if (quizKeyList == null) {
                        Toast.makeText(PageUser.this, "Fail to retrieve data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    HashMap<String, Quiz> quizListPast = new HashMap<>();
                    for (Map.Entry<String, Quiz> entry : quizList.entrySet()) {
                        if (!quizKeyList.getKeyList().contains(entry.getKey()) && entry.getValue().getEndDate().before(new Date())) {
                            quizListPast.put(entry.getKey(), entry.getValue());
                        }
                    }

                    setRecycleView(quizListPast, "detail");
                }
            }
        });
    }

    // get the quizzes that are ongoing but the user has not participated in
    private void getOngoingQuizzes(User.Quiz quizKeyList) {
        getAllQuizzes(new OnQuizDataListener() {
            @Override
            public void onQuizDataReceived(boolean isReceived, HashMap<String, Quiz> quizList) {
                if (isReceived) {

                    if (quizKeyList == null) {
                        Toast.makeText(PageUser.this, "Fail to retrieve data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    HashMap<String, Quiz> quizListOngoing = new HashMap<>();
                    for (Map.Entry<String, Quiz> entry : quizList.entrySet()) {
                        if (!quizKeyList.getKeyList().contains(entry.getKey()) && entry.getValue().getEndDate().after(new Date()) &&
                                entry.getValue().getStartDate().before(new Date())) {
                            quizListOngoing.put(entry.getKey(), entry.getValue());
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
            public void onQuizDataReceived(boolean isReceived, HashMap<String, Quiz> quizList) {
                if (isReceived) {

                    HashMap<String, Quiz> quizListUpcoming = new HashMap<>();
                    for (Map.Entry<String, Quiz> entry : quizList.entrySet()) {
                        if (entry.getValue().getStartDate().after(new Date())) {
                            quizListUpcoming.put(entry.getKey(), entry.getValue());
                        }
                    }

                    setRecycleView(quizListUpcoming, "");
                }
            }
        });
    }

    // interface for the quiz data listener
    public interface OnQuizDataListener {
        void onQuizDataReceived(boolean isReceived, HashMap<String, Quiz> quizList);
    }

    // interface for the user data listener
    public interface OnUserDataListener {
        void onUserDataReceived(boolean isReceived);
    }
}
