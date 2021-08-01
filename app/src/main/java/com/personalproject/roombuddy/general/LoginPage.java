package com.personalproject.roombuddy.general;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.database.SessionManager;

import java.util.HashMap;
import java.util.Objects;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;

public class LoginPage extends AppCompatActivity {

    //Variables
    App app;
    CheckBox rememberMe;
    RelativeLayout progressBar;
    String appID = "roombuddy-umrym";
    String Email, Password;
    TextInputLayout email, password;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);


        // Initialize the MongodbRealm database and build a new app instance
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(appID).build());




        //Hooks for the xml UI elements
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.login_progress_bar);
        rememberMe = findViewById(R.id.remember_me);




        progressBar.setVisibility(View.GONE);       //makes the progress bar invisible




        /*
         Checks weather email and password
         are already saved in Shared Preferences or not
        */

        SessionManager sessionManager = new SessionManager(LoginPage.this, SessionManager.SESSION_REMEMBERME);


        if(sessionManager.checkRememberMe())
        {

            HashMap<String, String> rememberMeDetails = sessionManager.getRememberMeDetailFromSession();




            /*
            Sets the saved login details
            on their fields
            */
            Objects.requireNonNull(email.getEditText()).setText(rememberMeDetails.get(SessionManager.KEY_SESSIONEMAIL));
            Objects.requireNonNull(password.getEditText()).setText(rememberMeDetails.get(SessionManager.KEY_SESSIONPASSWORD));
        }
    }






    public void loginUser(View view){

        progressBar.setVisibility(View.VISIBLE);   //loading bar starts to show


        Email = Objects.requireNonNull(email.getEditText()).getText().toString().trim();
        Password = Objects.requireNonNull(password.getEditText()).getText().toString().trim();



        //Updates session manager
        SessionManager loginSessionManager = new SessionManager(LoginPage.this, SessionManager.SESSION_USERSESSION);
        loginSessionManager.createLoginSession(Email, Password);



        Credentials credentials = Credentials.emailPassword(Email, Password);



        app.loginAsync(credentials,result -> {

            if(result.isSuccess())
            {


                if(rememberMe.isChecked())     //Saves the login details if the remember me box is checked
                {

                    SessionManager sessionManager = new SessionManager(LoginPage.this, SessionManager.SESSION_REMEMBERME);
                    sessionManager.createRememberMeSession(Email, Password);
                }

                progressBar.setVisibility(View.GONE);         //loading bar stops

                Intent intent = new Intent(getApplicationContext(), Homescreen.class);
                intent.putExtra("Email", Email);
                startActivity(intent);
                finish();
            }

            else
            {
                progressBar.setVisibility(View.GONE);        //loading bar stops
                Toast.makeText(getApplicationContext(),"Failed to login"+result.getError().toString(),Toast.LENGTH_LONG).show();
            }
        });

    }





    public void callSignUpPage(View view){

        Intent intent = new Intent(getApplicationContext(), SignUpPage.class);
        startActivity(intent);
    }

}