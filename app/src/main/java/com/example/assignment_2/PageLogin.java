package com.example.assignment_2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class PageLogin extends AppCompatActivity {

    Button btnLogin;
    Button btnSignup;
    TextInputLayout textInputLayoutName;
    TextInputLayout textInputLayoutPassword;

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



    }


}
