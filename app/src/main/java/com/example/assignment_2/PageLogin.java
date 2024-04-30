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
import com.google.firebase.database.Query;
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

    Button btnLogin, btnSignup;
    TextInputLayout textInputLayoutName, textInputLayoutPassword;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_login);

        initViews();

        btnLogin.setOnClickListener(v -> {

            String userNameInput = validateUserName();
            String passwordInput = validatePassword();

            if(userNameInput != null && passwordInput != null) {

                if (userNameInput.equals("admin") && passwordInput.equals("123456")) {

                    Toast.makeText(PageLogin.this, "Login successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PageLogin.this, PageAdmin.class);
                    startActivity(intent);
                } else {
                    matchUserInfo(new OnUserInfoCheckListener() {
                        @Override
                        public void onChecked(boolean validated, String userKey) {
                            if (validated) {

                                Toast.makeText(PageLogin.this, "Login successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PageLogin.this, PageUser.class);
                                intent.putExtra("userKey", userKey);
                                startActivity(intent);
                            } else {

                                textInputLayoutName.setError("Invalid username or password");
                                textInputLayoutPassword.setError("Invalid username or password");
                            }
                        }
                    }, userNameInput, passwordInput);
                }
            }
        });

        btnSignup.setOnClickListener(v -> {

            startActivity(new Intent(PageLogin.this, PageSignUp.class));
        });
    }
    private void initViews() {

        btnLogin = (Button) findViewById(R.id.login_btn_login);
        btnSignup = (Button) findViewById(R.id.login_btn_sign_up);
        textInputLayoutName = findViewById(R.id.login_txt_username);
        textInputLayoutPassword = findViewById(R.id.login_txt_password);
    }

    private String validateUserName() {

        String usernameInput = textInputLayoutName.getEditText().getText().toString().trim();

        if (usernameInput.isEmpty()) {
            textInputLayoutName.setError("Field can't be empty");
            return null;
        } else {
            textInputLayoutName.setError(null);
            return usernameInput;
        }
    }

    private String validatePassword() {

        String passwordInput = textInputLayoutPassword.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()) {
            textInputLayoutPassword.setError("Field can't be empty");
            return null;
        } else {
            textInputLayoutPassword.setError(null);
            return passwordInput;
        }
    }

    private void matchUserInfo(OnUserInfoCheckListener listener, String userNameInput, String passwordInput) {

        myRef = FirebaseDatabase.
                getInstance("https://assignment2-fd51e-default-rtdb.asia-southeast1.firebasedatabase.app/").
                getReference("User");

        Query myQuery = myRef.orderByChild("name").equalTo(userNameInput);
        myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                boolean validated = false;
                String userKey = "";

                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        String passwordStored = snapshot.getValue(User.class).getPassword();
                        userKey = snapshot.getKey();

                        if (passwordStored.equals(passwordInput)) {
                            validated = true;
                        }
                    }
                }

                listener.onChecked(validated, userKey);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(PageLogin.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public interface OnUserInfoCheckListener {
        void onChecked(boolean validated, String userKey);
    }
}
