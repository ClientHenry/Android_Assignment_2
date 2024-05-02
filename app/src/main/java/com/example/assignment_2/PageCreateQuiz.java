package com.example.assignment_2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PageCreateQuiz extends AppCompatActivity {

    TextInputLayout txtLayoutQuizName, txtLayoutQuizDifficulty, txtLayoutQuizCategory, txtLayoutQuizDate;
    Button btnCreateQuiz, btnCancel;
    FloatingActionButton btnReturn, btnDatePicker;
    String quizName, quizDifficulty, quizCategory, quizDate;
    int quizCategoryID;
    Date startDateSelected, endDateSelected;
    DatabaseReference myRef;
    OpenTDBService service;
    List<QuizCategory.TriviaCategoriesBean> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_create_quiz);

        initViews();
        retrieveQuizCategory();

        btnDatePicker.setOnClickListener(v -> {

            MaterialDatePicker dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select dates").build();

            dateRangePicker.addOnPositiveButtonClickListener(selection -> {

                Pair<Long, Long> dateSelected = (Pair<Long, Long>) dateRangePicker.getSelection();
                startDateSelected = new Date(dateSelected.first);
                endDateSelected = new Date(dateSelected.second);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM. d", Locale.ENGLISH);
                String startDateformatted = sdf.format(startDateSelected);
                String endDateformatted = sdf.format(endDateSelected);

                txtLayoutQuizDate.getEditText().setText(startDateformatted + " - " + endDateformatted);
            });

            dateRangePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        btnCreateQuiz.setOnClickListener(v -> {

            quizName = getName();
            quizDifficulty = getDifficulty();
            quizCategory = getCategory();
            quizCategoryID = getCategoryID(quizCategory);
            quizDate = getDate();

            if (validateInput(quizName, quizDate)) {
                validateQuizName(new OnReturnedListener() {
                    @Override
                    public void onReturned(boolean nameExists) {
                        if (!nameExists) {
                            retrieveAndCreateQuiz(quizCategoryID, quizDifficulty, quizName, startDateSelected, endDateSelected);
                        }
                    }
                }, quizName);
            }
        });

        btnReturn.setOnClickListener(v -> {
            finish();
        });

        btnCancel.setOnClickListener(v -> {
            finish();
        });
    }

    private void retrieveAndCreateQuiz(int categoryID, String difficulty, String name, Date startDate, Date endDate) {

        Call<RawOpenTDBDataReturned> dataCall = service.getDataFromOpenTDB(10, categoryID, difficulty, "boolean");
        dataCall.enqueue(new Callback<RawOpenTDBDataReturned>() {
            @Override
            public void onResponse(Call<RawOpenTDBDataReturned> call, Response<RawOpenTDBDataReturned> response) {

                if (response.body() == null) {

                    Toast.makeText(PageCreateQuiz.this, "Unable to retrieve data", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<RawOpenTDBDataReturned.ResultsBean> DataReturned = response.body().getResults();

                if (DataReturned.isEmpty()) {

                    Toast.makeText(PageCreateQuiz.this, "No Data Available, choose Another Option", Toast.LENGTH_SHORT).show();
                } else {

                    String categoryCreated = DataReturned.get(0).getCategory();
                    String difficultyCreated = DataReturned.get(0).getDifficulty();

                    List<Quiz.QuestionBean> questions = new ArrayList<>();
                    for (RawOpenTDBDataReturned.ResultsBean q : DataReturned) {

                        questions.add(new Quiz.QuestionBean(q.getQuestion(), q.getCorrect_answer(), q.getIncorrect_answers()));
                    }

                    Quiz quiz = new Quiz(name, difficultyCreated, categoryCreated, startDate, endDate, questions);
                    myRef.push().setValue(quiz);

                    Toast.makeText(PageCreateQuiz.this, "Quiz Created Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<RawOpenTDBDataReturned> call, Throwable t) {

                Toast.makeText(PageCreateQuiz.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {

        txtLayoutQuizName = findViewById(R.id.create_quiz_txt_name);
        txtLayoutQuizDifficulty = findViewById(R.id.create_quiz_txt_difficulty);
        txtLayoutQuizCategory = findViewById(R.id.create_quiz_txt_category);
        txtLayoutQuizDate = findViewById(R.id.create_quiz_txt_date);
        btnCreateQuiz = findViewById(R.id.create_quiz_btn_create);
        btnDatePicker = findViewById(R.id.create_quiz_btn_datepicker);
        btnReturn = findViewById(R.id.create_quiz_btn_return);
        btnCancel = findViewById(R.id.create_quiz_btn_cancel);

        myRef = FirebaseDatabase.getInstance("https://assignment2-fd51e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Quiz");

        service = new Retrofit.Builder().baseUrl("https://opentdb.com/").addConverterFactory(GsonConverterFactory.create()).build()
                .create(OpenTDBService.class);

        String[] itemsDifficulty = {"Any Difficulty", "Easy", "Medium", "Hard"};
        MaterialAutoCompleteTextView txtItems = (MaterialAutoCompleteTextView) txtLayoutQuizDifficulty.getEditText();
        if (txtItems instanceof MaterialAutoCompleteTextView) {
            txtItems.setSimpleItems(itemsDifficulty);
            txtItems.setText(itemsDifficulty[0], false);
        }
    }

    private boolean validateInput(String name, String date) {

        boolean isValid = false;

        if (name.isEmpty()) {
            txtLayoutQuizName.setError("Name cannot be empty");
        } else if (date.isEmpty()) {
            txtLayoutQuizDate.setError("Date cannot be empty");
        } else {
            isValid = true;
        }
        return isValid;
    }

    private void retrieveQuizCategory() {

        Call<QuizCategory> quizCategoryCall = service.getQuizCategories();
        quizCategoryCall.enqueue(new Callback<QuizCategory>() {
            @Override
            public void onResponse(Call<QuizCategory> call, Response<QuizCategory> response) {

                categories = response.body().getTrivia_categories();
                categories.add(0, new QuizCategory.TriviaCategoriesBean(0, "Any Category"));
                String[] itemsCategory = new String[categories.size()];

                for (int i = 0; i < categories.size(); i++) {
                    itemsCategory[i] = categories.get(i).getName();
                }

                MaterialAutoCompleteTextView txtItemsCategory = (MaterialAutoCompleteTextView) txtLayoutQuizCategory.getEditText();
                if (txtItemsCategory instanceof MaterialAutoCompleteTextView) {

                    txtItemsCategory.setSimpleItems(itemsCategory);
                    txtItemsCategory.setText(itemsCategory[0], false);
                }
            }

            @Override
            public void onFailure(Call<QuizCategory> call, Throwable t) {

                Toast.makeText(PageCreateQuiz.this, "Failed to retrieve category data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateQuizName(OnReturnedListener listener, String quizName) {

        Query myQuery = myRef.orderByChild("name").equalTo(quizName);
        myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                boolean nameExists = snapshot.exists();
                if (nameExists) {
                    txtLayoutQuizName.setError("Quiz name already exists");

                }
                listener.onReturned(nameExists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(PageCreateQuiz.this, "Error: unable to connect database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getName() {

        return txtLayoutQuizName.getEditText().getText().toString();
    }

    private String getDifficulty() {

        String difficulty = txtLayoutQuizDifficulty.getEditText().getText().toString();

        if (difficulty.equals("Any Difficulty")) {
            return "";
        } else {
            return difficulty.toLowerCase();
        }
    }

    private String getCategory() {

        return txtLayoutQuizCategory.getEditText().getText().toString();
    }

    private int getCategoryID(String category) {

        int id = 0;

        for (QuizCategory.TriviaCategoriesBean c : categories) {
            if (c.getName().equals(category)) {
                id = c.getId();
            }
        }

        return id;
    }

    private String getDate() {

        return txtLayoutQuizDate.getEditText().getText().toString();
    }

    public interface OnReturnedListener {
        void onReturned(boolean nameExists);
    }
}
