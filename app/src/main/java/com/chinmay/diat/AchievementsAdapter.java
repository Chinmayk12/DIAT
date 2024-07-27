package com.chinmay.diat;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder> {
    private Context context;
    private List<AchievementModel> achievementsList;
    FirebaseFirestore db;
    FirebaseStorage storage;

    public AchievementsAdapter(Context context, List<AchievementModel> achievementsList) {
        this.context = context;
        this.achievementsList = achievementsList;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_achivement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        AchievementModel achievementModel = achievementsList.get(position);
        holder.achievementName.setText("Name:"+achievementModel.getName());
        //holder.achievementDescription.setText("Description:"+achievementModel.getDescription());
        // Create HTML-formatted string
        String descriptionHtml = "<b>Description:</b> " + achievementModel.getDescription();
        // Set HTML-formatted text to the TextView
        holder.achievementDescription.setText(Html.fromHtml(descriptionHtml, Html.FROM_HTML_MODE_LEGACY));
        // Display the first image
        if (achievementModel.getImageUrls() != null && !achievementModel.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(achievementModel.getImageUrls().get(0))
                    .into(holder.achievementImage);
        } else {
            holder.achievementImage.setImageResource(R.drawable.diat_logo); // Placeholder image
        }

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, Achievement.class);
                intent.putExtra("id", achievementModel.getId());
                intent.putExtra("name", achievementModel.getName());
                intent.putExtra("description", achievementModel.getDescription());
                intent.putStringArrayListExtra("imageUrls", new ArrayList<>(achievementModel.getImageUrls()));
                context.startActivity(intent);
            }
        });

        holder.moreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view); // Use 'context' here
                popupMenu.getMenuInflater().inflate(R.menu.achievement_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if (id == R.id.achievement_update) {
                            Toast.makeText(context,"Update",Toast.LENGTH_SHORT).show();
                            updateAchievement(achievementModel);

                        } else if (id == R.id.achievement_delete) {
                            showDeleteConfirmationDialog(achievementModel);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

    }

    private void updateAchievement(AchievementModel achievementModel) {
        Intent intent = new Intent(context, UpdateAchievement.class);
        intent.putExtra("id", achievementModel.getId());
        intent.putExtra("name", achievementModel.getName());
        intent.putExtra("description", achievementModel.getDescription());
        intent.putStringArrayListExtra("imageUrls", new ArrayList<>(achievementModel.getImageUrls()));
        context.startActivity(intent);
    }

    private void showDeleteConfirmationDialog(AchievementModel achievementModel) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Achievement")
                .setMessage("Are you sure you want to delete this achievement?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAchievement(achievementModel);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    private void deleteAchievement(AchievementModel achievementModel) {

        String achievementId = achievementModel.getId();
        List<String> imageUrls = achievementModel.getImageUrls();

        // Delete achievement document from Firestore
        db.collection("achievements").document(achievementId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Delete images from Firebase Storage
                    for (String imageUrl : imageUrls) {
                        StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
                        imageRef.delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    // Image deleted successfully
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                    Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
                                });
                    }
                    Toast.makeText(context, "Achievement deleted successfully", Toast.LENGTH_SHORT).show();
                    // Optionally refresh the adapter or navigate to a different screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete achievement", Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public int getItemCount() {
        return achievementsList.size();
    }

    public static class AchievementViewHolder extends RecyclerView.ViewHolder {

        TextView achievementName, achievementDescription;
        ImageView achievementImage;
        LinearLayout linearLayout;

        ImageButton moreOptions;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.item_section);
            achievementName = itemView.findViewById(R.id.achievement_name_textview);
            achievementDescription = itemView.findViewById(R.id.achievement_description_textview);
            achievementImage = itemView.findViewById(R.id.achievmen_img);
            moreOptions = itemView.findViewById(R.id.achievement_more_option);
        }
    }
}
