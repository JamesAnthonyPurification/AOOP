package com.backup.core;

import com.backup.utils.CryptoUtils;
import com.backup.utils.ZipUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class BackupManager {
    private static final String METADATA_FILE = "backup_metadata.ser";
    private BackupConfig config;
    private Map<String, Long> lastBackupMetadata;

    public BackupManager(BackupConfig config) {
        this.config = config;
        this.lastBackupMetadata = loadMetadata();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> loadMetadata() {
        File file = new File(config.getDestinationDirectory(), METADATA_FILE);
        if (!file.exists()) return new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<String, Long>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private void saveMetadata() {
        File file = new File(config.getDestinationDirectory(), METADATA_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(lastBackupMetadata);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void performBackup() {
        System.out.println("Starting Backup Process...");
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File tempDir = new File("temp_backup_" + timestamp);
        if (!tempDir.exists()) tempDir.mkdirs();

        boolean filesChanged = false;

        try {
            for (String sourcePath : config.getSourceDirectories()) {
                File sourceDir = new File(sourcePath);
                if (sourceDir.exists() && sourceDir.isDirectory()) {
                    filesChanged |= copyChangedFiles(sourceDir, tempDir, sourceDir.getAbsolutePath());
                }
            }

            if (filesChanged) {
                // Zip
                File zipFile = new File("backup_" + timestamp + ".zip");
                ZipUtils.zipDirectory(tempDir, zipFile);

                // Encrypt
                File encryptedFile = new File(config.getDestinationDirectory(), "backup_" + timestamp + ".enc");
                CryptoUtils.encrypt(zipFile, encryptedFile, config.getEncryptionKey());

                System.out.println("Backup completed: " + encryptedFile.getAbsolutePath());
                
                // Cleanup
                zipFile.delete();
                saveMetadata();
            } else {
                System.out.println("No changes detected. Skipping backup.");
            }

        } catch (Exception e) {
            System.err.println("Backup failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            deleteDirectory(tempDir);
        }
    }

    private boolean copyChangedFiles(File source, File dest, String rootPath) throws IOException {
        boolean changed = false;
        File[] files = source.listFiles();
        if (files == null) return false;

        for (File file : files) {
            if (file.isDirectory()) {
                changed |= copyChangedFiles(file, dest, rootPath);
            } else {
                String relativePath = file.getAbsolutePath().substring(rootPath.length());
                // Key for metadata: relative path from source root
                // To handle multiple source roots, we might need a better key, but for academic scope this is usually enough or we prepend root hash.
                // Let's use absolute path for simplicity in metadata key to be safe across multiple roots.
                String uniqueKey = file.getAbsolutePath();
                
                long lastModified = file.lastModified();
                if (!lastBackupMetadata.containsKey(uniqueKey) || lastBackupMetadata.get(uniqueKey) < lastModified) {
                    // Copy to temp dir structure
                    // relativePath might start with slash, remove it for file constr
                    if (relativePath.startsWith(File.separator)) relativePath = relativePath.substring(1);
                    
                    File destFile = new File(dest, relativePath);
                    destFile.getParentFile().mkdirs();
                    Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    lastBackupMetadata.put(uniqueKey, lastModified);
                    changed = true;
                }
            }
        }
        return changed;
    }
    
    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
