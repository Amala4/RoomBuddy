package com.personalproject.roombuddy.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.textfield.TextInputLayout;
import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.database.SessionManager;
import com.personalproject.roombuddy.general.Homescreen;
import com.personalproject.roombuddy.general.Splash_Screen;
import com.personalproject.roombuddy.general.WelcomePage;

import org.bson.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

import static android.app.Activity.RESULT_OK;

public class PostRoomFragment extends Fragment   {

    //Variables
    App app;
    ArrayAdapter<String> campusSpinnerAdapter, stateSpinnerAdapter;
    Button submitRoom, selectPicture;
    DrawerLayout drawerLayout;
    ImageView menuIcon;
    ImageView roomPicImageView;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    MongoCollection<Document> roomDetailMongoCollection, profileDetailMongoCollection;
    private static final int PERMISSION_CODE = 1;
    private static final int PICK_IMAGE = 1;
    RadioButton selectedGender;
    RadioGroup radioGroup;
    Spinner stateSpinner, campusSpinner;
    String appID = "roombuddy-umrym";
    String chosenState, chosenCampus, lastNumber,
            userID, filePath, newNumber;
    String[] state = { "Imo", "Enugu", "Anambra"};
    String[] campus = { "IMSU", "Unizik, Nnewi", "Unizik, Awka"};
    TextInputLayout roomAddressAndDescription, aboutMe,
                    kindOfPerson, roommateRent, roomRent;
    TextView imageProgress;
    User user;
    View view;

    




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_post_room,container, false);





        // Initialize MongodbRealm and set up database
        Realm.init(Objects.requireNonNull(getContext()));
        app = new App(new AppConfiguration.Builder(appID).build());
        user = app.currentUser();
        assert user != null;
        userID = user.getId();
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
        roomDetailMongoCollection = mongoDatabase.getCollection("Room_Details");
        profileDetailMongoCollection = mongoDatabase.getCollection("Profile_Details");





        // Hooks xml fields to variables
        aboutMe = view.findViewById(R.id.fr_pr_aboutMe);
        campusSpinner = view.findViewById(R.id.fr_pr_campusSpinner);
        drawerLayout = view.findViewById(R.id.drawer_layout);
        imageProgress = view.findViewById(R.id.fr_pr_imageProgress);
        kindOfPerson = view.findViewById(R.id.fr_pr_personLookingFor);
        menuIcon = view.findViewById(R.id.menu_icon);
        radioGroup = view.findViewById(R.id.fr_pr_radioGroup);
        roomAddressAndDescription = view.findViewById(R.id.fr_pr_roomAddressAndDescription);
        roomPicImageView = view.findViewById(R.id.fr_pr_roomImage);
        roomRent = view.findViewById(R.id.fr_pr_roomRent);
        roommateRent = view.findViewById(R.id.fr_pr_roommateRent);
        selectPicture = view.findViewById(R.id.fr_pr_postRoomSelectPicture);
        submitRoom = view.findViewById(R.id.fr_pr_postRoomSubmitButton);
        stateSpinner = view.findViewById(R.id.fr_pr_stateSpinner);





        //Create the ArrayAdapter instance having the state list
        stateSpinnerAdapter = new ArrayAdapter<String>(getActivity(),
                              android.R.layout.simple_spinner_item,state);



        stateSpinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);


        //Set the ArrayAdapter data on the Spinner
        stateSpinner.setAdapter(stateSpinnerAdapter);
        stateSpinner.setOnItemSelectedListener(new StateSpinnerClass());




        //Create the ArrayAdapter instance having the campus list
        campusSpinnerAdapter = new ArrayAdapter<String>(getContext(),
                               android.R.layout.simple_spinner_item,campus);



        campusSpinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);



        //Setting the ArrayAdapter data on the Spinner
        campusSpinner.setAdapter(campusSpinnerAdapter);
        campusSpinner.setOnItemSelectedListener(new CampusSpinnerClass());






        /*
        Finds the last ID
        of the last post made so as
        to name the new ID better
         */
        profileDetailMongoCollection.findOne(new Document()
                .append("User ID",userID))
                .getAsync(result -> {
            if(result.isSuccess())
            {
                Document resultData = result.get();

                if(resultData == null){


                    Toast.makeText(getContext(), "User does not exist", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    lastNumber = resultData.getString("Last Number");
                    Log.v("Data","Data Found Successfully");
                }

            }
            else
            {
                Toast.makeText(getContext(), "did not work", Toast.LENGTH_SHORT).show();
                Log.v("Data","Error:"+result.getError().toString());
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








        submitRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //request permission to access external storage
                submitRoom();
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




    class StateSpinnerClass implements AdapterView.OnItemSelectedListener
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
        {
            chosenState = state[position];        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO - Custom Code
        }
    }



    class CampusSpinnerClass implements AdapterView.OnItemSelectedListener
    {
        @Override
        public  void onItemSelected(AdapterView<?> parent, View v, int position, long id)
        {
            chosenCampus = campus[position];       }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO - Custom Code
        }
    }





    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
                                           @NonNull @org.jetbrains.annotations.NotNull String[] permissions,
                                           @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                accessTheGallery();
            }
            else
            {
                Toast.makeText(getContext(), "Access Permission denied", Toast.LENGTH_SHORT).show();

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
                roomPicImageView.setImageBitmap(bitmap);
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

                imageProgress.setText("starting to Upload..");

            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                Double progress = (double) 100*(bytes/totalBytes);
                imageProgress.setText("Uploading... "+progress+"%");
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                roomPicImageView.setImageDrawable(null);
                imageProgress.setText("Post Submitted successfully \n wait for the home page to refresh...");
                new Handler().postDelayed(new Runnable() {
                                              @Override
                                              public void run() {

                                                  Intent intent = new Intent(getContext(), Homescreen.class);
                                                  startActivity(intent);
                                              }
                                          },
                        //Pass the delay time here
                        2500);
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                imageProgress.setText("error "+ error.getDescription());
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                imageProgress.setText("Reshedule "+error.getDescription());
            }
        })
                .option("public_id", newNumber+"_"+"_"+userID)
                .option("invalidate", true)
                .dispatch();
        roomPicImageView.setImageBitmap(null);

    }


    public void submitRoom(){

        if (roomPicImageView.getDrawable() == null){
            Toast.makeText(getContext(), "Please upload a picture", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedGender = view.findViewById(radioGroup.getCheckedRadioButtonId());
        String gender = selectedGender.getText().toString();
        String _roomAddressAndDescription = roomAddressAndDescription.getEditText().getText().toString().trim();
        String _aboutMe = aboutMe.getEditText().getText().toString().trim();
        String _roomRent = roomRent.getEditText().getText().toString().trim();
        String _roommateRent = roommateRent.getEditText().getText().toString().trim();
        String _kindOfPerson = kindOfPerson.getEditText().getText().toString().trim();
        int i=Integer.parseInt(lastNumber);
        int j = i+1;
        newNumber =  String.valueOf(j);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDateTime = dateFormat.format(new Date()); // Find today's date

        if(!TextUtils.isEmpty(_roomAddressAndDescription) && !TextUtils.isEmpty(_aboutMe) &&
                !TextUtils.isEmpty(_roomRent) && !TextUtils.isEmpty(_roommateRent)
                && !TextUtils.isEmpty(_kindOfPerson) && !TextUtils.isEmpty(gender)) {

            imageProgress.setText("Please wait..");

            roomDetailMongoCollection.insertOne(new Document().append("User ID",userID).append("Room Address and Description",_roomAddressAndDescription)
                    .append("About Me",_aboutMe).append("Room Rent",_roomRent).append("Roommate Rent",_roommateRent)
                    .append("Kind of Person",_kindOfPerson).append("Gender",gender).append("State",chosenState)
                    .append("Campus",chosenCampus).append("Time",currentDateTime).append("SameNumber","1").append("Post number",newNumber+"_"+"_"+userID))
                    .getAsync(result -> {
                        if(result.isSuccess()) {
                            Log.v("Data", "Data Inserted Successfully");


                            //Updates the Last number
                            Document queryFilter = new Document().append("User ID",userID);
                            profileDetailMongoCollection.findOne(queryFilter).getAsync(task -> {
                                if (task.isSuccess()) {
                                    Document results = task.get();

                                    Log.v("FindFunction", "Found Something");

                                    profileDetailMongoCollection.updateOne(queryFilter, results.append("Last Number", newNumber)).getAsync(result1 -> {
                                        if (result1.isSuccess()) {
                                            Log.v("UpdateFunction", "Updated Data");
                                            //Uploads the picture to database
                                            uploadToCloudinary(filePath);
                                        }
                                        else {
                                            Log.v("UpdateFunction", "Error" + result1.getError().toString());
                                        }

                                    });

                                }

                                else {
                                    Log.v("Error", task.getError().toString());

                                }
                            });


                        }
                        else
                        {
                            // Log.v("Data","Error:"+result.getError().toString());
                            Toast.makeText(getContext(), "Data not saved", Toast.LENGTH_SHORT).show();

                        }
                    });

        } else {
            imageProgress.setText("Please fill in all the fields");
        }





    }

}
