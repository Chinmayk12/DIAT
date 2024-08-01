package com.chinmay.diat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllAchievements extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AchievementsAdapter achievementsAdapter;
    private List<AchievementModel> achievementModelList;
    private List<AchievementModel> filteredList;
    private FirebaseFirestore db;
    private EditText searchAchivement;
    private Button addAchievementButton;
    private boolean isLoggedIn;
    NavigationView navigationView;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.all_achievements);

        // Check if the user is logged in
        isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        recyclerView = findViewById(R.id.recyclerView);
        searchAchivement = findViewById(R.id.searchAchivement);
        addAchievementButton = findViewById(R.id.addAchivementbtn);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        achievementModelList = new ArrayList<>();
        filteredList = new ArrayList<>();
        achievementsAdapter = new AchievementsAdapter(this, filteredList, isLoggedIn);
        recyclerView.setAdapter(achievementsAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Load achievements from Firestore
        loadAchievements();

        checkIfUserIsLoggedIn();

        // Set button click listener
        addAchievementButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AddAchievements.class));
        });

        // Set up search functionality
        searchAchivement.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter achievements as user types
                filterAchievements(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                startActivity(new Intent(AllAchievements.this, Home.class));
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
    }

    private void checkIfUserIsLoggedIn() {
        if (!isLoggedIn){
            navigationView.getMenu().findItem(R.id.logout).setVisible(false);
            //Toast.makeText(getApplicationContext(),"Logged In",Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(getApplicationContext(),"Not Logged In",Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAchievements() {
        db.collection("achievements")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }

                        achievementModelList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            AchievementModel achievementModel = document.toObject(AchievementModel.class);
                            achievementModelList.add(achievementModel);
                        }
                        // Initially, display all achievements
                        filteredList.clear();
                        filteredList.addAll(achievementModelList);
                        achievementsAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void logoutUser(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(AllAchievements.this, Login.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void filterAchievements(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(achievementModelList);
        } else {
            for (AchievementModel achievement : achievementModelList) {
                if (achievement.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(achievement);
                }
            }
        }
        achievementsAdapter.notifyDataSetChanged();
    }
}
