package com.chinmay.diat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.Toast;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AddAchievements extends AppCompatActivity {
    private static final int MAX_IMAGES_TO_SELECT = 3;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ActivityResultLauncher<Intent> launcher;

    private TextInputLayout nameLayout, descriptionLayout;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;
    DrawerLayout drawerLayout;
    NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_achievements);

        nameLayout = findViewById(R.id.achievement_name);
        descriptionLayout = findViewById(R.id.achievement_description);
        navigationView = findViewById(R.id.navigationView);
        drawerLayout = findViewById(R.id.drawerLayout);

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize ActivityResultLauncher
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getClipData() != null) {
                            // Clear previous selections
                            selectedImageUris.clear();

                            // Multiple images selected
                            int count = data.getClipData().getItemCount();
                            if (count > MAX_IMAGES_TO_SELECT) {
                                Toast.makeText(getApplicationContext(), "You can select up to " + MAX_IMAGES_TO_SELECT + " images only.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            for (int i = 0; i < count; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                selectedImageUris.add(imageUri);
                                Log.d("ImageUri", String.valueOf(imageUri));
                            }
                        } else if (data != null && data.getData() != null) {
                            // Single image selected
                            selectedImageUris.clear();
                            selectedImageUris.add(data.getData());
                        }
                    }
                });

        findViewById(R.id.achievement_image_upload_btn).setOnClickListener(v -> selectMultipleImages());

        findViewById(R.id.addAchivementbtn).setOnClickListener(v -> {
            String name = nameLayout.getEditText().getText().toString();
            String description = descriptionLayout.getEditText().getText().toString();

            // Validate inputs
            if (name.isEmpty() || description.isEmpty() || selectedImageUris.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please select images and fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress dialog
            progressDialog = new ProgressDialog(AddAchievements.this);
            progressDialog.setMessage("Uploading images...");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMax(100); // We will update the progress in percentage
            progressDialog.show();

            // Upload images to Firebase Storage and store in Firestore
            uploadImagesAndData(name, description);
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                startActivity(new Intent(getApplicationContext(), Home.class));
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
    private void selectMultipleImages() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        launcher.launch(intent);
    }

    private void uploadImagesAndData(String name, String description) {
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(getApplicationContext(), "No images to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new document reference for the achievement
        DocumentReference newAchievementRef = db.collection("achievements").document();
        String achievementId = newAchievementRef.getId();

        // Prepare a document to store in Firestore
        Map<String, Object> achievementData = new HashMap<>();
        achievementData.put("id", achievementId); // Add the unique ID to the data
        achievementData.put("name", name);
        achievementData.put("description", description);
        List<String> imageUrls = new ArrayList<>();

        // Counter for successfully uploaded images
        int totalImages = selectedImageUris.size();
        AtomicInteger uploadedCount = new AtomicInteger();

        // Upload each selected image to Firebase Storage and store in Firestore
        for (Uri imageUri : selectedImageUris) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Compress the image
                byte[] data = baos.toByteArray();

                String imageName = "achievement_" + System.currentTimeMillis(); // Generate a unique name for each image

                // Storage reference
                StorageReference storageRef = storage.getReference().child("achievement_images/" + imageName);
                UploadTask uploadTask = storageRef.putBytes(data);

                // Track upload progress
                uploadTask.addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading image " + (uploadedCount.get() + 1) + " of " + totalImages + " (" + (int) progress + "%)");
                    progressDialog.setProgress((int) progress);
                });

                // Task completion listener for Firebase Storage upload
                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Image uploaded successfully, get the download URL
                        Uri downloadUri = task.getResult();
                        String imageUrl = downloadUri.toString();

                        // Add image URL to list
                        imageUrls.add(imageUrl);

                        // Update progress dialog
                        int progress = uploadedCount.incrementAndGet();
                        progressDialog.setProgress(progress * 100 / totalImages);

                        // Check if all images are uploaded successfully
                        if (uploadedCount.get() == totalImages) {
                            achievementData.put("imageUrls", imageUrls);

                            // Store the document in Firestore
                            newAchievementRef
                                    .set(achievementData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getApplicationContext(), "AchievementModel added successfully", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                        finish(); // Finish the activity or navigate to another screen
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getApplicationContext(), "Failed to add achievement", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    });
                        }
                    } else {
                        // Handle failures
                        Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Failed to compress image", Toast.LENGTH_SHORT).show();
            }
        }


    }
}
