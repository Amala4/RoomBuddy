package com.personalproject.roombuddy.general;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.database.SessionManager;

import org.bson.Document;

import java.util.Calendar;
import java.util.Objects;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class SignUpPage extends AppCompatActivity {


    //Variables
    App app;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    MongoCollection<Document> mongoCollection;
    RadioButton selectedGender;
    RadioGroup radioGroup;
    RelativeLayout progressBar;
    Spinner stateSpinner, campusSpinner;

    String userID, PhoneNo, Fullname, Email, firstPasswordInput,
            secondPasswordInput, genderShortForm, weekDayInWords,
            monthInWords, chosenState, chosenCampus, gender, regDate;

    String appID = "roombuddy-umrym";
    String[] state = {"Imo", "Enugu", "Anambra"};
    String[] campus = {"IMSU", "Unizik, Nnewi", "Unizik, Awka"};
    TextInputLayout phoneNo, fullname, email, password, confirmPassword;
    User user;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);



        // Initialize the MongodbRealm database and build a new app instance
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(appID).build());





        //Hooks xml fields to variables
        phoneNo = findViewById(R.id.signup_phone_no);
        fullname = findViewById(R.id.signup_fullname);
        email = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);
        confirmPassword = findViewById(R.id.signup_confirm_password);
        progressBar = findViewById(R.id.sign_up_progress_bar);
        stateSpinner = findViewById(R.id.signup_stateSpinner);
        campusSpinner = findViewById(R.id.signup_campusSpinner);
        radioGroup = findViewById(R.id.signup_radioGroup);




        progressBar.setVisibility(View.GONE);    //Makes the circling loading bar invisible



        //Creates the ArrayAdapter instance having the states list for the state drop down
        ArrayAdapter<String> stateSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, state);
        stateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(stateSpinnerAdapter);     //Sets the ArrayAdapter data on the Spinner
        stateSpinner.setOnItemSelectedListener(new SignUpPage.StateSpinnerClass());



        //Creates the ArrayAdapter instance having the campus list for the campus drop down
        ArrayAdapter<String> campusSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, campus);
        campusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campusSpinner.setAdapter(campusSpinnerAdapter);     //Sets the ArrayAdapter data on the Spinner
        campusSpinner.setOnItemSelectedListener(new SignUpPage.CampusSpinnerClass());


    }



    public void signUpUser(View view) {

        // Ensures that the password format is valid and that confirm password matches with the password
        if (!validatePassword() || !validateConfirmPassword()) {
            return;
        }





        progressBar.setVisibility(View.VISIBLE);     //circling loading bar is now made visible



        //Gets all the data from the input fields and assign them to the respective variables
        Fullname = Objects.requireNonNull(fullname.getEditText()).getText().toString().trim();
        PhoneNo = Objects.requireNonNull(phoneNo.getEditText()).getText().toString().trim();
        Email = Objects.requireNonNull(email.getEditText()).getText().toString().trim();
        secondPasswordInput = Objects.requireNonNull(confirmPassword.getEditText()).getText().toString().trim();
        selectedGender = findViewById(radioGroup.getCheckedRadioButtonId());
        gender = selectedGender.getText().toString();



        //Makes the gender take its short form
        if (gender.equals("Male")) {

            genderShortForm = "(M)";
        }
        if (gender.equals("Female")) {

            genderShortForm = "(F)";
        }



        //Signs up user on the mongodbrealm system
        app.getEmailPassword().registerUserAsync(Email, secondPasswordInput, result -> {
            if (result.isSuccess()) {



                /*
                Log in the user right away using
                the provided credentials
                */
                Credentials credentials = Credentials.emailPassword(Email, secondPasswordInput);

                app.loginAsync(credentials, results -> {

                    if (results.isSuccess())
                    {


                        //Creates a login session
                        SessionManager sessionManager = new SessionManager(SignUpPage.this, SessionManager.SESSION_USERSESSION);
                        sessionManager.createLoginSession(Email, secondPasswordInput);




                        /*
                        Get the current date of
                        this account creation and
                        set it to the desired format
                        */
                        Calendar c = Calendar.getInstance();
                        int day = c.get(Calendar.DAY_OF_MONTH);
                        int weekDay = c.get(Calendar.DAY_OF_WEEK);
                        int month = c.get(Calendar.MONTH) + 1;
                        int year = c.get(Calendar.YEAR);

                        //Sets the weekday to the desired format
                        switch (weekDay) {

                            case 1:
                                weekDayInWords = "Sun";
                                break;

                            case 2:
                                weekDayInWords = "Mon";
                                break;

                            case 3:
                                weekDayInWords = "Tue";
                                break;

                            case 4:
                                weekDayInWords = "Wed";
                                break;

                            case 5:
                                weekDayInWords = "Thur";
                                break;

                            case 6:
                                weekDayInWords = "Fri";
                                break;

                            case 7:
                                weekDayInWords = "Sat";
                                break;
                        }



                        //Sets the month to the desired format
                        switch (month) {

                            case 1:
                                monthInWords = "Jan";
                                break;

                            case 2:
                                monthInWords = "Feb";
                                break;

                            case 3:
                                monthInWords = "Mar";
                                break;

                            case 4:
                                monthInWords = "Apr";
                                break;

                            case 5:
                                monthInWords = "May";
                                break;

                            case 6:
                                monthInWords = "Jun";
                                break;

                            case 7:
                                monthInWords = "Jul";
                                break;

                            case 8:
                                monthInWords = "Aug";
                                break;

                            case 9:
                                monthInWords = "Sept";
                                break;

                            case 10:
                                monthInWords = "Oct";
                                break;

                            case 11:
                                monthInWords = "Nov";
                                break;

                            case 12:
                                monthInWords = "Dec";
                                break;
                        }



                        //Sets the year to the desired format
                        int yearShortFormat = year % 100;


                        //Final format of the registration date string..."Member since Sat, May 1, '21"
                        regDate = "Member since " + weekDayInWords + "," + " " + (monthInWords) + " " + day + "," + " " + "'" + yearShortFormat;


                        //Gets User ID
                        user = app.currentUser();
                        assert user != null;
                        userID = user.getId();
                        mongoClient = user.getMongoClient("mongodb-atlas");
                        mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
                        mongoCollection = mongoDatabase.getCollection("Profile_Details");


                        /*
                        Saves the user signup details
                        in the profile collection
                        */
                        mongoCollection.insertOne(new Document()
                                .append("User ID", userID).append("Full Name", Fullname)
                                .append("Phone Number", PhoneNo).append("Email", Email)
                                .append("State", chosenState).append("Campus", chosenCampus)
                                .append("Gender", genderShortForm).append("Last Number", "0")
                                .append("Date of registration", regDate)).getAsync(task -> {


                            if (task.isSuccess()) {

                                Toast.makeText(getApplicationContext(), "Sign up Successful", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);


                                //Starts the next activity which in this case is the home page
                                Intent intent = new Intent(getApplicationContext(), Homescreen.class);
                                startActivity(intent);
                                finish();

                            }

                            else
                                {
                                Toast.makeText(this, "Data not saved", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        });

                    } else {

                        Log.v("Login", results.getError().toString());
                        Toast.makeText(getApplicationContext(), "Failed to login" + results.getError().toString(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });


            }

            else {

                progressBar.setVisibility(View.INVISIBLE);       //circling loading bar is made invisible since sign up was not successful

                Toast.makeText(getApplicationContext(), "Failed to sign up" + result.getError().toString(), Toast.LENGTH_LONG).show();
            }
        });
    }





    public void callLoginPage(View view) {

        Intent intent = new Intent(getApplicationContext(), LoginPage.class);
        startActivity(intent);
    }




    private boolean validateConfirmPassword() {

        /*
        Checks if confirm password
        matches password
         */
        firstPasswordInput = Objects.requireNonNull(password.getEditText()).getText().toString().trim();
        secondPasswordInput = Objects.requireNonNull(confirmPassword.getEditText()).getText().toString().trim();


        if (secondPasswordInput.isEmpty()) {
            confirmPassword.setError("Please enter your password again");
            confirmPassword.requestFocus();
            return false;
        }

        else if (!(firstPasswordInput.equals(secondPasswordInput)))
        {
            confirmPassword.setError("Passwords do not match, Try again");
            confirmPassword.requestFocus();
            return false;
        }
        else
            {
            return true;
        }
    }




    private boolean validatePassword() {

        /*
        Ensures that password has
        the valid format of not
        less than 6 characters
        */
        firstPasswordInput = Objects.requireNonNull(password.getEditText()).getText().toString().trim();

        if (firstPasswordInput.isEmpty()) {
            password.setError("Please enter your password");
            password.requestFocus();
            return false;
        }

        else if (firstPasswordInput.length() < 6)
        {
            password.setError("Password cannot be less than six characters");
            password.requestFocus();
            return false;
        }
        else {
            return true;
        }

    }



    class StateSpinnerClass implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            chosenState = state[position];
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO - Custom Code
        }
    }



    class CampusSpinnerClass implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            chosenCampus = campus[position];
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO - Custom Code

        }
    }

}