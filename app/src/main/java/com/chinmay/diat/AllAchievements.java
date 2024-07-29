package com.chinmay.diat;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.all_achievements);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        achievementModelList = new ArrayList<>();
        filteredList = new ArrayList<>(); // List to hold filtered achievements
        achievementsAdapter = new AchievementsAdapter(this, filteredList);
        recyclerView.setAdapter(achievementsAdapter);

        db = FirebaseFirestore.getInstance();

        loadAchievements();

        findViewById(R.id.addAchivementbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AddAchievements.class));
            }
        });

        searchAchivement = findViewById(R.id.searchAchivement);
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
