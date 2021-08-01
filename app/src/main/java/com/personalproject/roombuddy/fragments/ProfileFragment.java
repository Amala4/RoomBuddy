package com.personalproject.roombuddy.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.textfield.TextInputLayout;
import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.adapters.RoomAdapter;
import com.personalproject.roombuddy.general.FullPic;
import com.personalproject.roombuddy.general.Homescreen;
import com.personalproject.roombuddy.general.ProfileEdit;
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

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    //Variables
    App app;
    CircleImageView profilePic;
    DrawerLayout drawerLayout;
    ImageView editName, editPhoneNo, menuIcon;
    LinearLayout changePicture;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    MongoCollection<Document> profileDetailsMongoCollection;
    private static final int PERMISSION_CODE = 1;
    private static final int PICK_IMAGE = 1;
    RelativeLayout progressbar;
    String appID = "roombuddy-umrym";

    String user_gender, user_fullName, user_email, user_phoneNo,profileImageURL,
            user_state, user_campus, user_dateOfRegistration, userID, filePath;

    TextView genderTextView, fullNameTextView, emailTextView, phoneNoTextView,imageProgress,
            stateTextView, campusTextView, dateOfRegistrationTextView, verification_status;

    User user;
    View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);




        // Initialize the MongodbRealm database
        Realm.init(Objects.requireNonNull(getContext()));
        app = new App(new AppConfiguration.Builder(appID).build());
        user = app.currentUser();
        assert user != null;
        userID = Objects.requireNonNull(user).getId();
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
        profileDetailsMongoCollection = mongoDatabase.getCollection("Profile_Details");





        // Hooks xml fields to variables
        campusTextView = view.findViewById(R.id.profile_campus);
        changePicture = view.findViewById(R.id.profile_change_picture);
        dateOfRegistrationTextView = view.findViewById(R.id.profile_registration_date);
        drawerLayout = ((Homescreen) Objects.requireNonNull(getActivity())).drawerLayout;
        editName = view.findViewById(R.id.fullname_edit_icon);
        editPhoneNo = view.findViewById(R.id.phone_number_edit_icon);
        emailTextView = view.findViewById(R.id.profile_email_address);
        fullNameTextView = view.findViewById(R.id.profile_full_name);
        genderTextView = view.findViewById(R.id.profile_gender);
        menuIcon = view.findViewById(R.id.menu_icon);
        phoneNoTextView = view.findViewById(R.id.profile_phone_number);
        profilePic = view.findViewById(R.id.profile_image);
        progressbar = view.findViewById(R.id.profile_progress_bar);
        stateTextView = view.findViewById(R.id.profile_state);
        verification_status = view.findViewById(R.id.profile_verified_text);
        imageProgress = view.findViewById(R.id.profile_imageProgress);





        progressbar.setVisibility(View.VISIBLE);   //makes the progress bar visible





        /*
        Finds the user profile
        image from cloud
         */
        profileImageURL = MediaManager.get().url().transformation
                (new Transformation().width(450).height(300).crop("limit"))
                .generate(userID);
        Picasso.get().load(profileImageURL).into(profilePic);







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


            } else {

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


        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent;

                intent = new Intent(getContext(), FullPic.class);
                intent.putExtra("Public ID", userID);
                startActivity(intent);

                //Animation, executes the animation
                ((Activity) view.getContext()).overridePendingTransition(R.anim.left_side_anim, R.anim.hold_anim);


            }
        });



        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //request permission to access external storage
                openProfileEditPage();
            }
        });



        editPhoneNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //request permission to access external storage
                openProfileEditPage();
            }
        });


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





    public void openProfileEditPage(){
        Intent intent = new Intent(getContext(), ProfileEdit.class);
        startActivity(intent);
    }

    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            accessTheGallery();
        }
        else {

            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
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
                Toast.makeText(getContext(), "Access Permission denied", Toast.LENGTH_SHORT).show();
                accessTheGallery();
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
        filePath = getRealPathFromUri(data.getData(), Objects.requireNonNull(getActivity()));

        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK){
            try {
                //set picked image to the imageRender
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                profilePic.setImageBitmap(bitmap);
                progressbar.setVisibility(View.VISIBLE);
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