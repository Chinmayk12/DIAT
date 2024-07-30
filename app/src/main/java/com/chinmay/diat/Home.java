package com.chinmay.diat;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

    private static final int MEDIA_PERMISSION_REQUEST_CODE = 100;
    private static final int MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 101;
    private static final String PREFS_NAME = "PermissionPrefs";
    private static final String KEY_MANAGE_EXTERNAL_STORAGE_GRANTED = "manage_external_storage_granted";

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
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

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

        // Fetch and display user name and email in the drawer header
        View headerView = navigationView.getHeaderView(0);
        TextView drawerUserName = headerView.findViewById(R.id.drawerUserName);
        TextView drawerUserEmail = headerView.findViewById(R.id.drawerUserEmail);
        firebaseUtils.fetchAndDisplayUserInfo(drawerUserName, drawerUserEmail);

        // Check and request permissions if needed
        checkAndRequestPermissions();
    }

    private void addDepartment(String name, int iconResId) {
        DepartmentModel departmentModel = new DepartmentModel(name, iconResId);
        departmentModelList.add(departmentModel);
        adapter.notifyDataSetChanged();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED /*||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED*/) {

                // Request media permissions
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        /*Manifest.permission.READ_MEDIA_AUDIO*/
                }, MEDIA_PERMISSION_REQUEST_CODE);
            } else {
                // Media permissions already granted, show dialog for MANAGE_EXTERNAL_STORAGE
                if (!sharedPreferences.getBoolean(KEY_MANAGE_EXTERNAL_STORAGE_GRANTED, false)) {
                    showManageExternalStorageDialog();
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and 12
            if (!Environment.isExternalStorageManager()) {
                // Request MANAGE_EXTERNAL_STORAGE permission
                if (!sharedPreferences.getBoolean(KEY_MANAGE_EXTERNAL_STORAGE_GRANTED, false)) {
                    showManageExternalStorageDialog();
                }
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
                }, MEDIA_PERMISSION_REQUEST_CODE);
            } else {
                // Permissions already granted
                proceedWithMediaOperations();
            }
        }
    }

    private void showManageExternalStorageDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs 'Manage All Files' permission to access all files on your device. Please grant this permission in the settings.")
                    .setPositiveButton("Go to Settings", (dialog, which) -> {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
                        } catch (Exception e) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MEDIA_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Toast.makeText(this, "Media permissions granted", Toast.LENGTH_SHORT).show();
                // Show dialog for MANAGE_EXTERNAL_STORAGE if needed
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !sharedPreferences.getBoolean(KEY_MANAGE_EXTERNAL_STORAGE_GRANTED, false)) {
                    showManageExternalStorageDialog();
                } else {
                    proceedWithMediaOperations();
                }
            } else {
                Toast.makeText(this, "Media permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();
                    // Save preference indicating that the permission has been granted
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(KEY_MANAGE_EXTERNAL_STORAGE_GRANTED, true);
                    editor.apply();
                    proceedWithMediaOperations();
                } else {
                    Toast.makeText(this, "MANAGE_EXTERNAL_STORAGE permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void proceedWithMediaOperations() {
        // Your media operations code here
    }

    public void logoutUser(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(Home.this, Login.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
