package com.chinmay.diat;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.util.ArrayList;
import java.util.List;

public class Achievement extends AppCompatActivity {

    private ImageSlider imageSlider;
    private TextView achievementName, achievementDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.achievement);

        imageSlider = findViewById(R.id.image_slider);
        achievementName = findViewById(R.id.achievement_name_textview);
        achievementDescription = findViewById(R.id.achievement_description_textview);

        // Get the passed data
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        ArrayList<String> imageUrls = getIntent().getStringArrayListExtra("imageUrls");

        // Set the name and description
        achievementName.setText("Name:"+name);
        achievementDescription.setText(description);

        // Create a list of SlideModel objects
        List<SlideModel> slideModels = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            slideModels.add(new SlideModel(imageUrl, ScaleTypes.CENTER_CROP));
        }

        // Set the images in the ImageSlider
        imageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
    }
}
