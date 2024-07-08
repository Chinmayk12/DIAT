package com.chinmay.diat;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    private FirebaseStorage storage;
    private Context context;
    private List<FileModel> fileList;

    private FirebaseFirestore db;

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
    private void previewFile(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }

    private void showPopupMenu(View view, FileModel fileModel) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.folder_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id==R.id.menu_download)
                {
                    downloadFile(fileModel.getFileName(), fileModel.getDownloadUrl());
                }
                else if (id == R.id.menu_update) {
                    Toast.makeText(view.getContext(), "Update " + fileModel.getFileName(), Toast.LENGTH_SHORT).show();
                } else if (id == R.id.menu_delete) {
                    //Toast.makeText(view.getContext(), "Delete " + fileModel.getFileName(), Toast.LENGTH_SHORT).show();
                    deleteFile(view,fileModel);
                }
                return true;
            }
        });

        popupMenu.show();
    }

    private void downloadFile(String fileName, String downloadUrl) {
        StorageReference storageRef = storage.getReferenceFromUrl(downloadUrl);

        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsFolder.exists()) {
            downloadsFolder.mkdirs();
        }
        File localFile = new File(downloadsFolder, fileName);

        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(context, "File downloaded to " + localFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                Log.d("File Path",localFile.getAbsolutePath());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Download failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Download failed:",exception.getMessage());
            }
        });
    }

    private void deleteFile(View view,FileModel fileModel) {
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
                            deleteFileFromStorage(view,fileModel);
                            Toast.makeText(view.getContext(), "Deleted from firebase " + fileModel.getFileName(), Toast.LENGTH_SHORT).show();
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


    private void deleteFileFromStorage(View view,FileModel fileModel) {
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
                        Toast.makeText(view.getContext(), "Deleted from storage " + fileModel.getFileName(), Toast.LENGTH_SHORT).show();
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
