package com.backup.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BackupConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> sourceDirectories;
    private String destinationDirectory;
    private int scheduleIntervalMinutes;
    private String encryptionKey;

    public BackupConfig() {
        this.sourceDirectories = new ArrayList<>();
        this.scheduleIntervalMinutes = 0; // 0 means manual only
        this.encryptionKey = "DefaultSecretKey"; // 16 chars ideally, padded by CryptoUtils
    }

    public List<String> getSourceDirectories() {
        return sourceDirectories;
    }

    public void setSourceDirectories(List<String> sourceDirectories) {
        this.sourceDirectories = sourceDirectories;
    }

    public void addSourceDirectory(String path) {
        if (!this.sourceDirectories.contains(path)) {
            this.sourceDirectories.add(path);
        }
    }

    public String getDestinationDirectory() {
        return destinationDirectory;
    }

    public void setDestinationDirectory(String destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    public int getScheduleIntervalMinutes() {
        return scheduleIntervalMinutes;
    }

    public void setScheduleIntervalMinutes(int scheduleIntervalMinutes) {
        this.scheduleIntervalMinutes = scheduleIntervalMinutes;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
}
