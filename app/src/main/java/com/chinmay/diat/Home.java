package com.chinmay.diat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 101;

    DrawerLayout drawerLayout;
    EditText searchEditText;
    RecyclerView recyclerView;
    DepartmentAdapter adapter;
    List<DepartmentModel> departmentModelList;
    TextView shortnametextview;
    ImageButton profile;
    NavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize Views
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        searchEditText = findViewById(R.id.searchViewSearch);
        recyclerView = findViewById(R.id.recyclerViewDepartments);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        departmentModelList = new ArrayList<>();
        adapter = new DepartmentAdapter(this, departmentModelList);
        recyclerView.setAdapter(adapter);

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
        adapter.filter(" ");    // Display all departments initially

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

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.Achievements) {
                startActivity(new Intent(Home.this, AllAchievements.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.logout) {
                drawerLayout.closeDrawer(GravityCompat.START);
                logoutUser(navigationView);
                return true;
            }
            return false;
        });

        profile.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Profile.class)));

        // Fetch the username and display initials
        FirebaseUtils firebaseUtils = new FirebaseUtils();
        firebaseUtils.fetchAndDisplayInitials(shortnametextview);

        // Check and request permissions if needed
        checkAndRequestPermissions();
    }

    private void addDepartment(String name, int iconResId) {
        DepartmentModel departmentModel = new DepartmentModel(name, iconResId);
        departmentModelList.add(departmentModel);
        adapter.notifyDataSetChanged();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // Request MANAGE_EXTERNAL_STORAGE permission
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("This app needs permission to access all files on your device. Please grant this permission in the settings.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Redirect to the settings page if user agrees
                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            // Permission not granted, handle appropriately
                            Toast.makeText(this, "Permission denied. Cannot proceed with media operations.", Toast.LENGTH_SHORT).show();
                        })
                        .create()
                        .show();
            } else {
                // Permissions already granted
                proceedWithMediaOperations();
            }
        } else {
            // For Android 10 and below, request legacy storage permissions
            boolean readExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean writeExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            if (!readExternalStorage || !writeExternalStorage) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSION_REQUEST_CODE);
            } else {
                // Permissions already granted
                proceedWithMediaOperations();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                // All permissions granted
                proceedWithMediaOperations();
            } else {
                // Permissions denied
                Toast.makeText(this, "Permissions denied. Cannot proceed with media operations.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permission granted
                    proceedWithMediaOperations();
                } else {
                    // Permission denied
                    Toast.makeText(this, "Permission denied. Cannot proceed with media operations.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void proceedWithMediaOperations() {
        // Your media operations code here
    }

    public void logoutUser(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Sign out the current authenticated user from Firebase
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
            finishAffinity();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            // User clicked No, do nothing
            dialog.dismiss();
        });
        builder.create().show();
    }

    public void openDrawer(View view) {
        drawerLayout.open();
    }

    public void closeDrawer(View view) {
        drawerLayout.close();
    }
}
