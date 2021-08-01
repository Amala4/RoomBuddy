package com.personalproject.roombuddy.general;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.personalproject.roombuddy.R;

import org.bson.Document;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class ProfileEdit extends AppCompatActivity {
    //Variables
    App app;

    Button cancel, save;

    EditText nameTextField, phoneNoTextField;

    MongoDatabase mongoDatabase;

    MongoClient mongoClient;

    MongoCollection<Document> profileDetailsMongoCollection;


    String appID = "roombuddy-umrym";

    String userID, name, phoneNo;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);



        // Initialize the MongodbRealm database
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(appID).build());
        user = app.currentUser();
        assert user != null;
        userID = Objects.requireNonNull(user).getId();
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
        profileDetailsMongoCollection = mongoDatabase.getCollection("Profile_Details");





        // Hooks xml fields to variables
        cancel = findViewById(R.id.profile_edit_cancel_button);
        nameTextField = findViewById(R.id.profile_edit_full_name);
        phoneNoTextField = findViewById(R.id.profile_edit_phone_number);
        save = findViewById(R.id.profile_edit_save_button);



         /*
          Finds the post detail from database
          */
        Document profileQueryFilter = new Document().append("User ID", userID);
        profileDetailsMongoCollection.findOne(profileQueryFilter).getAsync(task -> {
            if (task.isSuccess()) {

                Document results = task.get();
                Log.v("FindFunction", "Found a user");



                 /*
                Assigns the user data from
                database to the respective variables
                 */
                name = results.getString("Full Name");
                phoneNo = results.getString("Phone Number");




                /*
                Sets the variables to the
                respective xml textviews
                 */
                nameTextField.setText(name);
                phoneNoTextField.setText(phoneNo);



            }
            else {

                Log.v("Error", task.getError().toString());
                Toast.makeText(getApplicationContext(), "Invalid user data", Toast.LENGTH_LONG).show();

            }
        });
    }


    public void save(View view) {


        String updatedName = nameTextField.getText().toString().trim();
        String updatedPhoneNo = phoneNoTextField.getText().toString().trim();





        /* Checks that no
        editable field is empty*/
        if (TextUtils.isEmpty(updatedName) | TextUtils.isEmpty(updatedPhoneNo))
        {
            Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_LONG).show();
        }

        else{

            Document update_Filter = new Document().append("User ID", userID);
            RealmResultTask<MongoCursor<Document>> findTask = profileDetailsMongoCollection.find(update_Filter).iterator();

            findTask.getAsync(task -> {
                if (task.isSuccess()) {
                    MongoCursor<Document> results = task.get();

                    if (results.hasNext()) {
                        Log.v("FindFunction", "Found Something");
                        Document result = results.next();


                        profileDetailsMongoCollection.updateOne(update_Filter,
                                result.append("Full Name", updatedName))
                                .getAsync(result1 -> {
                                    if (result1.isSuccess()) {
                                        Log.v("UpdateFunction", "About Me Updated");


                                    } else {
                                        Log.v("UpdateFunction", "Error" + result1.getError().toString());
                                    }

                                });


                        profileDetailsMongoCollection.updateOne(update_Filter,
                                result.append("Phone Number", updatedPhoneNo))
                                .getAsync(result1 -> {
                                    if (result1.isSuccess()) {
                                        Log.v("UpdateFunction", "Kind of Person Updated");
                                        Intent homeIntent = new Intent(getApplicationContext(), Homescreen.class);
                                        Toast.makeText(this, "Data updated", Toast.LENGTH_SHORT).show();
                                        startActivity(homeIntent);

                                    } else {
                                        Log.v("UpdateFunction", "Error" + result1.getError().toString());

                                    }

                                });


                    } else {
                        Log.v("FindFunction", "Found Nothing");
                    }

                } else {
                    Log.v("Error", task.getError().toString());

                }
            });

        }

    }

    public void cancel(View view){
        super.onBackPressed();
    }
}