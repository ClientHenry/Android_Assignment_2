package com.example.assignment_2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PageCreateQuiz extends AppCompatActivity {

    TextInputLayout txtLayoutQuizName;
    TextInputLayout txtLayoutQuizDifficulty;
    TextInputLayout txtLayoutQuizCategory;
    TextView txtDate;
    Button btnCreateQuiz;
    Button btnDatePicker;
    FloatingActionButton btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_create_quiz);

        txtLayoutQuizName = findViewById(R.id.create_quiz_txt_name);
        txtLayoutQuizDifficulty = findViewById(R.id.create_quiz_txt_difficulty);
        txtLayoutQuizCategory = findViewById(R.id.create_quiz_txt_category);
        txtDate = findViewById(R.id.create_quiz_txt_date);
        btnCreateQuiz = findViewById(R.id.create_quiz_btn_create);
        btnDatePicker = findViewById(R.id.create_quiz_btn_datepicker);
        btnReturn = findViewById(R.id.create_quiz_btn_return);

        btnDatePicker.setOnClickListener(v -> {
            MaterialDatePicker dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select dates").build();

            dateRangePicker.addOnPositiveButtonClickListener(selection -> {

                Pair<Long, Long> dateSelected = (Pair<Long, Long>) dateRangePicker.getSelection();
                Date startDateSelected = new Date(dateSelected.first);
                Date endDateSelected = new Date(dateSelected.second);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM. d", Locale.ENGLISH);
                String startDateformatted = sdf.format(startDateSelected);
                String endDateformatted = sdf.format(endDateSelected);

                txtDate.setText(startDateformatted + " - " + endDateformatted);

            });

            dateRangePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        String[] items = {"Item 1", "Item 2", "Item 3", "Item 4"};
        MaterialAutoCompleteTextView txtItems = (MaterialAutoCompleteTextView) txtLayoutQuizDifficulty.getEditText();
        if (txtItems instanceof MaterialAutoCompleteTextView) {
            txtItems.setSimpleItems(items);
        }

        //category follow the same logic as difficulty

        btnCreateQuiz.setOnClickListener(v -> {

            String quizDifficulty = txtLayoutQuizDifficulty.getEditText().getText().toString();

            Toast.makeText(this, "Quiz created with difficulty: " + quizDifficulty, Toast.LENGTH_SHORT).show();


        });



    }
}
