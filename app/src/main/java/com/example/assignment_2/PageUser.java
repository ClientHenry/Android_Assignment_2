package com.example.assignment_2;

import static android.content.ContentValues.TAG;

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
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PageUser extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton btnReturn;
    Button btnSignOut;
    HashMap<String, Quiz> quizMap;
    TextView txtName;
    String userKey, viewType;
    FirebaseDatabase database;
    User user;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_user);

        userKey = getIntent().getStringExtra("userKey");
        database = FirebaseDatabase.getInstance("https://assignment-2-e308f-default-rtdb.asia-southeast1.firebasedatabase.app/");


        getAllQuizzes();
        getUser();


        recyclerView = findViewById(R.id.user_recycler);
        btnReturn = findViewById(R.id.user_btn_return);
        btnSignOut = (Button) findViewById(R.id.user_btn_sign_out);
        txtName = findViewById(R.id.user_txt_name);




        TabLayout tab = findViewById(R.id.user_tab);
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                int position = tab.getPosition();
                switch(position){
                    case 0:

                        HashMap<String, Quiz> quizMapOngoing = new HashMap<>();
                        for(Map.Entry<String, Quiz> entry : quizMap.entrySet()){

                            Quiz value = entry.getValue();
                            if(value.getEndDate().after(new Date()) && value.getStartDate().before(new Date())){
                                quizMapOngoing.put(entry.getKey(), value);
                            }

                        }

                        setRecycleView(quizMapOngoing, "play");


                        break;
                    case 1:
                        HashMap<String, Quiz> quizMapUpcoming = new HashMap<>();
                        for(Map.Entry<String, Quiz> entry : quizMap.entrySet()){

                            Quiz value = entry.getValue();
                            if(value.getStartDate().after(new Date())){
                                quizMapUpcoming.put(entry.getKey(), value);
                            }

                        }

                        setRecycleView(quizMapUpcoming, "detail");

                        break;

                    case 2:

                        HashMap<String, Quiz> quizMapParticipated = new HashMap<>();

            /*            Toast.makeText(PageUser.this, user.quizzes.get(0), Toast.LENGTH_SHORT).show();


                        user.quizzes.forEach(quizKey -> {
                            Quiz quiz = quizMap.get(quizKey);
                            quizMapParticipated.put(quizKey, quiz);
                        });*/ //如何让这个代码自动生成arraylist

                        setRecycleView(quizMapParticipated, "detail");






                        break;
                    case 3:

                        HashMap<String, Quiz> quizMapUpPast = new HashMap<>();
                        for(Map.Entry<String, Quiz> entry : quizMap.entrySet()){

                            Quiz value = entry.getValue();
                            if(value.getEndDate().before(new Date())){
                                quizMapUpPast.put(entry.getKey(), value);
                            }

                        }

                        setRecycleView(quizMapUpPast, "detail");


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



        btnReturn.setOnClickListener(v -> {
            finish();
        });
    }

    private void getAllQuizzes(){

        DatabaseReference quizRef = database.getReference("Quiz");
        quizMap = new HashMap<>();
        quizRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot quiz: dataSnapshot.getChildren()) {
                    String key = quiz.getKey();
                    Quiz newQuiz = quiz.getValue(Quiz.class);
                    quizMap.put(key, newQuiz);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });


    }

    private void setRecycleView(HashMap<String, Quiz> quizMapSelected, String viewType){

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AdapterQuiz(this, quizMapSelected, viewType, userKey));

    }

    private void getUser(){

        DatabaseReference userRef = database.getReference("User").child(userKey);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                txtName.setText("Hi " + user.getName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });


    }



}
