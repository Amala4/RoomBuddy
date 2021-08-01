package com.personalproject.roombuddy.general;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.adapters.RoomAdapter;
import com.personalproject.roombuddy.database.SessionManager;
import com.personalproject.roombuddy.fragments.MessagesFragment;
import com.personalproject.roombuddy.fragments.PostRoomFragment;
import com.personalproject.roombuddy.fragments.ProfileFragment;
import com.personalproject.roombuddy.helperClasses.EndlessScrollListener;
import com.personalproject.roombuddy.helperClasses.Space;
import com.personalproject.roombuddy.homePageAdSlider.AdSliderAdapter;
import com.personalproject.roombuddy.homePageAdSlider.PicassoImageLoadingService;
import com.personalproject.roombuddy.models.AdImages;
import com.personalproject.roombuddy.models.Room;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import ss.com.bannerslider.Slider;
import ss.com.bannerslider.event.OnSlideClickListener;


public class Homescreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //Variables
    App app;
    BottomNavigationView bottomNavigationView;
    ImageView menuIcon;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    MongoCollection<Document> mongoCollection;
    NavigationView navigationView;
    public DrawerLayout drawerLayout;
    RecyclerView recyclerViewRooms;
    RelativeLayout contentView;
    RoomAdapter roomAdapter;
    Slider slider;
    static final float END_SCALE = 0.7f;
    String appID = "roombuddy-umrym";
    String link0,link1,link2,link;
    User user;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);





        // Initialize MongodbRealm and set up database
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(appID).build());
        user = app.currentUser();
        assert user != null;
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("RoomBuddyDB");
        mongoCollection = mongoDatabase.getCollection("Room_Details");




        // Hooks xml fields to variables
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home_button);
        contentView = findViewById(R.id.content);
        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        navigationView = findViewById(R.id.menu_navigation_view);
        recyclerViewRooms = findViewById(R.id.recyclerViewRooms);




        /*
        Sets up on-click listener
        for the bottom navigation icons
         */
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                Fragment fragment;


                switch (item.getItemId()) {

                    case R.id.home_button: //home button
                        Intent intent = new Intent(getApplicationContext(), Homescreen.class);
                        startActivity(intent);
                        break;



                    case R.id.messages_button: //messages button
                        fragment = new MessagesFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                        break;



                    case R.id.post_button: //Post button
                        fragment = new PostRoomFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                        break;



                    case R.id.profile_button:  //History button
                        fragment = new ProfileFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                        break;
                }


                return true;
            }
        });




        navigationDrawer();      //Sets up the behaviour of the side menu




        roomAdapter = new RoomAdapter(this);  //Create an instance of the RoomAdapter



        /*
        Creates new
        GridLayoutManager
         */
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,
                2,//span count no of items in single row
                GridLayoutManager.VERTICAL,//Orientation
                false);//reverse scrolling of recyclerview




        recyclerViewRooms.setLayoutManager(gridLayoutManager);  //set layout manager as gridLayoutManager



        /*
        Creates new EndlessScrollListener
        for endless recyclerview loading
         */
        EndlessScrollListener endlessScrollListener = new EndlessScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!roomAdapter.loading)
                    feedData(); //method to continuously feed data
            }
        };



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




        recyclerViewRooms.addOnScrollListener(endlessScrollListener);  //add on on Scroll listener
        recyclerViewRooms.addItemDecoration(new Space(1, 40, true, 0));   //add space between cards
        recyclerViewRooms.setAdapter(roomAdapter);   //Finally set the adapter
        endlessScrollListener.onLoadMore(0, 0);   //load first page of recyclerview


        feedAdImages();


        /*
        Sets up sliding
        ad using slider
         */
        slider = findViewById(R.id.banner_slider1);
        slider.setAdapter(new AdSliderAdapter());
        Slider.init(new PicassoImageLoadingService(this));
        slider.setSelectedSlide(1);
        slider.setOnSlideClickListener(new OnSlideClickListener() {
            @Override
            public void onSlideClick(int position) {


                switch (position) {
                    case 0:
                        link = link0;  //link to first ad slide
                        break;
                    case 1:
                        link = link1;  //link to second ad slide
                        break;
                    case 2:
                        link = link2;  //link to third ad slide
                        break;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link));
                startActivity(intent);
            }
        });


    }







    private void feedAdImages() {


        final List<AdImages> ad_Images = new ArrayList<>();

        final String[] adImageUrL = new String[1];
        final String[] adWebUrL = new String[1];

        adImageUrL[0] = "";
        adWebUrL[0] = "";

        RealmResultTask<MongoCursor<Document>> findTask = mongoCollection.find(new Document().append("Ad document", "ads")).iterator();

        findTask.getAsync(task-> {
            if (task.isSuccess()) {
                Log.v("Task Error","Task is succesful");


                MongoCursor<Document> results = task.get();


                if(results.hasNext()){

                    while (results.hasNext()) {
                        Document currentDoc = results.next();
                        if (currentDoc != null) {

                            Log.v("Task Error","CurrentDoc is not null");



                            adWebUrL[0] =  currentDoc.getString("AdUrl");


                            adImageUrL[0] = MediaManager.get().url().transformation(
                                    new Transformation().quality(80)).generate("Ad_number"+currentDoc.getString("Adnumber"));


                            AdImages addImages = new AdImages(adImageUrL[0],
                                    adWebUrL[0]);
                            ad_Images.add(addImages);

                        }
                        else
                            {
                            Toast.makeText(this, "CurrentDoc is null", Toast.LENGTH_SHORT).show();

                        }
                    }

                            //add ad images to recyclerview
                    link0 = ad_Images.get(0).getAdwebUrl();
                    link1 = ad_Images.get(1).getAdwebUrl();
                    link2 = ad_Images.get(2).getAdwebUrl();
                }


                else
                    {
                    Toast.makeText(this, "No more found", Toast.LENGTH_SHORT).show();
                }

            }

            else
                {
                Toast.makeText(this, "Contents do not load", Toast.LENGTH_SHORT).show();
            }
        });
    }




    //Navigation Drawer functions
    private void navigationDrawer() {

        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_history);



        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        animateNavigationDrawer();    //Adds animation to menu


    }






    private void animateNavigationDrawer() {

        //Add any color or remove it to use the default one!
        //To make it transparent use Color.Transparent in side setScrimColor();
        //drawerLayout.setScrimColor(Color.TRANSPARENT);
        // to add colour, activate this. drawerLayout.setScrimColor(getResources().getColor(R.color.colorPrimary));
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                                           @Override
                                           public void onDrawerSlide(View drawerView, float slideOffset) {

                                               // Scale the View based on current slide offset
                                               final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                                               final float offsetScale = 1 - diffScaledOffset;
                                               contentView.setScaleX(offsetScale);
                                               contentView.setScaleY(offsetScale);

                                               // Translate the View, accounting for the scaled width
                                               final float xOffset = drawerView.getWidth() * slideOffset;
                                               final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                                               final float xTranslation = xOffset - xOffsetDiff;
                                               contentView.setTranslationX(xTranslation);
                                           }
                                       }
        );
    }




    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else
            super.onBackPressed();


    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.nav_history:   //History button
                startActivity(new Intent(getApplicationContext(), History.class));
                break;


            case R.id.nav_all_logout:  //Logout button

                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(this);

                // Set the message show for the Alert time
                builder.setMessage("Do you want to log out ?");
                builder.setTitle("Log out");
                builder.setPositiveButton(
                        "Yes",
                        new DialogInterface
                                .OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which)
                            {

                                //Updates session manager
                                SessionManager sessionManager = new SessionManager(Homescreen.this, SessionManager.SESSION_USERSESSION);
                                sessionManager.logoutUserFromSession();
                                user.logOutAsync( result -> {
                                    if (result.isSuccess()) {
                                        Log.v("AUTH", "Successfully logged out.");

                                        startActivity(new Intent(getApplicationContext(), WelcomePage.class));
                                    } else {
                                        Log.e("AUTH", result.getError().toString());
                                    }
                                });

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

                break;

            case R.id.nav_contact_admin:   //Contact Admin button
                startActivity(new Intent(getApplicationContext(), ContactAdmin.class));
                break;


            case R.id.nav_rate_us:   //Rate us
                Toast.makeText(getApplicationContext(), "Feature coming soon", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_share:   //Share
                Toast.makeText(getApplicationContext(), "Feature coming soon", Toast.LENGTH_SHORT).show();
                break;


            case R.id.nav_terms_and_conditions:   //Terms and condition page
                Toast.makeText(getApplicationContext(), "Feature coming soon", Toast.LENGTH_SHORT).show();
                break;


            case R.id.nav_privacy_policy:   //Privacy policy page
                Toast.makeText(getApplicationContext(), "Feature coming soon", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_setting:   //Privacy policy page
                Toast.makeText(getApplicationContext(), "Feature coming soon", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;

    }




    //Load room Data from the server here
    private void feedData() {

        //show loading in recyclerview
        roomAdapter.showLoading();
        final List<Room> rooms = new ArrayList<>();

            final String[] state = new String[1];
            final String[] campus = new String[1];
            final String[] gender = new String[1];
            final String[] image = new String[1];
            final String[] currentPage = new String[1];
            final String[] postNumber = new String[1];
            final String[] userID = new String[1];
            state[0] = "";
            campus[0] = "";
            gender[0] = "";
            image[0] = "";
            currentPage[0] = "";
            postNumber[0] = "";
            userID[0] = "";

            RealmResultTask<MongoCursor<Document>> findTask = mongoCollection.find
                    (new Document().append("SameNumber", "1")).
                     sort(new Document().append("Time",-1)).iterator();


            findTask.getAsync(task-> {
                if (task.isSuccess()) {
                    Log.v("Task Error","Task is succesful");

                    MongoCursor<Document> results = task.get();
                    if(results.hasNext()){
                        Log.v("Task Error","Result has next");

                        while (results.hasNext()) {
                        Document currentDoc = results.next();
                        if (currentDoc != null) {

                            Log.v("Task Error","CurrentDoc is not null");



                            state[0] =  currentDoc.getString("State");
                            campus[0] = currentDoc.getString("Campus");
                            gender[0] = currentDoc.getString("Gender");
                            postNumber[0] = currentDoc.getString("Post number");
                            userID[0] = currentDoc.getString("User ID");
                            currentPage[0] = "homePage";


                            Log.v("Task Error","data assigned well");

                           // image[0] = MediaManager.get().url().transformation(new Transformation().height(300).crop("limit")).generate(currentDoc.getString("Unique number"));
                            image[0] = MediaManager.get().url()
                                    .transformation(new Transformation().aspectRatio("1.0")
                                            .width(450).height(300).crop("lfill")).generate(currentDoc.getString("Post number"));


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
                            }
                        }, 20);
                    }


                    else{
                        Toast.makeText(this, "No more found", Toast.LENGTH_SHORT).show();
                    }



                }

                else
                    {
                    Toast.makeText(this, "Contents do not load", Toast.LENGTH_SHORT).show();
                }
                });
    }


}
