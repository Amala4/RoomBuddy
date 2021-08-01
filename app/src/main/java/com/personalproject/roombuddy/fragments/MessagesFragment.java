package com.personalproject.roombuddy.fragments;

import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.google.android.material.appbar.AppBarLayout;
import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.adapters.ChatListAdapter;
import com.personalproject.roombuddy.general.Homescreen;
import com.personalproject.roombuddy.helperClasses.Space;
import com.personalproject.roombuddy.models.ChatListModel;

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


public class MessagesFragment extends Fragment {

    //Variables
    App app;
    DrawerLayout drawerLayout;
    ImageView menuIcon;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    MongoCollection<Document> profileDetailsMongoCollection, chatDetailsMongoCollection;
    RecyclerView chatlistRecyclerView;
    ChatListAdapter chatListAdapter;
    String appID = "roombuddy-umrym";

    String ownUserID, ownName;


    User user;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_messages,container, false);





        // Initialize the MongodbRealm database
        Realm.init(Objects.requireNonNull(getContext()));
        app = new App(new AppConfiguration.Builder(appID).build());
        user = app.currentUser();
        assert user != null;
        ownUserID = Objects.requireNonNull(user).getId();
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
        chatDetailsMongoCollection = mongoDatabase.getCollection("Chat_Collection");
        profileDetailsMongoCollection = mongoDatabase.getCollection("Profile_Details");




        // Hooks xml fields to variables
        chatlistRecyclerView = view.findViewById(R.id.chatList_recyclerView);
        drawerLayout = ((Homescreen) Objects.requireNonNull(getActivity())).drawerLayout;
        menuIcon = view.findViewById(R.id.menu_icon);



        chatListAdapter = new ChatListAdapter(getActivity());  //Create new ChatListAdapter



         /*
        Creates new
        GridLayoutManager
         */
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),
                1,//span count no of items in single row
                GridLayoutManager.VERTICAL,//Orientation
                false);//reverse scrolling of recyclerview





        chatlistRecyclerView.setLayoutManager(gridLayoutManager);  //set layout manager as gridLayoutManager



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




        displayMessages();          //Finds all user's chats from database

        chatlistRecyclerView.addItemDecoration(new Space(1,
                50, false, 0));   //add space between cards

        chatlistRecyclerView.setAdapter(chatListAdapter);   //Finally set the adapter



        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else drawerLayout.openDrawer(GravityCompat.START);
            }
        });



        // Inflate the layout for this fragment
        return view;
    }


    private void displayMessages() {

        final List<ChatListModel> chatList = new ArrayList<>();


        RealmResultTask<MongoCursor<Document>> findTask = chatDetailsMongoCollection.
                find(new Document("Participants", ownUserID)).
                sort(new Document().append("Message Count",1)).iterator();

        findTask.getAsync(task-> {
            if (task.isSuccess()) {
                Log.v("Task Error","Task is successful");

                MongoCursor<Document> results = task.get();
                if(results.hasNext()){
                    Log.v("Task Error","Result has next");

                    while (results.hasNext()) {
                        Document currentDoc = results.next();
                        if (currentDoc != null) {


                            Log.v("Task Error","CurrentDoc is not null"+currentDoc.getString("Message"));

                            ArrayList participants = (ArrayList) currentDoc.get("Participants");


                            String firstParticipant = participants.get(0).toString();
                            String secondParticipant = participants.get(1).toString();

                            Log.v("Task Error",firstParticipant);
                            Log.v("Task Error",secondParticipant);

                            if (firstParticipant.equals(ownUserID))
                            {

                                ArrayList friend = (ArrayList) currentDoc.get("Second Participant");

                                String friendName = friend.get(1).toString();


                                String messageCount = currentDoc.getString("Message Count");
                                ArrayList messageArray = (ArrayList) currentDoc.get("Message("+messageCount+")");
                                String message = messageArray.get(0).toString();



                                String imageUrl = MediaManager.get().url().transformation
                                        (new Transformation().width(450).height(300).crop("limit"))
                                        .generate(secondParticipant);

                                Log.v("Task Error","data assigned well");




                                ChatListModel chat = new ChatListModel(friendName,
                                        message,
                                        imageUrl,
                                        secondParticipant);
                                chatList.add(chat);

                            }
                            else
                            {

                                ArrayList friend = (ArrayList) currentDoc.get("First Participant");

                                String friendName = friend.get(1).toString();



                                String messageCount = currentDoc.getString("Message Count");
                                ArrayList messageArray = (ArrayList) currentDoc.get("Message("+messageCount+")");
                                String message = messageArray.get(0).toString();


                                String imageUrl = MediaManager.get().url().transformation
                                        (new Transformation().width(450).height(300).crop("limit"))
                                        .generate(firstParticipant);

                                Log.v("Task Error","data assigned well");




                                ChatListModel chat = new ChatListModel(friendName,
                                        message,
                                        imageUrl,
                                        firstParticipant);
                                chatList.add(chat);
                            }



                        }
                        else{
                            Log.v("Task Error","current doc is null");
                        }



                    }

                    //add chat messages to the recyclerview
                    chatListAdapter.addChatList(chatList);
                }


                else{
                    Toast.makeText(getContext(), "No more found", Toast.LENGTH_SHORT).show();
                }


            }
            else {
                Log.v("Task Error","Error:"+task.getError().toString());
                Toast.makeText(getContext(), "Contents do not load", Toast.LENGTH_SHORT).show();
            }



        });
    }
}