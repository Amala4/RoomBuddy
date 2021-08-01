package com.personalproject.roombuddy.general;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.personalproject.roombuddy.helperClasses.Space;
import com.personalproject.roombuddy.models.Room;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class History extends AppCompatActivity {

    //Variables
    App app;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    MongoCollection<Document> profileDetailsMongoCollection, roomDetailsMongoCollection;
    RecyclerView historyRecyclerViewRooms;
    RoomAdapter roomAdapter;
    String appID = "roombuddy-umrym";

    String  user_dateOfRegistration, userID;

    TextView postCount, dateOfRegistrationTextView;

    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);



        // Initialize the MongodbRealm database
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(appID).build());
        user = app.currentUser();
        assert user != null;
        userID = Objects.requireNonNull(user).getId();
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
        profileDetailsMongoCollection = mongoDatabase.getCollection("Profile_Details");
        roomDetailsMongoCollection = mongoDatabase.getCollection("Room_Details");




        // Hooks xml fields to variables
        postCount = findViewById(R.id.history_post_count);
        dateOfRegistrationTextView = findViewById(R.id.history_registration_date);
        historyRecyclerViewRooms = findViewById(R.id.history_recyclerViewRooms);








        /*
        Finds the user profile detail from database
         */
        Document queryFilter = new Document().append("User ID",userID);
        profileDetailsMongoCollection.findOne(queryFilter).getAsync(task -> {
            if (task.isSuccess()) {

                Document results = task.get();
                Log.v("FindFunction", "Found a user");



                /*
                Assigns the user data from
                database to the respective variables
                 */
                user_dateOfRegistration = results.getString("Date of registration");



                /*
                Sets the variables to the
                respective xml textviews
                 */

                dateOfRegistrationTextView.setText(user_dateOfRegistration);


            }
            else {

                Log.v("Error", task.getError().toString());

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





        historyRecyclerViewRooms.setLayoutManager(gridLayoutManager);  //set layout manager as gridLayoutManager



        //To give loading item full single row
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (roomAdapter.getItemViewType(position)) {
                    case RoomAdapter.HISTORY_ROOM_ITEM:
                        return 2;
                    case RoomAdapter.LOADING_ITEM:
                        return 2; //number of columns of the grid
                    default:
                        return -1;
                }
            }
        });




        feedData();
        historyRecyclerViewRooms.addItemDecoration(new Space(1,
                50, true, 0));   //add space between cards

        historyRecyclerViewRooms.setAdapter(roomAdapter);   //Finally set the adapter
        ViewCompat.setNestedScrollingEnabled(historyRecyclerViewRooms, false);

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

        RealmResultTask<MongoCursor<Document>> findTask = roomDetailsMongoCollection.
                find(new Document().append("User ID",user.getId())).
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
                            userID[0] = user.getId();
                            currentPage[0] = "History";



                            Log.v("Task Error","data assigned well");

                            image[0] = MediaManager.get().url().transformation
                                    (new Transformation().aspectRatio("1.0")
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

                        }
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //hide loading
                            roomAdapter.hideLoading();
                            //add products to recyclerview
                            roomAdapter.addRooms(rooms);

                            postCount.setText("You have "+roomAdapter.getItemCount()+" active posts");


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