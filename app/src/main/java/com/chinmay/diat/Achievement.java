package com.chinmay.diat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class Achievement extends AppCompatActivity {

    private ImageSlider imageSlider;
    private TextView achievementName, achievementDescription;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    private boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.achievement);

        isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        imageSlider = findViewById(R.id.image_slider);
        achievementName = findViewById(R.id.achievement_name_textview);
        achievementDescription = findViewById(R.id.achievement_description_textview);

        checkIfUserIsLoggedIn();

        // Get the passed data
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        ArrayList<String> imageUrls = getIntent().getStringArrayListExtra("imageUrls");

        // Set the name and description
        achievementName.setText("Name:"+name);
        achievementDescription.setText(description);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                startActivity(new Intent(Achievement.this, Home.class));
                finishAffinity();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.Achievements) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.logout) {
                drawerLayout.closeDrawer(GravityCompat.START);
                logoutUser(navigationView);
                return true;
            }
            return false;
        });

        // Create a list of SlideModel objects
        List<SlideModel> slideModels = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            slideModels.add(new SlideModel(imageUrl, ScaleTypes.CENTER_CROP));
        }

        // Set the images in the ImageSlider
        imageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);

    }

    private void checkIfUserIsLoggedIn() {
        if (!isLoggedIn){
            navigationView.getMenu().findItem(R.id.logout).setVisible(false);
            //Toast.makeText(getApplicationContext(),"Logged In",Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(getApplicationContext(),"Not Logged In",Toast.LENGTH_SHORT).show();
        }
    }

    public void logoutUser(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(Achievement.this, Login.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
