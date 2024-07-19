package com.chinmay.diat;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.util.ArrayList;
import java.util.List;

public class Achievement extends AppCompatActivity {

    ImageSlider imageSlider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.achievement);

        imageSlider= findViewById(R.id.image_slider);

        // Create a list of SlideModel objects
        List<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.diat_logo, ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.students, ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.achievement_logo, ScaleTypes.CENTER_CROP));

        // Set the images in the ImageSlider
        imageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
    }
}