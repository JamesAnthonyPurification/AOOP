package com.backup.core;

import com.backup.utils.CryptoUtils;
import com.backup.utils.ZipUtils;

import java.io.File;

public class RestoreManager {
    
    public void restore(File encryptedBackup, File restoreDestination, String key) {
        System.out.println("Starting Restore Process...");
        File decryptedZip = new File("decrypted_restore.zip");
        
        try {
            // Decrypt
            System.out.println("Decrypting...");
            CryptoUtils.decrypt(encryptedBackup, decryptedZip, key);
            
            // Unzip
            System.out.println("Unzipping to " + restoreDestination.getAbsolutePath() + "...");
            ZipUtils.unzip(decryptedZip, restoreDestination);
            
            System.out.println("Restore completed successfully.");
            
        } catch (Exception e) {
            System.err.println("Restore failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (decryptedZip.exists()) {
                decryptedZip.delete();
            }
        }
    }
}
