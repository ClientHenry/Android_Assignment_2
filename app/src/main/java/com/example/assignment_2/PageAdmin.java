package com.example.assignment_2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PageAdmin extends AppCompatActivity {

    Button btnCreateQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_admin);

        btnCreateQuiz = (Button) findViewById(R.id.admin_btn_create_quiz);
        btnCreateQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(PageAdmin.this, PageCreateQuiz.class);
            startActivity(intent);
        });
    }

}
