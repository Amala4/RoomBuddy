package com.personalproject.roombuddy.homePageAdSlider;


import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

public class AdSliderAdapter extends SliderAdapter {



    @Override
    public int getItemCount() {
        return 3;
    }



    @Override
    public void onBindImageSlide(int position, ImageSlideViewHolder imageSlideViewHolder) {


        switch (position) {
            case 0:
                imageSlideViewHolder.bindImageSlide("https://res.cloudinary.com/amala4/image/upload/v1618139236/Ad_number1.jpg");
                break;
            case 1:
                imageSlideViewHolder.bindImageSlide("https://res.cloudinary.com/amala4/image/upload/v1618139236/Ad_number2.jpg");
                break;
            case 2:
                imageSlideViewHolder.bindImageSlide("https://res.cloudinary.com/amala4/image/upload/v1618139236/Ad_number3.jpg");
                break;
        }





    }
}
