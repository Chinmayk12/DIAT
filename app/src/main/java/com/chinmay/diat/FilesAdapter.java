package com.chinmay.diat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    private FirebaseStorage storage;
    private Context context;
    private List<FileModel> fileList;
    private FirebaseFirestore db;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_FILE_REQUEST = 2;
    private Uri fileUri;
    private TextView fileNameTextView;
    private EditText filenameedittext;
    private FileModel currentFileModel;

    public FilesAdapter(Context context, List<FileModel> fileList) {
        this.context = context;
        this.fileList = fileList;
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_document, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileModel fileModel = fileList.get(position);
        holder.fileName.setText(fileModel.getFileName());
        holder.folder.setImageResource(R.drawable.folder_icon);
        // Set click listener for folder icon to download the file
        holder.folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previewFile(fileModel.getDownloadUrl());
            }
        });

        // Set click listener for more options button to show the popup menu
        holder.moreOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.moreOption, fileModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public void filterList(List<FileModel> filteredList) {
        fileList = filteredList;
        notifyDataSetChanged();
    }

    private void previewFile(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }

    private void showPopupMenu(View view, FileModel fileModel) {
        PopupMenu popupMenu = new PopupMenu(context, view); // Use 'context' here
        popupMenu.getMenuInflater().inflate(R.menu.folder_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.menu_download) {
                    downloadFile(fileModel.getFileName(), fileModel.getDownloadUrl());
                } else if (id == R.id.menu_update) {
                    showUpdateDialog(view, fileModel);
                } else if (id == R.id.menu_delete) {
                    showDeleteConfirmationDialog(view, fileModel);
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void showUpdateDialog(View view, FileModel fileModel) {
        currentFileModel = fileModel;
        // Create a new dialog
        Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogLayout = inflater.inflate(R.layout.update_file_dialog, null);

        // Set the custom layout as the dialog's content
        dialog.setContentView(dialogLayout);

        // Get references to the views in the custom dialog layout
        filenameedittext = dialogLayout.findViewById(R.id.dialogDeptEditText);
        Button uploadFileButton = dialogLayout.findViewById(R.id.uploadFileButton);
        Button saveButton = dialogLayout.findViewById(R.id.addDeptButton);
        fileNameTextView = dialogLayout.findViewById(R.id.fileNameTextView);

        // Set the current file name in the EditText
        filenameedittext.setText(fileModel.getFileName());

        // Set up the upload button click listener
        uploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Set up the save button click listener
        saveButton.setOnClickListener(v -> {
            String filename = filenameedittext.getText().toString().trim();
            if (filename.isEmpty()) {
                Toast.makeText(context, "Please Enter All Fields", Toast.LENGTH_SHORT).show();
            } else {
                if (fileUri == null) {
                    // Update only the file name
                    updateFileNameInFirebase(fileModel.getDepartmentId(), fileModel.getDocumentId(), filename);
                } else {
                    // Proceed with updating the file and saving the data
                    updateFileInFirebase(fileUri, fileModel.getDepartmentId(), fileModel.getDocumentId(), filename);
                }
                dialog.dismiss();
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
        ((Activity) context).startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            String fileName = getFileName(fileUri);
            if (fileNameTextView != null) {
                fileNameTextView.setText(fileName);
            } else {
                Log.e("FilesAdapter", "fileNameTextView is null");
            }
        }
    }


    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null); // Use 'context' here
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void updateFileInFirebase(@Nullable Uri fileUri, String departmentId, String documentId, String newFileName) {
        if (fileUri != null && documentId != null && !newFileName.isEmpty()) {
            StorageReference oldFileRef = storage.getReferenceFromUrl(currentFileModel.getDownloadUrl());
            oldFileRef.delete().addOnSuccessListener(aVoid -> {
                StorageReference newFileRef = storage.getReference().child(departmentId + "/" + newFileName);
                newFileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot ->
                        newFileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            Map<String, Object> fileData = new HashMap<>();
                            fileData.put("fileName", newFileName);
                            fileData.put("downloadUrl", downloadUrl);
                            db.collection("documents")
                                    .document(departmentId)
                                    .collection("files")
                                    .document(documentId)
                                    .update(fileData)
                                    .addOnSuccessListener(aVoid1 -> {
                                        Toast.makeText(context, "File updated successfully", Toast.LENGTH_SHORT).show();
                                        int position = fileList.indexOf(currentFileModel);
                                        currentFileModel.setFileName(newFileName);
                                        currentFileModel.setDownloadUrl(downloadUrl);
                                        notifyItemChanged(position);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update file in Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                ).addOnFailureListener(e -> Toast.makeText(context, "Failed to upload new file: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(context, "Failed to delete old file: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updateFileNameInFirebase(String departmentId, String documentId, String newFileName) {
        if (documentId != null && !newFileName.isEmpty()) {
            StorageReference oldFileRef = storage.getReferenceFromUrl(currentFileModel.getDownloadUrl());
            StorageReference newFileRef = storage.getReference().child(departmentId + "/" + newFileName);

            oldFileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                newFileRef.putBytes(bytes)
                        .addOnSuccessListener(taskSnapshot ->
                                newFileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String newDownloadUrl = uri.toString();
                                    Map<String, Object> fileData = new HashMap<>();
                                    fileData.put("fileName", newFileName);
                                    fileData.put("downloadUrl", newDownloadUrl);
                                    db.collection("documents")
                                            .document(departmentId)
                                            .collection("files")
                                            .document(documentId)
                                            .update(fileData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(context, "Filename updated successfully", Toast.LENGTH_SHORT).show();
                                                int position = fileList.indexOf(currentFileModel);
                                                currentFileModel.setFileName(newFileName);
                                                currentFileModel.setDownloadUrl(newDownloadUrl);
                                                notifyItemChanged(position);
                                                oldFileRef.delete();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to update filename in Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                }))
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to upload file with new name: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(context, "Failed to read old file: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }


    private void showDeleteConfirmationDialog(View view, FileModel fileModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this file?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteFile(view, fileModel);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private void downloadFile(String fileName, String downloadUrl) {
        // Extract the file extension from the downloadUrl
        String fileExtension = getFileExtension(downloadUrl);
        // Append the extension to the fileName
        String fileNameWithExtension = fileName + "." + fileExtension;

        StorageReference storageRef = storage.getReferenceFromUrl(downloadUrl);

        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsFolder.exists()) {
            if (!downloadsFolder.mkdirs()) {
                Log.e("Download Error", "Failed to create downloads directory");
                Toast.makeText(context, "Failed to create downloads directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        File localFile = new File(downloadsFolder, fileNameWithExtension);

        try {
            if (!localFile.createNewFile()) {
                Log.e("Download Error", "Failed to create file: " + localFile.getAbsolutePath());
                Toast.makeText(context, "Failed to create file", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IOException e) {
            Log.e("Download Error", "IOException: " + e.getMessage(), e);
            Toast.makeText(context, "IOException: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(context, "File downloaded to " + localFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                Log.d("File Path", localFile.getAbsolutePath());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Download failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Download failed:", exception.getMessage());
            }
        });
    }

    private String getFileExtension(String url) {
        return url.substring(url.lastIndexOf(".") + 1);
    }

    private void deleteFile(View view, FileModel fileModel) {
        if (fileModel.getDocumentId() != null) {
            db.collection("documents")
                    .document("administration")
                    .collection("files")
                    .document(fileModel.getDocumentId()) // Ensure documentId is not null
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "File deleted from Firestore", Toast.LENGTH_SHORT).show();
                            // Delete from Storage after Firestore delete is successful
                            deleteFileFromStorage(view, fileModel);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to delete file from Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(context, "Document ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteFileFromStorage(View view, FileModel fileModel) {
        StorageReference storageRef = storage.getReferenceFromUrl(fileModel.getDownloadUrl());

        // Delete the file from Storage
        storageRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "File deleted from Storage", Toast.LENGTH_SHORT).show();
                        // Optionally remove the item from your fileList and notify adapter
                        fileList.remove(fileModel);
                        notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to delete file from Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView folder;
        TextView fileName;
        ImageButton moreOption;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            folder = itemView.findViewById(R.id.folder);
            fileName = itemView.findViewById(R.id.fileName);
            moreOption = itemView.findViewById(R.id.more_option);
        }
    }
}