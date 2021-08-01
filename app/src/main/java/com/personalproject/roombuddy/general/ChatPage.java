package com.personalproject.roombuddy.general;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.adapters.ChatAdapter;
import com.personalproject.roombuddy.adapters.RoomAdapter;
import com.personalproject.roombuddy.helperClasses.Space;
import com.personalproject.roombuddy.models.Messages;
import com.personalproject.roombuddy.models.Room;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import static io.realm.Realm.getApplicationContext;

public class ChatPage extends AppCompatActivity {

    //Variables
    App app;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    MongoCollection<Document> profileDetailsMongoCollection, chatDetailsMongoCollection;
    RecyclerView messageRecyclerView;
    RelativeLayout buddyProfile;
    ChatAdapter chatAdapter;
    String appID = "roombuddy-umrym";

    String  posterUserID, ownUserID, conversationID, alternativeConversationID, PosterName, ownName;


    ImageView sendButton, buddyPicture;


    EditText chatPageInputMessage;

    TextView buddyName;


    User user;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);



        // Initialize the MongodbRealm database
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(appID).build());
        user = app.currentUser();
        assert user != null;
        ownUserID = Objects.requireNonNull(user).getId();
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
        chatDetailsMongoCollection = mongoDatabase.getCollection("Chat_Collection");
        profileDetailsMongoCollection = mongoDatabase.getCollection("Profile_Details");







        // Hooks xml fields to variables
        sendButton = findViewById(R.id.chatPage_sendButton);
        chatPageInputMessage = findViewById(R.id.chatPage_inputMessage);
        messageRecyclerView = findViewById(R.id.chatPage_recyclerView);
        buddyName = findViewById(R.id.chat_page_buddy_name);
        buddyPicture = findViewById(R.id.chat_page_buddy_picture);
        buddyProfile = findViewById(R.id.header);






        //Get all values passed from previous screens using Intent
        posterUserID = getIntent().getStringExtra("User ID");



        //show the profile pic data
        Picasso.get().load(MediaManager.get().url().transformation
                (new Transformation().width(450).height(300).crop("limit"))
                .generate(posterUserID)).networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(buddyPicture);


        //Assign names of poster
        profileDetailsMongoCollection.findOne(new Document()
                .append("User ID", posterUserID))
                .getAsync(profiletask -> {
                    if (profiletask.isSuccess()) {

                        Document profileResults = profiletask.get();
                        PosterName = profileResults.getString("Full Name");
                        buddyName.setText(PosterName);

                    } else {
                        Log.v("Task Error","No User found");
                    }
                });



        //Assign name of current user
        profileDetailsMongoCollection.findOne(new Document()
                .append("User ID", ownUserID))
                .getAsync(profiletask -> {
                    if (profiletask.isSuccess()) {

                        Document profileResults = profiletask.get();
                        ownName = profileResults.getString("Full Name");

                    } else {
                        Log.v("Task Error","No User found");
                    }
                });



        //Create a conversation ID by combining both the poster ID and own ID
        conversationID = posterUserID+ownUserID;
        alternativeConversationID = ownUserID+posterUserID;



        chatAdapter = new ChatAdapter(this);  //Create new ChatAdapter



        /*
        Creates new
        GridLayoutManager
         */
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,
                1,//span count no of items in single row
                GridLayoutManager.VERTICAL,//Orientation
                false);//reverse scrolling of recyclerview





        messageRecyclerView.setLayoutManager(gridLayoutManager);  //set layout manager as gridLayoutManager




        displayMessages();          //Finds the chat history detail from database
        messageRecyclerView.addItemDecoration(new Space(1,
                50, true, 0));   //add space between cards




        messageRecyclerView.setAdapter(chatAdapter);   //Finally set the adapter
        //ViewCompat.setNestedScrollingEnabled(historyRecyclerViewRooms, false);


        messageRecyclerView.scrollToPosition(chatAdapter.getItemCount()-1);








        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOutMessage();
            }
        });


        buddyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewPosterProfileIntent = new Intent(getApplicationContext(), PosterProfile.class);
                viewPosterProfileIntent.putExtra("User ID", posterUserID);
                startActivity(viewPosterProfileIntent);
            }
        });


    }




    private void sendOutMessage() {

        String chatMessage = chatPageInputMessage.getText().toString().trim();

        if(!TextUtils.isEmpty(chatMessage)){


            Calendar rightNow = Calendar.getInstance();

            long now = rightNow.getTimeInMillis();
            long nowInSeconds = now/1000;
            String timeNowInString = String.valueOf(nowInSeconds);



            Document conversationFilter = new Document().append("Conversation ID", conversationID);

            chatDetailsMongoCollection.findOne(conversationFilter).getAsync(task -> {
                if (task.isSuccess()) {

                    Document results = task.get();
                    Log.v("FindFunction", "Conversation already exists");

                    if (results != null){
                        String messageCount = results.getString("Message Count");
                        int intOfMessageCount = Integer.parseInt(messageCount);
                        int intOfUpdatedMessageCount = 1+intOfMessageCount;

                        String updatedMessageCount = String.valueOf(intOfUpdatedMessageCount);

                        chatDetailsMongoCollection.updateOne(conversationFilter,
                                results.append("Message("+updatedMessageCount+")", Arrays.asList(chatMessage, timeNowInString, ownUserID)))
                                .getAsync(result1 -> {
                                    if (result1.isSuccess()) {
                                        Log.v("UpdateFunction", "Message Added");

                                        Messages newMessages = new Messages(ownUserID,
                                                ownUserID,
                                                chatMessage,
                                                timeNowInString);


                                        //update chat messages of the recyclerview
                                        chatAdapter.addMessage(newMessages);

                                        messageRecyclerView.scrollToPosition(chatAdapter.getItemCount()-1);

                                        chatPageInputMessage.getText().clear();


                                    } else {
                                        Log.v("UpdateFunction", "Error" + result1.getError().toString());
                                    }

                                });


                        chatDetailsMongoCollection.updateOne(conversationFilter,
                                results.append("Message Count", updatedMessageCount))
                                .getAsync(result2 -> {
                                    if (result2.isSuccess()) {
                                        Log.v("UpdateFunction", "My Message Count Updated");


                                    } else {
                                        Log.v("UpdateFunction", "Error" + result2.getError().toString());

                                    }

                                });
                    }
                    else {

                        Log.v("Error", "Conversation doesn't exist");

                        chatDetailsMongoCollection.insertOne(new Document().append("Conversation ID",Arrays.asList(conversationID, alternativeConversationID))
                                .append("Participants", Arrays.asList(ownUserID, posterUserID))
                                .append("First Participant", Arrays.asList(ownUserID, ownName))
                                .append("Second Participant", Arrays.asList(posterUserID, PosterName))
                                .append("Message Count","1")
                                .append("Message(1)",Arrays.asList(chatMessage, timeNowInString,ownUserID)))

                                .getAsync(result -> {
                                    if(result.isSuccess()) {
                                        Log.v("Data", "Data Inserted Successfully");


                                        Messages newMessages = new Messages(ownUserID,
                                                ownUserID,
                                                chatMessage,
                                                timeNowInString);


                                        //update chat messages of the recyclerview
                                        chatAdapter.addMessage(newMessages);

                                        messageRecyclerView.scrollToPosition(chatAdapter.getItemCount()-1);

                                        chatPageInputMessage.getText().clear();
                                    }
                                    else
                                    {
                                        Toast.makeText(getApplicationContext(), "Chat not saved", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }


                }
                else {

                    Toast.makeText(this, "Find did not work", Toast.LENGTH_SHORT).show();


                }
            });

        }

        else {
            Toast.makeText(this, "Please type a message", Toast.LENGTH_SHORT).show();

        }

    }


    private void displayMessages(){

        final List<Messages> messageList = new ArrayList<>();

        Document conversation = new Document().append("Conversation ID", conversationID);

        chatDetailsMongoCollection.findOne(conversation).getAsync(task -> {
            if (task.isSuccess()) {

                Document results = task.get();
                Log.v("FindFunction", "Conversation exists");

                if (results != null){
                    String messageCount = results.getString("Message Count");

                    int intOfMessageCount = Integer.parseInt(messageCount);

                    for(int i=1;i<=intOfMessageCount;i++)
                    {
                        ArrayList listOfMessageAndTime = (ArrayList) results.get("Message("+i+")");

                        String message_dis = listOfMessageAndTime.get(0).toString();
                        String messageTime_dis = listOfMessageAndTime.get(1).toString();
                        String messageAuthor_dis = listOfMessageAndTime.get(2).toString();

                        Messages messages = new Messages(messageAuthor_dis,
                                ownUserID,
                                message_dis,
                                messageTime_dis);

                        messageList.add(messages);
                    }

                    //add chat messages to the recyclerview
                    chatAdapter.addMessages(messageList);
                } else{
                    Toast.makeText(this, "No messages yet", Toast.LENGTH_SHORT).show();

                }





            } else {
                Toast.makeText(this, "Find did not work", Toast.LENGTH_SHORT).show();

            }

        });

    }





    }