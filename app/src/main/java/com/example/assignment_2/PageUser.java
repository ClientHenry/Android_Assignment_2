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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PageUser extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton btnReturn;
    Button btnSignOut;
    TextView txtName;
    String userKey, viewType;
    FirebaseDatabase database;
    TabLayout tab;
    HashMap<String, String> quizKeyMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_user);

        userKey = getIntent().getStringExtra("userKey");
        database = FirebaseDatabase.getInstance("https://assignment2-fd51e-default-rtdb.asia-southeast1.firebasedatabase.app/");

        initViews();
        getUser(new OnUserDataListener() {
            @Override
            public void onUserDataReceived(boolean isReceived, HashMap<String, String> quizKeyMap) {
                if (isReceived) {
                    getOngoingQuizzes();
                }
            }
        });
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        getOngoingQuizzes();
                        break;
                    case 1:
                        getUpcomingQuizzes();
                        break;
                    case 2:
                        getParticipatedQuizzes();
                        break;
                    case 3:
                        getPastQuizzes();
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
    }

    private void initViews() {

        recyclerView = findViewById(R.id.user_recycler);
        btnReturn = findViewById(R.id.user_btn_return);
        btnSignOut = findViewById(R.id.user_btn_sign_out);
        txtName = findViewById(R.id.user_txt_name);
        tab = findViewById(R.id.user_tab);
    }

    private void getAllQuizzes(OnQuizDataListener listener) {

        DatabaseReference quizRef = database.getReference("Quiz");
        quizRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                HashMap<String, Quiz> quizMap = new HashMap<>();

                for (DataSnapshot quiz : dataSnapshot.getChildren()) {
                    String key = quiz.getKey();
                    Quiz newQuiz = quiz.getValue(Quiz.class);
                    quizMap.put(key, newQuiz);
                }

                listener.onQuizDataReceived(true, quizMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(PageUser.this, "Failed to get quizzes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setRecycleView(HashMap<String, Quiz> quizMapSelected, String viewType) {

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AdapterQuiz(this, quizMapSelected, viewType, userKey));
    }

    private void getUser(OnUserDataListener listener) {

        DatabaseReference userRef = database.getReference("User").child(userKey);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                txtName.setText("Hi " + user.getName());
                quizKeyMap = user.getQuizKeyMap();

                listener.onUserDataReceived(true, quizKeyMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(PageUser.this, "Failed to get user", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getParticipatedQuizzes() {
        getAllQuizzes(new OnQuizDataListener() {
            @Override
            public void onQuizDataReceived(boolean isReceived, HashMap<String, Quiz> quizMap) {
                if (isReceived) {

                    HashMap<String, Quiz> quizMapParticipated = new HashMap<>();

                    if (quizKeyMap == null) {
                        Toast.makeText(PageUser.this, "Fail to retrieve data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (Map.Entry<String, Quiz> entry : quizMap.entrySet()) {

                        Quiz value = entry.getValue();
                        if (quizKeyMap.containsValue(entry.getKey())) {
                            quizMapParticipated.put(entry.getKey(), value);
                        }
                    }
                    setRecycleView(quizMapParticipated, "detail");
                }
            }
        });
    }

    private void getPastQuizzes() {
        getAllQuizzes(new OnQuizDataListener() {
            @Override
            public void onQuizDataReceived(boolean isReceived, HashMap<String, Quiz> quizMap) {
                if (isReceived) {

                    HashMap<String, Quiz> quizMapPast = new HashMap<>();

                    if (quizKeyMap == null) {
                        Toast.makeText(PageUser.this, "Fail to retrieve data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (Map.Entry<String, Quiz> entry : quizMap.entrySet()) {

                        Quiz value = entry.getValue();
                        String key = entry.getKey();

                        if (value.getEndDate().before(new Date()) && !quizKeyMap.containsValue(key)) {

                            quizMapPast.put(key, value);
                        }
                    }
                    setRecycleView(quizMapPast, "detail");
                }
            }
        });
    }

    private void getOngoingQuizzes() {

        getAllQuizzes(new OnQuizDataListener() {
            @Override
            public void onQuizDataReceived(boolean isReceived, HashMap<String, Quiz> quizMap) {
                if (isReceived) {

                    HashMap<String, Quiz> quizMapOngoing = new HashMap<>();

                    if (quizKeyMap == null) {
                        Toast.makeText(PageUser.this, "Fail to retrieve data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (Map.Entry<String, Quiz> entry : quizMap.entrySet()) {

                        Quiz value = entry.getValue();
                        String key = entry.getKey();

                        if (!quizKeyMap.containsValue(key) && value.getEndDate().after(new Date()) &&
                                value.getStartDate().before(new Date())) {
                            quizMapOngoing.put(key, value);
                        }
                    }
                    setRecycleView(quizMapOngoing, "play");
                }
            }
        });

    }

    //更改类型
    private void getUpcomingQuizzes() {
        getAllQuizzes(new OnQuizDataListener() {
            @Override
            public void onQuizDataReceived(boolean isReceived, HashMap<String, Quiz> quizMap) {
                if (isReceived) {

                    HashMap<String, Quiz> quizMapUpcoming = new HashMap<>();

                    for (Map.Entry<String, Quiz> entry : quizMap.entrySet()) {

                        Quiz value = entry.getValue();
                        if (value.getStartDate().after(new Date())) {
                            quizMapUpcoming.put(entry.getKey(), value);
                        }
                    }

                    setRecycleView(quizMapUpcoming, "play");
                }
            }
        });
    }

    public interface OnQuizDataListener {
        void onQuizDataReceived(boolean isReceived, HashMap<String, Quiz> quizMap);
    }

    public interface OnUserDataListener {
        void onUserDataReceived(boolean isReceived, HashMap<String, String> quizKeyMap);
    }
}
