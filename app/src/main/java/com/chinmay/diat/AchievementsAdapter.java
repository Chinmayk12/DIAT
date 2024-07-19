package com.chinmay.diat;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder> {

    private Context context;
    private List<AchievementModel> achievementsList;

    public AchievementsAdapter(Context context, List<AchievementModel> achievementsList) {
        this.context = context;
        this.achievementsList = achievementsList;
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
        holder.achievementName.setText(achievementModel.getName());
        holder.achievementDescription.setText(Html.fromHtml(achievementModel.getDescription()));

        // Display the first image
        if (achievementModel.getImageUrls() != null && !achievementModel.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(achievementModel.getImageUrls().get(0))
                    .into(holder.achievementImage);
        } else {
            holder.achievementImage.setImageResource(R.drawable.diat_logo); // Placeholder image
        }
    }

    @Override
    public int getItemCount() {
        return achievementsList.size();
    }

    public static class AchievementViewHolder extends RecyclerView.ViewHolder {

        TextView achievementName, achievementDescription;
        ImageView achievementImage;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            achievementName = itemView.findViewById(R.id.achievement_name_textview);
            achievementDescription = itemView.findViewById(R.id.achievement_description_textview);
            achievementImage = itemView.findViewById(R.id.achievmen_img);
        }
    }
}
