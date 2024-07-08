package com.chinmay.diat;

public class FileModel {
    private String fileName;
    private String downloadUrl;
    private String documentId; // Add this field for document ID

    // Constructors, getters, and setters

    public FileModel() {
        // Default constructor required for Firestore
    }

    public FileModel(String fileName, String downloadUrl, String documentId) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.documentId = documentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
