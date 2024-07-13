package com.chinmay.diat;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Profile extends AppCompatActivity {

    DrawerLayout drawerLayout;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    TextInputLayout name,email, password;
    AppCompatButton updatebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile);

        // Initialize Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Instances
        drawerLayout = findViewById(R.id.drawerLayout);
        name  = findViewById(R.id.admin_name);
        email = findViewById(R.id.admin_mail);
        password = findViewById(R.id.admin_password);
        updatebtn = findViewById(R.id.update_profile_btn);
    }
    public void openDrawer(View view) {
        drawerLayout.open();
    }

    public void closeDrawer(View view) {
        drawerLayout.close();
    }
}