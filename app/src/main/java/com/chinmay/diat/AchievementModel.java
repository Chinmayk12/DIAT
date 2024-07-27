package com.chinmay.diat;
import java.util.List;
public class AchievementModel {

    private String id;
    private String name;
    private String description;
    private List<String> imageUrls;

    public AchievementModel() {
        // Default constructor required for calls to DataSnapshot.getValue(AchievementModel.class)
    }

    public AchievementModel(String id, String name, String description, List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrls = imageUrls;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description =description;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
