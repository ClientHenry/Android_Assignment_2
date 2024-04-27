package com.example.assignment_2;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    DataReturned quiz;
    String name;
    DatabaseReference myRef;
    HashMap<String, User> userMap;
    List<User> userList;

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
            } else if (checkUser(name, password) != null) {

                Intent intent = new Intent(PageLogin.this, PageUser.class);
                intent.putExtra("userKey", checkUser(name, password));
                startActivity(intent);

            } else {
                // Show error message
                textInputLayoutName.setError("Invalid username or password");
                textInputLayoutPassword.setError("Invalid username or password");
            }
        });


        btnSignup.setOnClickListener(v ->

        {
            startActivity(new Intent(PageLogin.this, PageSignUp.class));
        });

        getUsers();


    }

    private void getUsers() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://assignment-2-e308f-default-rtdb.asia-southeast1.firebasedatabase.app/");

        myRef = database.getReference("User");

        userMap = new HashMap<>();
        userList = new ArrayList<>();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot user : dataSnapshot.getChildren()) {

                    String key = user.getKey();
                    User newUser = user.getValue(User.class);
                    userMap.put(key, newUser);
                }

                for (User user : userMap.values()) {
                    userList.add(user);
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


    private String checkUser(String name, String password) {

        String userKey = null;
        for (Map.Entry<String, User> entry : userMap.entrySet()) {
            String key = entry.getKey();
            User user = entry.getValue();

            if (user.getName().equals(name) && user.getPassword().equals(password)) {
                userKey = key;
                break;
            }
        }

        return userKey;
    }
}
