package com.personalproject.roombuddy.general;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.personalproject.roombuddy.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.bson.Document;

import java.io.IOException;
import java.util.Map;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class EditRoom extends AppCompatActivity {

    //Variables
    Boolean IMAGE_IS_SELECTED = false;

    Button selectPicture, updatePost;


    EditText roomAddressAndDesc, kindOfPerson, aboutMe, roomRent,
            roommateRent;

    ImageView roomPic;


    RelativeLayout progressbar;


    String  _campus, _gender, _roomDescription,
            _kindOfPerson, _aboutMe, _roomRent, _roommateRent,
             _PostNumber, _userID, filePath;
    TextView posterCampus, gender, imageProgress;


    private static final int PERMISSION_CODE = 1;
    private static final int PICK_IMAGE = 1;


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
        setContentView(R.layout.activity_edit_room);



        // Hooks
        aboutMe = findViewById(R.id.edit_room_aboutMeMessage);
        gender = findViewById(R.id.edit_room_genderLookingFor);
        imageProgress = findViewById(R.id.edit_room_imageProgress);
        kindOfPerson = findViewById(R.id.edit_room_kindOfPersonMessage);
        posterCampus = findViewById(R.id.edit_room_posterCampus);
        progressbar = findViewById(R.id.edit_room_progress_bar);
        roomPic = findViewById(R.id.edit_room_roomPicture);
        roomRent = findViewById(R.id.edit_room_roomRentValue);
        roommateRent = findViewById(R.id.edit_room_roommateRentValue);
        roomAddressAndDesc = findViewById(R.id.edit_room_roomAddressAndDescriptionMessage);
        selectPicture = findViewById(R.id.edit_room_postRoomSelectPicture);
        updatePost = findViewById(R.id.edit_room_update_button);



        progressbar.setVisibility(View.VISIBLE);   //makes the progress bar visible




        //Get all values passed from previous screens using Intent
        _PostNumber = getIntent().getStringExtra("Post number");





        // Initialize the MongodbRealm database
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(appID).build());
        user = app.currentUser();
        assert user != null;
        _userID = user.getId();
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
        profileMongoCollection = mongoDatabase.getCollection("Profile_Details");
        roomMongoCollection = mongoDatabase.getCollection("Room_Details");







        Picasso.get().load(MediaManager.get().url().transformation(new Transformation()
                .quality(60)).generate(_PostNumber)).networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(roomPic, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {


                /*
                 Finds the post detail from database
                */
                Document roomQueryFilter = new Document().append("Post number",_PostNumber);
                roomMongoCollection.findOne(roomQueryFilter).getAsync(task -> {
                    if (task.isSuccess()) {

                        Document results = task.get();
                        Log.v("FindFunction", "Found a user");



                /*
                Assigns the post data from
                database to the respective variables
                 */
                        _campus = results.getString("Campus");
                        _gender = results.getString("Gender");
                        _roomDescription = results.getString("Room Address and Description");
                        _kindOfPerson = results.getString("Kind of Person");
                        _aboutMe = results.getString("About Me");
                        _roomRent = results.getString("Room Rent");
                        _roommateRent = results.getString("Roommate Rent");




                        //Bind data to their respective fields
                        posterCampus.setText(_campus);
                        gender.setText("Looking for "+_gender);
                        roomAddressAndDesc.setText(_roomDescription);
                        kindOfPerson.setText(_kindOfPerson);
                        aboutMe.setText(_aboutMe);
                        roomRent.setText(_roomRent);
                        roommateRent.setText(_roommateRent);


                        progressbar.setVisibility(View.GONE);   //makes the progress bar invisible

                    }
                    else {

                        Log.v("Error", task.getError().toString());
                        Toast.makeText(getApplicationContext(), "Invalid user", Toast.LENGTH_LONG).show();

                    }
                });

            }

            @Override
            public void onError(Exception e) {

                progressbar.setVisibility(View.GONE);   //makes the progress bar invisible
                Toast.makeText(getApplicationContext(), "Error in loading page", Toast.LENGTH_LONG).show();

            }
        });



        //when clicked, mImageAdd request the permission to access the gallery
        selectPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //request permission to access external storage
                requestPermission();
            }
        });
    }





    public void updatePost(View view){

        progressbar.setVisibility(View.VISIBLE);   //makes the progress bar visible


        String updatedAboutMe = aboutMe.getText().toString().trim();
        String updatedKindOfPerson = kindOfPerson.getText().toString().trim();
        String updatedRoomDescription = roomAddressAndDesc.getText().toString().trim();
        String updatedRoomRent = roomRent.getText().toString().trim();
        String updatedRoommateRent = roommateRent.getText().toString().trim();





        /* Checks that no
        editable field is empty*/
            if (TextUtils.isEmpty(updatedAboutMe) | TextUtils.isEmpty(updatedKindOfPerson) |
                    TextUtils.isEmpty(updatedRoomDescription) | TextUtils.isEmpty(updatedRoomRent) |
                    TextUtils.isEmpty(updatedRoommateRent))
            {
                Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_LONG).show();
            }

            else{

                Document update_Filter = new Document().append("Post number",_PostNumber);
                RealmResultTask<MongoCursor<Document>> findTask = roomMongoCollection.find(update_Filter).iterator();

                findTask.getAsync(task -> {
                    if (task.isSuccess()) {
                        MongoCursor<Document> results = task.get();

                        if (results.hasNext()) {
                            Log.v("FindFunction", "Found Something");
                            Document result = results.next();


                            roomMongoCollection.updateOne(update_Filter,
                                    result.append("About Me", updatedAboutMe))
                                    .getAsync(result1 -> {
                                if (result1.isSuccess()) {
                                    Log.v("UpdateFunction", "About Me Updated");


                                } else {
                                    Log.v("UpdateFunction", "Error" + result1.getError().toString());
                                }

                            });


                            roomMongoCollection.updateOne(update_Filter,
                                    result.append("Kind of Person", updatedKindOfPerson))
                                    .getAsync(result1 -> {
                                        if (result1.isSuccess()) {
                                            Log.v("UpdateFunction", "Kind of Person Updated");


                                        } else {
                                            Log.v("UpdateFunction", "Error" + result1.getError().toString());
                                        }

                                    });



                            roomMongoCollection.updateOne(update_Filter,
                                    result.append("Room Address and Description", updatedRoomDescription))
                                    .getAsync(result1 -> {
                                        if (result1.isSuccess()) {
                                            Log.v("UpdateFunction", "Room Address and Description Updated");


                                        } else {
                                            Log.v("UpdateFunction", "Error" + result1.getError().toString());
                                        }

                                    });



                            roomMongoCollection.updateOne(update_Filter,
                                    result.append("Room Rent", updatedRoomRent))
                                    .getAsync(result1 -> {
                                        if (result1.isSuccess()) {
                                            Log.v("UpdateFunction", "Room Rent Updated");

                                        } else {
                                            Log.v("UpdateFunction", "Error" + result1.getError().toString());
                                        }

                                    });



                            roomMongoCollection.updateOne(update_Filter,
                                    result.append("Roommate Rent", updatedRoommateRent))
                                    .getAsync(result1 -> {
                                        if (result1.isSuccess()) {
                                            Log.v("UpdateFunction", "Roommate Rent Updated");


                                        } else {
                                            Log.v("UpdateFunction", "Error" + result1.getError().toString());
                                        }

                                    });


                            if(IMAGE_IS_SELECTED) {
                                //Uploads the picture to database
                                uploadToCloudinary(filePath);
                            }
                            else {
                                imageProgress.setText("Post updated successfully");
                                progressbar.setVisibility(View.GONE);   //makes the progress bar invisible
                            }



                        } else {
                            Log.v("FindFunction", "Found Nothing");
                            progressbar.setVisibility(View.GONE);
                        }

                    } else {
                        Log.v("Error", task.getError().toString());
                        progressbar.setVisibility(View.GONE);

                    }
                });

            }

    }










    private void requestPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            accessTheGallery();
        }

        else
        {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                accessTheGallery();
            }
            else
            {
                Toast.makeText(this, "Access Permission denied", Toast.LENGTH_SHORT).show();
                accessTheGallery();
            }
        }
    }


    private void accessTheGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data == null) {
            return;
        }

        // get the image's file location
        filePath = getRealPathFromUri(data.getData(), this);

        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK){
            try {
                //set picked image to the imageRender
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                roomPic.setImageBitmap(bitmap);
                IMAGE_IS_SELECTED = true;
            } catch (IOException e) {
                Log.d("P", "nothing picked");
                e.printStackTrace();
                IMAGE_IS_SELECTED = false;
            }
        }
        else
        {
            Log.d("P", "nothing picked");

        }
//
    }






    private String getRealPathFromUri(Uri imageUri, Activity activity) {

        Cursor cursor = activity.getContentResolver().query(imageUri, null, null, null, null);

        if(cursor==null) {
            return imageUri.getPath();
        }
        else
        {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }




    private void uploadToCloudinary(String filePath) {


        Log.d("A", "sign up uploadToCloudinary- ");
        MediaManager.get().upload(filePath).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {

                imageProgress.setText("starting to Upload..");

            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                Double progress = (double) 100*(bytes/totalBytes);
                imageProgress.setText("Uploading... "+progress+"%");
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                imageProgress.setText("Post updated successfully \n wait for post history page to refresh...");
                new Handler().postDelayed(new Runnable() {
                                              @Override
                                              public void run() {

                                                  Intent intent = new Intent(getApplicationContext(), History.class);
                                                  startActivity(intent);
                                              }
                                          },
                        //Pass the delay time here
                        2500);
                progressbar.setVisibility(View.GONE);

            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                imageProgress.setText("error "+ error.getDescription());
                progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                imageProgress.setText("Reshedule "+error.getDescription());
            }
        })
                .option("public_id", _PostNumber)
                .option("invalidate", true)
                .dispatch();

    }

}
