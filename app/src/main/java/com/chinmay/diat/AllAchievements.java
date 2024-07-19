package com.chinmay.diat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.all_achievements);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        achievementModelList = new ArrayList<>();
        achievementsAdapter = new AchievementsAdapter(this, achievementModelList);
        recyclerView.setAdapter(achievementsAdapter);

        db = FirebaseFirestore.getInstance();

        loadAchievements();
        findViewById(R.id.addAchivementbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),AddAchievements.class));
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
                        achievementsAdapter.notifyDataSetChanged();
                    }
                });
    }
}
