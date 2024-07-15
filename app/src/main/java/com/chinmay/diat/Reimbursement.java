package com.chinmay.diat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reimbursement extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_FILE_REQUEST = 2;
    private Uri fileUri;
    private TextView fileNameTextView;
    private EditText filenameedittext;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private ImageView more_options;
    private FloatingActionButton addfile;

    private RecyclerView recyclerView;
    private FilesAdapter filesAdapter;
    private List<FileModel> fileList;
    private EditText searchViewSearch; // Add this line
    TextView shortnametextview;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reimbursement);

        // Initialize Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Instances
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns in the grid
        fileList = new ArrayList<>();
        filesAdapter = new FilesAdapter(this, fileList);
        recyclerView.setAdapter(filesAdapter);
        shortnametextview = (TextView)findViewById(R.id.shortnametextview);
        addfile = findViewById(R.id.floatingbutton);
        searchViewSearch = findViewById(R.id.searchViewSearch);  // Initialize search input field

        // Fetch data from Firestore
        fetchDocumentsFromFirestore();
        addfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog();
            }
        });

        shortnametextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Profile.class));
            }
        });
        searchViewSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFiles(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // Fetch the username and display initials
        FirebaseUtils firebaseUtils = new FirebaseUtils();
        firebaseUtils.fetchAndDisplayInitials(shortnametextview);
    }

    private void fetchDocumentsFromFirestore() {
        db.collection("documents")
                .document("reimbursement")
                .collection("files")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }
                        fileList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            FileModel fileModel = document.toObject(FileModel.class);
                            fileList.add(fileModel);
                        }
                        filesAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void showCustomDialog() {
        // Create a new dialog
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.upload_file_dialog, null);

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
                String filename = filenameedittext.getText().toString().trim();
                if (fileUri == null || filename.isEmpty()) {
                    Toast.makeText(Reimbursement.this, "Please Enter All Fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Proceed with uploading the file and saving the data
                    uploadFileToFirebase(fileUri, filename);
                    dialog.dismiss();
                }
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "image/*",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation" // .pptx
        });
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    /*protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fileUri = data.getData();
        String fileName = getFileName(fileUri);
        fileNameTextView.setText(fileName);
        filesAdapter.handleActivityResult(requestCode, resultCode, data);
    }*/

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            String fileName = getFileName(fileUri);
            if (fileNameTextView != null) {
                fileNameTextView.setText(fileName);
            } else {
                Log.e("Administration", "fileNameTextView is null when handling result");
                // Handle this case based on your application logic
            }
            // Pass the result to FilesAdapter if needed
            filesAdapter.handleActivityResult(requestCode, resultCode, data);
        } else {
            Log.e("Administration", "Failed to handle file picker result");
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void uploadFileToFirebase(Uri fileUri, String filename) {
        if (fileUri != null) {
            StorageReference fileRef = storageReference.child("reimbursement/" + filename);
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
                                    saveFileLinkToFirestore(filename, downloadUrl);
                                    // Here you can save the download URL to Firestore or perform other operations
                                    Toast.makeText(Reimbursement.this, "File uploaded to storage", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Reimbursement.this, "Failed to upload file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                .document("reimbursement")
                .collection("files")
                .add(fileData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(Reimbursement.this, "File link saved to Firestore", Toast.LENGTH_SHORT).show();
                        String documentId = documentReference.getId();
                        // Optionally update the document to include the documentId
                        documentReference.update("documentId", documentId);
                        documentReference.update("departmentId", "reimbursement");
                        fetchDocumentsFromFirestore();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Reimbursement.this, "Failed to save file link: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterFiles(String query) {
        List<FileModel> filteredList = new ArrayList<>();
        for (FileModel file : fileList) {
            if (file.getFileName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(file);
            }
        }
        filesAdapter.filterList(filteredList);
    }
}
