package com.personalproject.roombuddy.general;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.personalproject.roombuddy.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.bson.Document;


public class FullPic extends AppCompatActivity {

    //Variables
    ImageView fullPic;

    RelativeLayout progressbar;

    String publicId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_pic);



        // Hooks- links variables to xml views
        fullPic = findViewById(R.id.full_pic);
        progressbar = findViewById(R.id.fp_progress_bar);


        progressbar.setVisibility(View.VISIBLE);   //makes the progress bar visible

        //Get the image public ID from previous screens using Intent
        publicId = getIntent().getStringExtra("Public ID");



         /*
        Finds the
        image from cloud
         */


        Picasso.get().load(MediaManager.get().url().transformation(
                new Transformation().quality(80)).generate(publicId))
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(fullPic, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                        Log.v("Load full pic", "Picture loaded");

                        progressbar.setVisibility(View.GONE);   //makes the progress bar visible

                    }

                    @Override
                    public void onError(Exception e) {

                        Toast.makeText(getApplicationContext(), "Error in loading Image, please re-launch the app", Toast.LENGTH_LONG).show();
                        progressbar.setVisibility(View.GONE);   //makes the progress bar visible
                    }
                });

    }

    public void back(View view){

        super.onBackPressed();
    }
}