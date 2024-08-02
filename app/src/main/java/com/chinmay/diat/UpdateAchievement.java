package com.chinmay.diat;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
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

public class UpdateAchievement extends AppCompatActivity {
    private static final int MAX_IMAGES_TO_SELECT = 3;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ActivityResultLauncher<Intent> launcher;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private boolean isLoggedIn = false;


    private TextInputLayout nameLayout, descriptionLayout;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private ProgressDialog progressDialog;
    private String achievementId;
    private List<String> oldImageUrls;
    NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.update_achievement);

        // For Network Connectivity Checking
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);

        // Instances
        navigationView = findViewById(R.id.navigationView);
        drawerLayout = findViewById(R.id.drawerLayout);
        nameLayout = findViewById(R.id.update_achievement_name);
        descriptionLayout = findViewById(R.id.update_achievement_description);

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Retrieve data from Intent
        achievementId = getIntent().getStringExtra("id");
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        oldImageUrls = getIntent().getStringArrayListExtra("imageUrls"); // Retrieve the list of image URLs

        nameLayout.getEditText().setText(name);
        descriptionLayout.getEditText().setText(description);

        // Set up the image selector
        findViewById(R.id.update_achievement_image_upload_btn).setOnClickListener(v -> selectMultipleImages());

        findViewById(R.id.updateAchivementbtn).setOnClickListener(v -> {
            String updateName = nameLayout.getEditText().getText().toString();
            String updateDescription = descriptionLayout.getEditText().getText().toString();

            // Validate inputs
            if (updateName.isEmpty() || updateDescription.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress dialog
            progressDialog = new ProgressDialog(UpdateAchievement.this);
            progressDialog.setMessage("Updating...");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            // Check if new images have been selected
            if (!selectedImageUris.isEmpty()) {
                updateDataWithImages(updateName, updateDescription);
            } else {
                updateData(updateName, updateDescription);
            }
        });

        // Initialize the launcher for image selection
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getClipData() != null) {
                    // Multiple images selected
                    int count = data.getClipData().getItemCount();
                    if (count > MAX_IMAGES_TO_SELECT) {
                        Toast.makeText(getApplicationContext(), "You can select up to " + MAX_IMAGES_TO_SELECT + " images only.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedImageUris.clear();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        selectedImageUris.add(imageUri);
                    }
                } else if (data != null && data.getData() != null) {
                    // Single image selected
                    selectedImageUris.clear();
                    selectedImageUris.add(data.getData());
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                finishAffinity();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.Achievements) {
                //startActivity(new Intent(getApplicationContext(), AllAchievements.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.logout) {
                drawerLayout.closeDrawer(GravityCompat.START);
                logoutUser(navigationView);
                return true;
            }
            return false;
        });

        checkIfUserIsLoggedIn();
    }

    private void checkIfUserIsLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            //Toast.makeText(getApplicationContext(),"Logged In",Toast.LENGTH_SHORT).show();
        } else {
            navigationView.getMenu().findItem(R.id.logout).setVisible(false);
            //Toast.makeText(getApplicationContext(),"Not Logged In",Toast.LENGTH_SHORT).show();
        }
    }

    private void selectMultipleImages() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        launcher.launch(intent);
    }

    private void updateData(String name, String description) {
        DocumentReference achievementRef = db.collection("achievements").document(achievementId);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", name);
        updateData.put("description", description);

        achievementRef.update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Achievement updated successfully", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish(); // Finish the activity or navigate to another screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to update achievement", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }

    private void updateDataWithImages(String name, String description) {
        // Show progress dialog
        progressDialog.setMessage("Uploading images...");
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();

        // Delete old images
        deleteOldImagesAndUploadNew(name, description);
    }

    private void deleteOldImagesAndUploadNew(String name, String description) {
        if (oldImageUrls != null && !oldImageUrls.isEmpty()) {
            AtomicInteger deleteCount = new AtomicInteger();
            AtomicInteger totalDeletes = new AtomicInteger(oldImageUrls.size());

            for (String oldImageUrl : oldImageUrls) {
                StorageReference oldImageRef = storage.getReferenceFromUrl(oldImageUrl);
                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                    Log.d("UpdateAchievement", "Old image deleted");
                    if (deleteCount.incrementAndGet() == totalDeletes.get()) {
                        // All old images deleted, start uploading new ones
                        uploadNewImages(name, description);
                    }
                }).addOnFailureListener(e -> {
                    Log.e("UpdateAchievement", "Failed to delete old image", e);
                    progressDialog.dismiss();
                });
            }
        } else {
            // No old images to delete, directly upload new ones
            uploadNewImages(name, description);
        }
    }

//    private void uploadNewImages(String name, String description) {
//        List<String> imageUrls = new ArrayList<>();
//        AtomicInteger uploadedCount = new AtomicInteger();
//
//        for (Uri imageUri : selectedImageUris) {
//            try {
//                InputStream inputStream = getContentResolver().openInputStream(imageUri);
//                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Compress the image
//                byte[] data = baos.toByteArray();
//
//                String imageName = "achievement_" + System.currentTimeMillis(); // Generate a unique name for each image
//                StorageReference storageRef = storage.getReference().child("achievement_images/" + imageName);
//                UploadTask uploadTask = storageRef.putBytes(data);
//
//                uploadTask.addOnProgressListener(snapshot -> {
//                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
//                    progressDialog.setMessage("Uploading image " + (uploadedCount.get() + 1) + " of " + selectedImageUris.size() + " (" + (int) progress + "%)");
//                    progressDialog.setProgress((int) progress);
//                });
//
//                uploadTask.continueWithTask(task -> {
//                    if (!task.isSuccessful()) {
//                        throw task.getException();
//                    }
//                    return storageRef.getDownloadUrl();
//                }).addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Uri downloadUri = task.getResult();
//                        String imageUrl = downloadUri.toString();
//                        imageUrls.add(imageUrl);
//
//                        if (uploadedCount.incrementAndGet() == selectedImageUris.size()) {
//                            // All images uploaded successfully, update Firestore
//                            updateFirestore(name, description, imageUrls);
//                        }
//                    } else {
//                        Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
//                        progressDialog.dismiss();
//                    }
//                });
//            } catch (IOException e) {
//                Log.e("UpdateAchievement", "Error reading image data", e);
//                Toast.makeText(getApplicationContext(), "Failed to read image data", Toast.LENGTH_SHORT).show();
//                progressDialog.dismiss();
//            }
//        }
//    }

    private void uploadNewImages(String name, String description) {
        List<String> imageUrls = new ArrayList<>();
        AtomicInteger uploadedCount = new AtomicInteger();

        // Initialize and show the progress dialog
        progressDialog.setMessage("Uploading images...");
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setProgress(0);
        progressDialog.show();

        // Track the total images being uploaded
        int totalImages = selectedImageUris.size();
        AtomicInteger uploadProgress = new AtomicInteger(0);

        for (Uri imageUri : selectedImageUris) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Compress the image
                byte[] data = baos.toByteArray();

                String imageName = "achievement_" + System.currentTimeMillis(); // Generate a unique name for each image
                StorageReference storageRef = storage.getReference().child("achievement_images/" + imageName);
                UploadTask uploadTask = storageRef.putBytes(data);

                uploadTask.addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    int currentProgress = uploadProgress.addAndGet((int) progress);
                    progressDialog.setMessage("Uploading image " + (uploadedCount.get() + 1) + " of " + totalImages + " (" + (int) currentProgress / totalImages + "%)");
                    progressDialog.setProgress((int) currentProgress / totalImages);
                });

                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String imageUrl = downloadUri.toString();
                        imageUrls.add(imageUrl);

                        if (uploadedCount.incrementAndGet() == totalImages) {
                            // All images uploaded successfully, update Firestore
                            updateFirestore(name, description, imageUrls);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            } catch (IOException e) {
                Log.e("UpdateAchievement", "Error reading image data", e);
                Toast.makeText(getApplicationContext(), "Failed to read image data", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }


    private void updateFirestore(String name, String description, List<String> imageUrls) {
        DocumentReference achievementRef = db.collection("achievements").document(achievementId);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", name);
        updateData.put("description", description);
        updateData.put("imageUrls", imageUrls);

        achievementRef.update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Achievement updated successfully", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish(); // Finish the activity or navigate to another screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to update achievement", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
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
}
