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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    TextInputLayout txtLayoutQuizName, txtLayoutQuizDifficulty, txtLayoutQuizCategory, txtLayoutQuizType;
    TextView txtDate;
    Button btnCreateQuiz, btnDatePicker;
    FloatingActionButton btnReturn;
    String quizName, quizDifficulty, quizCategory, quizDate, quizType;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://assignment-2-e308f-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference myref = database.getReference("Quiz");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_create_quiz);

        initViews();

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

        String[] itemsCategory = {"Any Category"};
        MaterialAutoCompleteTextView txtItemsCategory = (MaterialAutoCompleteTextView) txtLayoutQuizCategory.getEditText();
        if (txtItemsCategory instanceof MaterialAutoCompleteTextView) {
            txtItemsCategory.setSimpleItems(itemsCategory);
        }

        String[] itemsDifficulty = {"Any Difficulty", "Easy", "Medium", "Hard"};
        MaterialAutoCompleteTextView txtItems = (MaterialAutoCompleteTextView) txtLayoutQuizDifficulty.getEditText();
        if (txtItems instanceof MaterialAutoCompleteTextView) {
            txtItems.setSimpleItems(itemsDifficulty);
        }

        String[] itemsType = {"Any Type", "Multiple Choice", "True/False"};
        MaterialAutoCompleteTextView txtItemsType = (MaterialAutoCompleteTextView) txtLayoutQuizType.getEditText();
        if (txtItemsType instanceof MaterialAutoCompleteTextView) {
            txtItemsType.setSimpleItems(itemsType);
        }

        //category follow the same logic as difficulty

        btnCreateQuiz.setOnClickListener(v -> {
            quizName = getName();
            quizDifficulty = getDifficulty();
            quizCategory = getCategory();
            quizDate = getDate();
            quizType = getType();

            if (quizName.isEmpty()) {

                txtLayoutQuizName.setError("Name cannot be empty");

            } else if (quizDate.isEmpty()) {

                txtDate.setError("Date cannot be empty");

            } else if (quizCategory.isEmpty()) {

                txtLayoutQuizCategory.setError("Category cannot be empty");

            } else if (quizDifficulty.isEmpty()) {

                txtLayoutQuizDifficulty.setError("Difficulty cannot be empty");

            } else if (quizType.isEmpty()) {

                txtLayoutQuizType.setError("Type cannot be empty");

            } else {

               retrieveData();

            }
        });
    }

    private void retrieveData() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuizService service = retrofit.create(QuizService.class);
        Call<DataReturned> quizCall = service.getQuiz(10,0, quizDifficulty, quizType);

        quizCall.enqueue(new Callback<DataReturned>(){
            @Override
            public void onResponse(Call<DataReturned> call, Response<DataReturned> response) {

                List<DataReturned.ResultsBean> DataReturn = response.body().getResults();

                List<Quiz.QuestionBean> questions = new ArrayList<>();


             //   Quiz quiz = new Quiz(quizName, quizType, quizDifficulty, quizCategory, new Date(), new Date());

                for(DataReturned.ResultsBean q : DataReturn){
                    questions.add(new Quiz.QuestionBean(q.getQuestion(), q.getCorrect_answer(), q.getIncorrect_answers()));
                }


                Quiz quiz = new Quiz(quizName, quizType, quizDifficulty, quizCategory, new Date(), new Date(), questions);



             //   response.body().setName("My DataReturned");
            //    String name = response.body().getName();

           //     DataReturned.ResultsBean q = DataReturn.get(0);

                myref.push().setValue(quiz);

            }

            @Override
            public void onFailure(Call<DataReturned> call, Throwable t) {

            }
        });




    }

    private void initViews() {
        txtLayoutQuizName = findViewById(R.id.create_quiz_txt_name);
        txtLayoutQuizDifficulty = findViewById(R.id.create_quiz_txt_difficulty);
        txtLayoutQuizCategory = findViewById(R.id.create_quiz_txt_category);
        txtDate = findViewById(R.id.create_quiz_txt_date);
        txtLayoutQuizType = findViewById(R.id.create_quiz_txt_type);
        btnCreateQuiz = findViewById(R.id.create_quiz_btn_create);
        btnDatePicker = findViewById(R.id.create_quiz_btn_datepicker);
        btnReturn = findViewById(R.id.create_quiz_btn_return);

    }

    private String getName() {
        return txtLayoutQuizName.getEditText().getText().toString();
    }

    private String getDifficulty() {

        String difficulty = txtLayoutQuizDifficulty.getEditText().getText().toString();

        if(difficulty.equals("Any Difficulty")){
            return "";
        }
        else{
            return difficulty.toLowerCase();
        }
    }

    private String getCategory() {
        return txtLayoutQuizCategory.getEditText().getText().toString();
    }

    private String getDate() {
        return txtDate.getText().toString();
    }

    private String getType() {

        String type = txtLayoutQuizType.getEditText().getText().toString();

        if (type.equals("Multiple Choice")) {
            return "multiple";
        } else if (type.equals("True/False")) {
            return "boolean";
        } else {
            return "";
        }
    }

}
