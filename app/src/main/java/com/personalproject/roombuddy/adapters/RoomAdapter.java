package com.personalproject.roombuddy.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.general.EditRoom;
import com.personalproject.roombuddy.general.History;
import com.personalproject.roombuddy.general.Homescreen;
import com.personalproject.roombuddy.general.RoomDetails;
import com.personalproject.roombuddy.models.Room;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

import static io.realm.Realm.getApplicationContext;
//RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>

public class RoomAdapter extends RecyclerView.Adapter{
    List<Room> mRooms;
    Context mContext;
    public static final int LOADING_ITEM = 0;
    public static final int ROOM_ITEM = 1;
    public static final int HISTORY_ROOM_ITEM = 2;
    int LoadingItemPos;
    public boolean loading = false;
    int viewtype;


    public RoomAdapter(Context mContext) {
        mRooms = new ArrayList<>();
        this.mContext = mContext;
    }


    //method to add rooms as soon as they fetched
    public void addRooms(List<Room> rooms) {
        int lastPos = mRooms.size();
        this.mRooms.addAll(rooms);
        notifyItemRangeInserted(lastPos, mRooms.size());
    }


    @Override
    public int getItemViewType(int position) {
        Room currentRoom = mRooms.get(position);
        if (currentRoom.isLoading()) {
            return LOADING_ITEM;
        }
        else if(!currentRoom.isLoading() && currentRoom.getCurrentPage().equals("History"))
        {

            return HISTORY_ROOM_ITEM;
        }
        else {
            return ROOM_ITEM;
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        viewtype = viewType;
        //Check which view has to be populated
        if (viewType == LOADING_ITEM) {
            View row = inflater.inflate(R.layout.custom_row_loading, parent, false);
            return new LoadingViewHolder(row);
        } else if (viewType == ROOM_ITEM) {
            View row = inflater.inflate(R.layout.custom_row_room, parent, false);
            return new RoomViewHolder(row);
        }
        else if (viewType == HISTORY_ROOM_ITEM) {
            View row = inflater.inflate(R.layout.custom_row_room_history, parent, false);
            return new RoomViewHolder(row);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //get current product
        final Room currentRoom = mRooms.get(position);
         if (holder instanceof RoomAdapter.RoomViewHolder) {
        RoomAdapter.RoomViewHolder roomViewHolder = (RoomAdapter.RoomViewHolder) holder;

        //bind products information with view
        Picasso.get().load(currentRoom.getImageUrl()).networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(roomViewHolder.imageViewRoomThumb);
        roomViewHolder.textViewCampus.setText(currentRoom.getRoomCampus());
        roomViewHolder.textViewRoommateGender.setText(currentRoom.getRoommateGender());
        roomViewHolder.textViewState.setText(currentRoom.getRoomState());
        String currentPage = currentRoom.getCurrentPage();


                 roomViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {

                         Intent intent;

                         switch (currentPage) {
                             case "homePage":
                                 intent = new Intent(getApplicationContext(), RoomDetails.class);
                                 intent.putExtra("User ID", currentRoom.getUserID());
                                 intent.putExtra("Post number", currentRoom.getPostNumber());
                                 intent.putExtra("Previous Page", "homePage");
                                 mContext.startActivity(intent);

                                 //Animation, executes the animation
                                 ((Activity) view.getContext()).overridePendingTransition(R.anim.left_side_anim, R.anim.hold_anim);


                                 break;

                             case "posterProfile":
                                 intent = new Intent(getApplicationContext(), RoomDetails.class);
                                 intent.putExtra("User ID", currentRoom.getUserID());
                                 intent.putExtra("Post number", currentRoom.getPostNumber());
                                 intent.putExtra("Previous Page", "posterProfile");
                                 mContext.startActivity(intent);

                                 //Animation, executes the animation
                                 ((Activity) view.getContext()).overridePendingTransition(R.anim.left_side_anim, R.anim.hold_anim);

                                 break;


                             case "History":
                                 intent = new Intent(getApplicationContext(), RoomDetails.class);
                                 intent.putExtra("User ID", currentRoom.getUserID());
                                 intent.putExtra("Post number", currentRoom.getPostNumber());
                                 intent.putExtra("Previous Page", "History");
                                 mContext.startActivity(intent);

                                 //Animation, executes the animation
                                 ((Activity) view.getContext()).overridePendingTransition(R.anim.left_side_anim, R.anim.hold_anim);

                                 break;


                             default:
                                 intent = new Intent(getApplicationContext(), RoomDetails.class);
                                 intent.putExtra("User ID", currentRoom.getUserID());
                                 intent.putExtra("Post number", currentRoom.getPostNumber());
                                 mContext.startActivity(intent);

                                 //Animation, executes the animation
                                 ((Activity) view.getContext()).overridePendingTransition(R.anim.left_side_anim, R.anim.hold_anim);

                                 break;

                         }



                     }
                 });



        if (viewtype==2)
        {
            roomViewHolder.editPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                   Intent editPostIntent = new Intent(getApplicationContext(), EditRoom.class);
                    editPostIntent.putExtra("Post number", currentRoom.getPostNumber());
                    mContext.startActivity(editPostIntent);

                    //Animation, executes the animation
                    ((Activity) view.getContext()).overridePendingTransition(R.anim.left_side_anim, R.anim.hold_anim);

                }
            });

            roomViewHolder.deletePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(mContext);

                    // Set the message show for the Alert time
                    builder.setMessage("Do you want to delete this post ?");
                    builder.setTitle("Delete Post");
                    builder.setPositiveButton(
                                    "Yes",
                                    new DialogInterface
                                            .OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which)
                                        {
                                            App app;
                                            MongoDatabase mongoDatabase;
                                            MongoClient mongoClient;
                                            MongoCollection<Document>  roomMongoCollection;
                                            String appID = "roombuddy-umrym";
                                            User user;
                                            assert getApplicationContext() != null;
                                            Realm.init(getApplicationContext());
                                            app = new App(new AppConfiguration.Builder(appID).build());
                                            user = app.currentUser();
                                            assert user != null;
                                            mongoClient = user.getMongoClient("mongodb-atlas");
                                            mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
                                            roomMongoCollection = mongoDatabase.getCollection("Room_Details");

                                            roomMongoCollection.deleteOne(new Document().append("Post number",currentRoom.getPostNumber()))
                                                    .getAsync(result2 -> {
                                                                if (result2.isSuccess()) {
                                                                    Log.v("UpdateFunction", "Room Address and Description Updated");
                                                                    Toast.makeText(getApplicationContext(), "Post deleted", Toast.LENGTH_LONG).show();
                                                                    Intent historyIntent = new Intent(getApplicationContext(), History.class);
                                                                    mContext.startActivity(historyIntent);


                                                                } else {
                                                                    Log.v("UpdateFunction", "Error" + result2.getError().toString());
                                                                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();

                                                                }

                                                            }
                                                    );

                                            ((Activity)view.getContext()).finish();

                                        }
                            });

                    builder.setNegativeButton(
                                    "No",
                                    new DialogInterface
                                            .OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which)
                                        {

                                            // If user click no
                                            // then dialog box is canceled.
                                            dialog.cancel();
                                        }
                                    });


                    //Creating dialog box
                    AlertDialog dialog = builder.create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();


                }
            });
        }

    }


    }

    @Override
    public int getItemCount() {
        return mRooms.size();
    }

    //Holds view of rooms with information
    private static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewRoomThumb;
        TextView textViewCampus, textViewRoommateGender, textViewState;
        Button deletePost, editPost;


        public RoomViewHolder(View itemView) {
            super(itemView);
            imageViewRoomThumb = itemView.findViewById(R.id.imageViewRoomThumb);
            textViewCampus = itemView.findViewById(R.id.textViewCampus);
            textViewRoommateGender = itemView.findViewById(R.id.textViewRoommateGender);
            textViewState = itemView.findViewById(R.id.textViewState);
            deletePost = itemView.findViewById(R.id.delete_btn);
            editPost = itemView.findViewById(R.id.edit_btn);

        }
    }

    //holds view of loading item...
    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    //method to show loading
    public void showLoading() {
        Room room = new Room();
        room.setLoading(true);
        mRooms.add(room);
        LoadingItemPos = mRooms.size();
        notifyItemInserted(mRooms.size());
        loading = true;
    }

    //method to hide loading
    public void hideLoading() {
        if (LoadingItemPos <= mRooms.size()) {
            mRooms.remove(LoadingItemPos - 1);
            notifyItemRemoved(LoadingItemPos);
            loading = false;
        }

    }

}
