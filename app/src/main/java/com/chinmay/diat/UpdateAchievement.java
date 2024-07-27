package com.chinmay.diat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.progressindicator.CircularProgressIndicator;
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
import java.util.Map;

public class UpdateAchievement extends AppCompatActivity {

    private static final int MAX_IMAGES_TO_SELECT = 1; // Assuming single image for update
    private Uri selectedImageUri;

    private TextInputLayout nameLayout, descriptionLayout;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;
    private CircularProgressIndicator progressCircular;

    private ActivityResultLauncher<Intent> launcher;
    private String achievementId;
    private ArrayList<String> oldImageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.update_achievement);

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        nameLayout = findViewById(R.id.update_achievement_name);
        descriptionLayout = findViewById(R.id.update_achievement_description);
        progressCircular = findViewById(R.id.progress_circular);

        // Retrieve data from Intent
        achievementId = getIntent().getStringExtra("id");
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        oldImageUrls = getIntent().getStringArrayListExtra("imageUrls");

        nameLayout.getEditText().setText(name);
        descriptionLayout.getEditText().setText(description);

        // Initialize ActivityResultLauncher
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectedImageUri = data.getData();
                            Log.d("ImageUri", String.valueOf(selectedImageUri));
                        }
                    }
                });

        findViewById(R.id.update_achievement_image_upload_btn).setOnClickListener(v -> selectImage());

        findViewById(R.id.updateAchivementbtn).setOnClickListener(v -> {
            String update_name = nameLayout.getEditText().getText().toString();
            String update_description = descriptionLayout.getEditText().getText().toString();

            // Validate inputs
            if (update_name.isEmpty() || update_description.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress dialog
            progressDialog = new ProgressDialog(UpdateAchievement.this);
            progressDialog.setMessage("Updating...");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            // Upload image and update data
            if (selectedImageUri != null) {
                uploadImageAndUpdateData(name, description);
            } else {
                updateData(name, description, null);
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        launcher.launch(intent);
    }

    private void uploadImageAndUpdateData(String name, String description) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            String imageName = "updated_achievement_" + System.currentTimeMillis();

            StorageReference storageRef = storage.getReference().child("achievement_images/" + imageName);
            UploadTask uploadTask = storageRef.putBytes(data);

            uploadTask.addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                progressCircular.setProgress((int) progress);
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
                    deleteOldImagesAndUpdateData(name, description, imageUrl);
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Failed to compress image", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private void deleteOldImagesAndUpdateData(String name, String description, String newImageUrl) {
        // Create a reference to the Firebase Firestore document
        DocumentReference achievementRef = db.collection("achievements").document(achievementId);

        // Remove old image URLs from Firebase Storage
        if (oldImageUrls != null && !oldImageUrls.isEmpty()) {
            for (String oldImageUrl : oldImageUrls) {
                StorageReference oldImageRef = storage.getReferenceFromUrl(oldImageUrl);
                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                    // Log success for each deletion
                    Log.d("DeleteImage", "Successfully deleted: " + oldImageUrl);
                }).addOnFailureListener(e -> {
                    // Log failure for each deletion
                    Log.e("DeleteImage", "Failed to delete: " + oldImageUrl, e);
                });
            }
        }

        // Update the Firestore document with the new data
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", name);
        updateData.put("description", description);
        if (newImageUrl != null) {
            updateData.put("imageUrl", newImageUrl);
        }

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

    private void updateData(String name, String description, String imageUrl) {
        DocumentReference achievementRef = db.collection("achievements").document(achievementId);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", name);
        updateData.put("description", description);
        if (imageUrl != null) {
            updateData.put("imageUrl", imageUrl);
        }

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
}
