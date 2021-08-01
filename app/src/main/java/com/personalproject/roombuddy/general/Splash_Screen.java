package com.personalproject.roombuddy.general;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.database.SessionManager;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;

public class Splash_Screen extends AppCompatActivity {

    //Variables
    App app;
    Map<String, String> config = new HashMap<>();
    SharedPreferences signupScreen;
    String appID = "roombuddy-umrym";




    /*
    Configures cloudinary, which is the image
    cloud infrastructure for the app
     */
    private void configCloudinary() {
        config.put("cloud_name", "amala4");
        config.put("api_key", "254489593719218");
        config.put("api_secret", "ujgl8zIC2Gw2zuTOJTjPlugZC-c");
        MediaManager.init(this, config);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);


        // Initialize the MongodbRealm database
        Realm.init(this);


        //Build a realm instance of the app
        app = new App(new AppConfiguration.Builder(appID).build());


        //Call the image cloud configuration
        configCloudinary();






        /*
        Calls the new activity
        after SPLASH_TIMER duration
        1000 = 1s
         */
        int SPLASH_TIMER = 2000;

        new Handler().postDelayed(new Runnable() {
                                      @Override
                                      public void run() {



                                          /*
                                          Checks if the users is opening the app
                                          for the first time after installation
                                          */
                                          signupScreen = getSharedPreferences("signupScreen", MODE_PRIVATE);
                                          boolean isFirstTime = signupScreen.getBoolean("firstTime", true);


                                          if (isFirstTime) {

                                              SharedPreferences.Editor editor = signupScreen.edit();



                                              editor.putBoolean("firstTime", false);    //sets firstTime to false so that next
                                              // opening of the app would no longer be the firstTime


                                              editor.apply();
                                              Intent intent = new Intent(getApplicationContext(), WelcomePage.class);   //Welcome page opens for first time users
                                              startActivity(intent);
                                              finish();

                                          }

                                          else

                                              {

                                                  /*
                                                  If not firstTime, checks if user logged out the last time
                                                  if logged out, then welcome page shows for user to login,
                                                  else user is logged in automatically
                                                  */

                                              SessionManager loginsessionManager = new SessionManager(Splash_Screen.this, SessionManager.SESSION_USERSESSION);
                                              boolean loginStatus = loginsessionManager.checkLogin();


                                              if (loginStatus) {

                                              /*
                                              Get user login details from session and
                                              log user in automatically
                                               */
                                                  SessionManager sessionManager = new SessionManager(Splash_Screen.this, SessionManager.SESSION_USERSESSION);
                                                  HashMap<String, String> loginDetails = sessionManager.getUsersDetailFromSession();



                                                  String Email = loginDetails.get(SessionManager.KEY_EMAIL);
                                                  String Password = loginDetails.get(SessionManager.KEY_PASSWORD);


                                                  Credentials credentials = Credentials.emailPassword(Email, Password);


                                                  app.loginAsync(credentials, result -> {

                                                      if (result.isSuccess()) {

                                                          Intent intent = new Intent(getApplicationContext(), Homescreen.class);
                                                          startActivity(intent);
                                                          finish();
                                                      }

                                                      else

                                                          {

                                                              Intent intent = new Intent(getApplicationContext(), Homescreen.class);
                                                              startActivity(intent);
                                                              finish();

                                                      }
                                                  });
                                              }

                                              else

                                                  {
                                                  Intent intent = new Intent(getApplicationContext(), WelcomePage.class);
                                                  startActivity(intent);
                                                  finish();
                                              }


                                          }

                                      }
                                  },
                //Pass the delay time here
                SPLASH_TIMER);
    }


}
