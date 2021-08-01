package com.personalproject.roombuddy.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.personalproject.roombuddy.R;
import com.personalproject.roombuddy.general.RoomDetails;
import com.personalproject.roombuddy.models.AdImages;
import com.personalproject.roombuddy.models.Room;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static io.realm.Realm.getApplicationContext;

public class AdAdapter extends RecyclerView.Adapter {
    List<AdImages> adImages;
    Context adContext;
    int LoadingItemPos;


    public AdAdapter(Context adContext) {
        adImages = new ArrayList<>();
        this.adContext = adContext;
    }
    //method to add ad images as soon as they fetched
    public void add_AdImages(List<AdImages> ad_images) {
        int lastPos = adImages.size();
        this.adImages.addAll(ad_images);
        notifyItemRangeInserted(lastPos, adImages.size());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            View row = inflater.inflate(R.layout.custom_colum_ad_images, parent, false);
            return new AdAdapter.AdViewHolder(row);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //get current product
        final AdImages currentAd = adImages.get(position);
        if (holder instanceof AdAdapter.AdViewHolder) {
            AdAdapter.AdViewHolder adViewHolder = (AdAdapter.AdViewHolder) holder;

            //bind ad pictures with view
            Picasso.get().load(currentAd.getAdimageUrl()).into(adViewHolder.ad_image);

            adViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // user selected one ad item now you can link user to the details of that id

                    Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(currentAd.getAdwebUrl()));
                    adContext.startActivity(intent);
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return adImages.size();
    }

    //Holds view of ads with information
    private static class AdViewHolder extends RecyclerView.ViewHolder {
        ImageView ad_image;

        public AdViewHolder(View itemView) {
            super(itemView);
            ad_image = itemView.findViewById(R.id.ad_image);
        }
    }
}
