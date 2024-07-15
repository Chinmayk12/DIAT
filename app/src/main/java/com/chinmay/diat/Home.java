package com.chinmay.diat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    DrawerLayout drawerLayout;
    EditText searchEditText;
    RecyclerView recyclerView;
    DepartmentAdapter adapter;
    List<DepartmentModel> departmentModelList;
    TextView shortnametextview;
    ImageButton profile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Instances
        drawerLayout = findViewById(R.id.drawerLayout);
        searchEditText = findViewById(R.id.searchViewSearch);
        recyclerView = findViewById(R.id.recyclerViewDepartments);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        departmentModelList = new ArrayList<>();
        adapter = new DepartmentAdapter(this, departmentModelList);
        recyclerView.setAdapter(adapter);

        // Instances
        profile = findViewById(R.id.profile_icon);
        shortnametextview = findViewById(R.id.shortnametextview);

        // Manually add departments (replace with your actual data)
        addDepartment("Administration", R.drawable.administration_logo);
        addDepartment("Academics", R.drawable.academics_logo);
        addDepartment("Research", R.drawable.research_logo);
        addDepartment("Faculty", R.drawable.faculty);
        addDepartment("Students", R.drawable.students);
        addDepartment("Non DIAT Students", R.drawable.others_students);
        addDepartment("Reimbursement", R.drawable.reimbursement_logo);
        adapter.filter(" ");    // For to display all the departments when we open the app

        // Add more departments as needed

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Profile.class));
            }
        });

        // Fetch the username and display initials
        FirebaseUtils firebaseUtils = new FirebaseUtils();
        firebaseUtils.fetchAndDisplayInitials(shortnametextview);
    }

    private void addDepartment(String name, int iconResId) {
        DepartmentModel departmentModel = new DepartmentModel(name, iconResId);
        departmentModelList.add(departmentModel);
        adapter.notifyDataSetChanged();
    }


    public void openDrawer(View view) {
        drawerLayout.open();
    }

    public void closeDrawer(View view) {
        drawerLayout.close();
    }
}
