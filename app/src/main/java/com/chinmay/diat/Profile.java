package com.chinmay.diat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private static final int REQUEST_CODE_IMAGE_PICKER = 5;

    DrawerLayout drawerLayout;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    TextInputLayout name, designation;
    NavigationView navigationView;
    TextView welcomeUserText;
    AppCompatButton updatebtn;
    ImageButton camerabtn;
    ImageView profileImageView;
    Uri imageUri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Instances
        navigationView = findViewById(R.id.navigationView);
        drawerLayout = findViewById(R.id.drawerLayout);
        name = findViewById(R.id.admin_name);
        designation = findViewById(R.id.admin_designation);
        updatebtn = findViewById(R.id.update_profile_btn);
        welcomeUserText = findViewById(R.id.welcomeUserText);
        camerabtn = findViewById(R.id.camerabtn);
        profileImageView = findViewById(R.id.profile_image);

        // Fetch user data including profile image
        fetchUserData();

        // Set update button click listener
        updatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserProfile();
            }
        });

        // Set camera button click listener using ImagePicker library
        camerabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchImagePicker();
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.Achievements) {
                startActivity(new Intent(Profile.this, AllAchievements.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.logout) {
                drawerLayout.closeDrawer(GravityCompat.START);
                logoutUser(navigationView);
                return true;
            }
            return false;
        });

        FirebaseUtils firebaseUtils = new FirebaseUtils();
        // Fetch and display user name and email in the drawer header
        View headerView = navigationView.getHeaderView(0);
        TextView drawerUserName = headerView.findViewById(R.id.drawerUserName);
        TextView drawerUserEmail = headerView.findViewById(R.id.drawerUserEmail);
        firebaseUtils.fetchAndDisplayUserInfo(drawerUserName, drawerUserEmail);
    }

    // Method to fetch user data including profile image
    private void fetchUserData() {
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String username = documentSnapshot.getString("username");
                                String userDesignation = documentSnapshot.getString("designation");
                                String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                                name.getEditText().setText(username);
                                designation.getEditText().setText(userDesignation);
                                welcomeUserText.setText("Welcome " + username);

                                if (profileImageUrl != null) {
                                    Glide.with(Profile.this).load(profileImageUrl).into(profileImageView);
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(),"Invalid Or Null Profile Picture",Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(Profile.this, "No user data found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Profile", "Failed to fetch user data: " + e.getMessage());
                            Toast.makeText(Profile.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Method to update user profile in Firestore
    private void updateUserProfile() {
        String newUsername = name.getEditText().getText().toString().trim();
        String newDesignation = designation.getEditText().getText().toString().trim();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            Map<String, Object> userData = new HashMap<>();
            userData.put("username", newUsername);
            userData.put("designation", newDesignation);

            db.collection("users").document(uid)
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Profile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Profile", "Error writing document", e);
                            Toast.makeText(Profile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Method to launch ImagePicker..
    private void launchImagePicker() {
        ImagePicker.Companion.with(Profile.this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_IMAGE_PICKER);
    }

    // Handle result from ImagePicker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            // Get image URI
            imageUri = data.getData();

            // Display the selected image in the ImageView
            Glide.with(Profile.this).load(imageUri).into(profileImageView);

            // Upload image to Firebase Storage
            uploadImageToFirebaseStorage();
        }
    }

    // Method to upload image to Firebase Storage
    private void uploadImageToFirebaseStorage() {
        if (imageUri != null) {
            StorageReference fileRef = storageReference.child("profile_images/" + currentUser.getUid());

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get the uploaded image URL
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Update Firestore with the new image URL
                                    updateProfileImageUrl(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Profile.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                            Log.e("Profile", "Failed to upload image: " + e.getMessage());
                        }
                    });
        }
    }

    // Method to update profile image URL in Firestore
    private void updateProfileImageUrl(String imageUrl) {
        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid)
                    .update("profileImageUrl", imageUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Update UI or show success message
                            Toast.makeText(Profile.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();

                            // Load the new image using Glide
                            Glide.with(Profile.this).load(imageUrl).into(profileImageView);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Profile", "Error updating profile image URL: " + e.getMessage());
                            Toast.makeText(Profile.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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

    // Method to open the navigation drawer
    public void openDrawer(View view) {
        drawerLayout.open();
    }

    // Method to close the navigation drawer
    public void closeDrawer(View view) {
        drawerLayout.close();
    }
}
