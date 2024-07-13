package com.chinmay.diat;
public class FileModel {
    private String fileName;
    private String downloadUrl;
    private String documentId;
    private String departmentId;

    // Constructors, getters, and setters
    public FileModel() {
    }
    public FileModel(String fileName, String downloadUrl, String documentId, String departmentId) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.documentId = documentId;
        this.departmentId = departmentId;
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

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }
}
