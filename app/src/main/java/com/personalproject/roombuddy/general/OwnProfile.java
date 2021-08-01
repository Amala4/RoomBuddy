package com.personalproject.roombuddy.general;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

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
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class OwnProfile extends AppCompatActivity {

    //Variables
    App app;
    CircleImageView profilePic;
    LinearLayout changePicture;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    MongoCollection<Document> profileDetailsMongoCollection;
    private static final int PERMISSION_CODE = 1;
    private static final int PICK_IMAGE = 1;
    RecyclerView profileRecyclerViewRooms;
    RelativeLayout progressbar;
    String appID = "roombuddy-umrym";

    String user_gender, user_fullName, user_email, user_phoneNo,profileImageURL,
            user_state, user_campus, user_dateOfRegistration, userID, filePath;

    TextView genderTextView, fullNameTextView, emailTextView, phoneNoTextView,imageProgress,
            stateTextView, campusTextView, dateOfRegistrationTextView, verification_status;

    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_profile);



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
        campusTextView = findViewById(R.id.own_profile_campus);
        changePicture = findViewById(R.id.own_profile_change_picture);
        dateOfRegistrationTextView = findViewById(R.id.own_profile_registration_date);
        emailTextView = findViewById(R.id.own_profile_email_address);
        fullNameTextView = findViewById(R.id.own_profile_full_name);
        genderTextView = findViewById(R.id.own_profile_gender);
        phoneNoTextView = findViewById(R.id.own_profile_phone_number);
        profilePic = findViewById(R.id.own_profile_image);
        progressbar = findViewById(R.id.own_profile_progress_bar);
        stateTextView = findViewById(R.id.own_profile_state);
        verification_status = findViewById(R.id.own_profile_verified_text);
        imageProgress = findViewById(R.id.own_profile_imageProgress);





        progressbar.setVisibility(View.VISIBLE);   //makes the progress bar visible





        /*
        Finds the user profile
        image from cloud
         */
        profileImageURL = MediaManager.get().url().transformation
                (new Transformation().width(450).height(300).crop("limit"))
                .generate(userID);
        Picasso.get().load(profileImageURL).networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).into(profilePic);







        /*
        Finds the user profile detail from database
         */
        Document queryFilter = new Document().append("User ID", userID);
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
                progressbar.setVisibility(View.GONE);   //makes the progress bar invisible

            }
        });






        //when clicked, mImageAdd request the permission to access the gallery
        changePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //request permission to access external storage
                requestPermission();
            }
        });
    }


    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            accessTheGallery();
        }
        else {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE);
        }

    }




    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                accessTheGallery();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Access Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }





    private void accessTheGallery() {

        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data == null) {
            return;
        }

        // get the image's file location
        filePath = getRealPathFromUri(data.getData(), this);

        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK){
            try {
                //set picked image to the imageRender
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                profilePic.setImageBitmap(bitmap);
                uploadToCloudinary(filePath);
            } catch (IOException e) {
                e.printStackTrace();
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
                progressbar.setVisibility(View.VISIBLE);   //makes the progress bar visible
                imageProgress.setText("wait while picture uploads..");

            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                Double progress = (double) 100*(bytes/totalBytes);
                imageProgress.setText("Uploading... "+progress+"%");
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                progressbar.setVisibility(View.GONE);
                imageProgress.setText(" ");

            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                progressbar.setVisibility(View.GONE);
                imageProgress.setText("error "+ error.getDescription());
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                progressbar.setVisibility(View.GONE);
                imageProgress.setText("Reshedule "+error.getDescription());
            }
        })
                .option("public_id",  userID)
                .option("invalidate", true)
                .dispatch();

    }
}