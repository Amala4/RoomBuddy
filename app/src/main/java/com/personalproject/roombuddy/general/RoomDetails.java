package com.personalproject.roombuddy.general;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.fragments.ProfileFragment;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.bson.Document;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

import static io.realm.Realm.getApplicationContext;

public class RoomDetails extends AppCompatActivity {

    //Variables
    ImageView roomPic;

    CircleImageView profilePic;

    RelativeLayout progressbar;

    String _rdfullName, _rdcampus, _rdgender, _rdroomDescription,profileImageURL,
            _rdkindOfPerson, _rdaboutMe, _rdroomRent, _rdroommateRent,
            _rdphoneNo, _rdPostNumber, _rduserID, previousPage;


    TextView posterName, posterCampus, gender, roomAddressAndDesc,
            kindOfPerson, aboutMe, roomRent, roommateRent, phoneNo;


    //Database Variables
    App app;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    MongoCollection<Document> profileMongoCollection, roomMongoCollection;
    String appID = "roombuddy-umrym";
    User user;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);



        // Hooks- links variables to xml views
        aboutMe = findViewById(R.id.rd_aboutMeMessage);
        gender = findViewById(R.id.rd_genderLookingFor);
        kindOfPerson = findViewById(R.id.rd_kindOfPersonMessage);
        phoneNo = findViewById(R.id.rd_poster_phone_no);
        posterName = findViewById(R.id.rd_posterName);
        posterCampus = findViewById(R.id.rd_posterCampus);
        progressbar = findViewById(R.id.rd_progress_bar);
        profilePic = findViewById(R.id.rd_poster_image);
        roomAddressAndDesc = findViewById(R.id.rd_roomAddressAndDescriptionMessage);
        roomRent = findViewById(R.id.rd_roomRentValue);
        roommateRent = findViewById(R.id.rd_roommateRentValue);
        roomPic = findViewById(R.id.rd_roomPicture);




        progressbar.setVisibility(View.VISIBLE);   //makes the progress bar visible




        //Get all values passed from previous screens using Intent
         _rdPostNumber = getIntent().getStringExtra("Post number");
         _rduserID = getIntent().getStringExtra("User ID");
         previousPage = getIntent().getStringExtra("Previous Page");




        // Initialize the MongodbRealm database
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(appID).build());
        user = app.currentUser();
        assert user != null;
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
        profileMongoCollection = mongoDatabase.getCollection("Profile_Details");
        roomMongoCollection = mongoDatabase.getCollection("Room_Details");







         /*
        Finds the user profile
        image from cloud
         */
        profileImageURL = MediaManager.get().url().transformation
                (new Transformation().width(450).height(300).crop("limit"))
                .generate(_rduserID);
        Picasso.get().load(profileImageURL).networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(profilePic);



        Picasso.get().load(MediaManager.get().url().transformation(
                new Transformation().quality(60)).generate(_rdPostNumber))
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(roomPic, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {

                /*
                 Finds the user profile detail from database
                */
                Document profileQueryFilter = new Document().append("User ID",_rduserID);
                profileMongoCollection.findOne(profileQueryFilter).getAsync(task -> {
                    if (task.isSuccess()) {

                        Document results = task.get();
                        Log.v("FindFunction", "Found a user");



                /*
                Assigns the user data from
                database to the respective variables
                 */
                        _rdfullName = results.getString("Full Name");
                        _rdphoneNo = results.getString("Phone Number");



                        //Bind data to their respective fields
                        posterName.setText(_rdfullName);
                        phoneNo.setText(_rdphoneNo);

                    }
                    else {

                        Log.v("Error", task.getError().toString());
                        Toast.makeText(getApplicationContext(), "Invalid user", Toast.LENGTH_LONG).show();
                        progressbar.setVisibility(View.GONE);   //makes the progress bar invisible

                    }
                });


                /*
                 Finds the post detail from database
                */
                Document roomQueryFilter = new Document().append("Post number",_rdPostNumber);
                roomMongoCollection.findOne(roomQueryFilter).getAsync(task -> {
                    if (task.isSuccess()) {

                        Document results = task.get();
                        Log.v("FindFunction", "Found a user");



                /*
                Assigns the post data from
                database to the respective variables
                 */
                        _rdcampus = results.getString("Campus");
                        _rdgender = results.getString("Gender");
                        _rdroomDescription = results.getString("Room Address and Description");
                        _rdkindOfPerson = results.getString("Kind of Person");
                        _rdaboutMe = results.getString("About Me");
                        _rdroomRent = results.getString("Room Rent");
                        _rdroommateRent = results.getString("Roommate Rent");




                        //Bind data to their respective fields
                        posterCampus.setText(_rdcampus);
                        gender.setText("Looking for "+_rdgender);
                        roomAddressAndDesc.setText(_rdroomDescription);
                        kindOfPerson.setText(_rdkindOfPerson);
                        aboutMe.setText(_rdaboutMe);
                        roomRent.setText(_rdroomRent);
                        roommateRent.setText(_rdroommateRent);

                        progressbar.setVisibility(View.GONE);   //makes the progress bar invisible

                    }
                    else {

                        Log.v("Error", task.getError().toString());
                        Toast.makeText(getApplicationContext(), "Invalid user", Toast.LENGTH_LONG).show();
                        progressbar.setVisibility(View.GONE);   //makes the progress bar invisible

                    }
                });




            }

            @Override
            public void onError(Exception e) {

                progressbar.setVisibility(View.GONE);   //makes the progress bar invisible
                Toast.makeText(getApplicationContext(), "Error in loading page", Toast.LENGTH_LONG).show();



            }
        });


        roomPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent;

                intent = new Intent(getApplicationContext(), FullPic.class);
                intent.putExtra("Public ID", _rdPostNumber);
                startActivity(intent);

                //Animation, executes the animation
                ((Activity) view.getContext()).overridePendingTransition(R.anim.left_side_anim, R.anim.hold_anim);


            }
        });

    }



    public void callPoster(View view){

        switch (previousPage) {
            case "homePage":

                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+_rdphoneNo));
                startActivity(callIntent);
                break;


            case "posterProfile":
                Intent calIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+_rdphoneNo));
                startActivity(calIntent);
                break;


            case "History":
                Toast.makeText(getApplicationContext(), "Cannot call yourself", Toast.LENGTH_LONG).show();
                break;

        }


    }


    public void viewPosterProfile(View view){



        switch (previousPage) {
            case "homePage":

                Intent viewPosterProfileIntent = new Intent(getApplicationContext(), PosterProfile.class);
                viewPosterProfileIntent.putExtra("User ID", _rduserID);
                startActivity(viewPosterProfileIntent);
                break;


            case "posterProfile":
                super.onBackPressed();
                break;


            case "History":
                Intent ownProfileIntent = new Intent(getApplicationContext(), OwnProfile.class);
                startActivity(ownProfileIntent);
                break;

            default:
                Toast.makeText(getApplicationContext(), previousPage, Toast.LENGTH_LONG).show();

                break;
        }

    }


    public void chatWithPoster(View view){
        Intent chatPosterIntent = new Intent(getApplicationContext(), ChatPage.class);
        chatPosterIntent.putExtra("User ID", _rduserID);
        startActivity(chatPosterIntent);
    }

}
