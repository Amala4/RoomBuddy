package com.personalproject.roombuddy.general;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.personalproject.roombuddy.R;

import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class ContactAdmin extends AppCompatActivity {

    //Variables
    App app;
    Button sendButton;
    TextInputLayout userMessageLayout;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    MongoCollection<Document> userMessageCollection;
    RelativeLayout progressBar;
    String appID = "roombuddy-umrym";
    String userID, userMessage;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_admin);



        // Initialize MongodbRealm and set up database
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(appID).build());
        user = app.currentUser();
        assert user != null;
        userID = user.getId();
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
        userMessageCollection = mongoDatabase.getCollection("User_messages");




        // Hooks xml fields to variables
        sendButton = findViewById(R.id.send_to_admin);
        userMessageLayout = findViewById(R.id.user_message);
        progressBar = findViewById(R.id.ca_progress_bar);


        progressBar.setVisibility(View.GONE);   //makes the progress bar invisible


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //request permission to access external storage
                send();
            }
        });
    }


    public void send() {

        progressBar.setVisibility(View.VISIBLE);
        userMessage = userMessageLayout.getEditText().getText().toString().trim();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDateTime = dateFormat.format(new Date()); // Find today's date

        if(!TextUtils.isEmpty(userMessage)){

            userMessageCollection.insertOne(new Document().append("User ID",userID).append("Message Time",currentDateTime)
                    .append("Message",userMessage))
                    .getAsync(result -> {
                        if(result.isSuccess()) {

                            progressBar.setVisibility(View.GONE);

                            userMessageLayout.getEditText().getText().clear();

                            Toast.makeText(getApplicationContext(), "Message sent succesfully", Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(getApplicationContext(), "Message not sent, please re-launch the app", Toast.LENGTH_SHORT).show();

                        }
                    });
        }

        else{
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Please type a message", Toast.LENGTH_SHORT).show();

        }


    }
}