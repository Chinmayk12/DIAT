package com.chinmay.diat;

public class DepartmentModel {
    private String name;
    private int iconResId;

    public DepartmentModel(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }
}

