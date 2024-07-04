package com.chinmay.diat;
import android.annotation.SuppressLint;
import androidx.documentfile.provider.DocumentFile;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class Administration extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private TextView fileNameTextView;
    EditText filenameedittext;
    private FirebaseStorage storage;
    FirebaseFirestore db;
    private StorageReference storageReference;

    ImageView more_options;
    FloatingActionButton addfile;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.administration);

        // Initialize Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Instances
        more_options = findViewById(R.id.more_option);
        addfile = findViewById(R.id.floatingbutton);
        more_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        addfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog();
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.folder_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();

                if (id == R.id.menu_update) {
                    Toast.makeText(view.getContext(), "Update", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.menu_delete) {
                    Toast.makeText(view.getContext(), "Delete", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

        });

        popupMenu.show();
    }

    private void showCustomDialog() {
        // Create a new dialog
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.add_dept_dialog, null);

        // Set the custom layout as the dialog's content
        dialog.setContentView(dialogLayout);

        // Get references to the views in the custom dialog layout
        filenameedittext = dialogLayout.findViewById(R.id.dialogDeptEditText);
        Button uploadFileButton = dialogLayout.findViewById(R.id.uploadFileButton);
        Button saveButton = dialogLayout.findViewById(R.id.addDeptButton);  // Add this line to get reference to the Save button
        fileNameTextView = dialogLayout.findViewById(R.id.fileNameTextView);

        // Set up the upload button click listener
        uploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Set up the save button click listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri == null || fileNameTextView.getText().toString().trim().isEmpty()) {
                    Toast.makeText(Administration.this, "Please Enter All Fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Proceed with uploading the file and saving the data
                    uploadFileToFirebase(imageUri);
                    dialog.dismiss();
                }
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            String fileName = getFileName(imageUri);
            fileNameTextView.setText(fileName);
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String filename = filenameedittext.getText().toString().trim();
        if (filename != null) {
            return filename;
        } else {
            return "Name Error";
        }
    }


    private void uploadFileToFirebase(Uri fileUri) {
        if (fileUri != null) {
            StorageReference fileRef = storageReference.child("administration/" + getFileName(fileUri));
            fileRef.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get the download URL from the task snapshot
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // You can use uri.toString() to access the file URL
                                    String downloadUrl = uri.toString();
                                    saveFileLinkToFirestore(getFileName(fileUri), downloadUrl);
                                    // Here you can save the download URL to Firestore or perform other operations
                                    Toast.makeText(Administration.this, "File uploaded to storage", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Administration.this, "Failed to upload file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveFileLinkToFirestore(String fileName, String downloadUrl) {
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("fileName", fileName);
        fileData.put("downloadUrl", downloadUrl);

        // Add the file link to the "files" subcollection under "administration"
        db.collection("documents")
                .document("administration")
                .collection("files")
                .add(fileData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(Administration.this, "File link saved to Firestore", Toast.LENGTH_SHORT).show();
                        // Optionally fetch or update UI after successful save
                        //fetchDocumentsFromFirestore();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Administration.this, "Failed to save file link: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
