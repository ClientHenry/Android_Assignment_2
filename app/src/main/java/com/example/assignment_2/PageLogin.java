package com.example.assignment_2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PageLogin extends AppCompatActivity {

    Button btnLogin;
    Button btnSignup;
    TextInputLayout textInputLayoutName;
    TextInputLayout textInputLayoutPassword;

    Quiz quiz;
    String name;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_login);

        btnLogin = (Button) findViewById(R.id.login_btn_login);
        btnSignup = (Button) findViewById(R.id.login_btn_sign_up);
        textInputLayoutName = findViewById(R.id.login_txt_username);
        textInputLayoutPassword = findViewById(R.id.login_txt_password);


        btnLogin.setOnClickListener(v -> {
            String name = textInputLayoutName.getEditText().getText().toString();
            String password = textInputLayoutPassword.getEditText().getText().toString();
            if (name.equals("admin") && password.equals("123456")) {

                startActivity(new Intent(PageLogin.this, PageAdmin.class));
            } else {
                // Show error message
                textInputLayoutName.setError("Invalid username or password");
                textInputLayoutPassword.setError("Invalid username or password");
            }
        });

        btnSignup.setOnClickListener(v -> {
            startActivity(new Intent(PageLogin.this, PageSignUp.class));
        });

  //      getQuiz();
  //      getDBUser();

    }

    private void getDBUser() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://assignment-2-e308f-default-rtdb.asia-southeast1.firebasedatabase.app/");

        myRef = database.getReference("User");




   //     getQuiz();

        // Read from the database


     //   myRef.setValue(name);


/*
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
            //   Quiz.ResultsBean value = dataSnapshot.getValue(Quiz.ResultsBean.class);

             //   Toast.makeText(PageLogin.this, value.getCategory(), Toast.LENGTH_SHORT).show();



            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });*/


    }

    private  void getQuiz() {


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuizService service = retrofit.create(QuizService.class);
        Call<Quiz> quizCall = service.getQuiz(10,0, null, null);

        quizCall.enqueue(new Callback<Quiz>(){

            @Override
            public void onResponse(Call<Quiz> call, Response<Quiz> response) {
            quiz = response.body();
        //   name = quiz.getResults().get(0).getCategory();
         //  Toast.makeText(PageLogin.this, quiz.getResults().get(0).getCategory(), Toast.LENGTH_SHORT).show();
           myRef.setValue(quiz);
            //    List<Quiz.ResultsBean> quiz = response.body().getResults();

         //       quiz = response.body().getResults();

          //      response.body().setName("My Quiz");
             //   String name = response.body().getName();
          //
              //  Quiz.ResultsBean q = quiz.get(0);
           //

            }

            @Override
            public void onFailure(Call<Quiz> call, Throwable t) {

            }
        });


}}
