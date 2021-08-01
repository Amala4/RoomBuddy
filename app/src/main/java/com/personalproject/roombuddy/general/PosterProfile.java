package com.personalproject.roombuddy.general;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.adapters.RoomAdapter;
import com.personalproject.roombuddy.helperClasses.EndlessScrollListener;
import com.personalproject.roombuddy.helperClasses.Space;
import com.personalproject.roombuddy.models.Room;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
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

public class PosterProfile extends AppCompatActivity {

    //Variables
    App app;
    CircleImageView profilePic;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    MongoCollection<Document> profileDetailsMongoCollection, roomDetailsMongoCollection;
    RecyclerView posterProfileRecyclerViewRooms;
    RelativeLayout progressbar;
    RoomAdapter roomAdapter;
    String appID = "roombuddy-umrym";

    String user_gender, user_fullName, user_email, user_phoneNo,profileImageURL,
            user_state, user_campus, user_dateOfRegistration, userID, posterUserID ;

    TextView genderTextView, fullNameTextView, emailTextView, phoneNoTextView,
            stateTextView, campusTextView, dateOfRegistrationTextView, verification_status;

    User user;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster_profile);



        // Initialize the MongodbRealm database
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(appID).build());
        user = app.currentUser();
        assert user != null;
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
        profileDetailsMongoCollection = mongoDatabase.getCollection("Profile_Details");
        roomDetailsMongoCollection = mongoDatabase.getCollection("Room_Details");




        // Hooks xml fields to variables
        profilePic = findViewById(R.id.poster_profile_image);
        verification_status = findViewById(R.id.poster_profile_verified_text);
        genderTextView = findViewById(R.id.poster_profile_gender);
        fullNameTextView = findViewById(R.id.poster_profile_full_name);
        emailTextView = findViewById(R.id.poster_profile_email_address);
        phoneNoTextView = findViewById(R.id.poster_profile_phone_number);
        stateTextView = findViewById(R.id.poster_profile_state);
        campusTextView = findViewById(R.id.poster_profile_campus);
        dateOfRegistrationTextView = findViewById(R.id.poster_profile_registration_date);
        posterProfileRecyclerViewRooms = findViewById(R.id.poster_profile_recyclerViewRooms);
        progressbar = findViewById(R.id.poster_profile_progress_bar);



        progressbar.setVisibility(View.VISIBLE);   //makes the progress bar visible




        //Get all values passed from previous screens using Intent
        posterUserID = getIntent().getStringExtra("User ID");





         /*
        Finds the user profile
        image from cloud
         */
        profileImageURL = MediaManager.get().url().transformation
                (new Transformation().aspectRatio("1.0").width(450).height(300).crop("lfill"))
                .generate(posterUserID);
        Picasso.get().load(profileImageURL).networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(profilePic);





        /*
        Finds the user profile detail from database
         */
        Document queryFilter = new Document().append("User ID",posterUserID);
        profileDetailsMongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {

                Document results = task.get();
                Log.v("FindFunction", "Found a user");



                /*
                Assigns the user data from
                database to the respective variables
                 */
                user_gender = results.getString("Gender");
                user_fullName = results.getString("Full Name");
                user_email = results.getString("Email");
                user_phoneNo = results.getString("Phone Number");
                user_state = results.getString("State");
                user_campus = results.getString("Campus");
                user_dateOfRegistration = results.getString("Date of registration");



                /*
                Sets the variables to the
                respective xml textviews
                 */
                genderTextView.setText(user_gender);
                fullNameTextView.setText(user_fullName);
                emailTextView.setText(user_email);
                phoneNoTextView.setText(user_phoneNo);
                stateTextView.setText(user_state);
                campusTextView.setText(user_campus);
                dateOfRegistrationTextView.setText(user_dateOfRegistration);


                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressbar.setVisibility(View.GONE);   //makes the progress bar invisible
                    }
                }, 500);

            }
            else {

                Log.v("Error", task.getError().toString());
                Toast.makeText(getApplicationContext(), "Invalid user", Toast.LENGTH_LONG).show();
                progressbar.setVisibility(View.GONE);   //makes the progress bar invisible


            }
        });





        roomAdapter = new RoomAdapter(this);  //Create new RoomAdapter



        /*
        Creates new
        GridLayoutManager
         */
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,
                2,//span count no of items in single row
                GridLayoutManager.VERTICAL,//Orientation
                false);//reverse scrolling of recyclerview





        posterProfileRecyclerViewRooms.setLayoutManager(gridLayoutManager);  //set layout manager as gridLayoutManager



        //To give loading item full single row
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (roomAdapter.getItemViewType(position)) {
                    case RoomAdapter.ROOM_ITEM:
                        return 2;
                    case RoomAdapter.LOADING_ITEM:
                        return 2; //number of columns of the grid
                    default:
                        return -1;
                }
            }
        });




        feedData();
        posterProfileRecyclerViewRooms.addItemDecoration(new Space(1, 50, true, 0));   //add space between cards
        posterProfileRecyclerViewRooms.setAdapter(roomAdapter);   //Finally set the adapter
        ViewCompat.setNestedScrollingEnabled(posterProfileRecyclerViewRooms, false);



        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent;

                intent = new Intent(getApplicationContext(), FullPic.class);
                intent.putExtra("Public ID", posterUserID);
                startActivity(intent);

                //Animation, executes the animation
                ((Activity) view.getContext()).overridePendingTransition(R.anim.left_side_anim, R.anim.hold_anim);


            }
        });

    }



    private void feedData() {

        //show loading in recyclerview
        roomAdapter.showLoading();
        final List<Room> rooms = new ArrayList<>();

        final String[] state = new String[1];
        final String[] campus = new String[1];
        final String[] gender = new String[1];
        final String[] image = new String[1];
        final String[] postNumber = new String[1];
        final String[] userID = new String[1];
        final String[] currentPage = new String[1];
        state[0] = "";
        campus[0] = "";
        gender[0] = "";
        image[0] = "";
        postNumber[0] = "";
        userID[0] = "";
        currentPage[0] = "";

        RealmResultTask<MongoCursor<Document>> findTask = roomDetailsMongoCollection.find
                (new Document().append("User ID",posterUserID)).
                sort(new Document().append("Time",-1)).iterator();

        findTask.getAsync(task-> {
            if (task.isSuccess()) {
                Log.v("Task Error","Task is successful");

                MongoCursor<Document> results = task.get();
                if(results.hasNext()){
                    Log.v("Task Error","Result has next");

                    while (results.hasNext()) {
                        Document currentDoc = results.next();
                        if (currentDoc != null) {

                            Log.v("Task Error","CurrentDoc is not null");



                            postNumber[0] = currentDoc.getString("Post number");
                            state[0] =  currentDoc.getString("State");
                            campus[0] = currentDoc.getString("Campus");
                            gender[0] = currentDoc.getString("Gender");
                            userID[0] = posterUserID;
                            currentPage[0] = "posterProfile";



                            Log.v("Task Error","data assigned well");

                            // image[0] = MediaManager.get().url().transformation(new Transformation().height(300).crop("limit")).generate(currentDoc.getString("Unique number"));
                            image[0] = MediaManager.get().url()
                                    .transformation(new Transformation().aspectRatio("1.0")
                                            .width(450).height(300).crop("lfill"))
                                    .generate(currentDoc.getString("Post number"));


                            Room room = new Room(image[0],
                                    state[0],
                                    campus[0],
                                    gender[0],
                                    postNumber[0],
                                    userID[0],
                                    currentPage[0]);
                            rooms.add(room);



                            Log.v("Task Error","imagedata added");

                        } else {
                            Toast.makeText(this, "CurrentDoc is null", Toast.LENGTH_SHORT).show();
                            progressbar.setVisibility(View.GONE);   //makes the progress bar invisible


                        }
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //hide loading
                            roomAdapter.hideLoading();
                            //add products to recyclerview
                            roomAdapter.addRooms(rooms);

                        }
                    }, 20);
                }


                else{
                    Toast.makeText(this, "No more found", Toast.LENGTH_SHORT).show();
                }



            }
            else {
                Log.v("Task Error","Error:"+task.getError().toString());
                Toast.makeText(this, "Contents do not load", Toast.LENGTH_SHORT).show();
            }



        });
    }

}