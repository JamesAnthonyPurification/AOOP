package com.backup.ui;

import com.backup.core.BackupConfig;
import com.backup.core.BackupManager;
import com.backup.core.BackupScheduler;
import com.backup.core.RestoreManager;

import java.io.*;
import java.util.Scanner;

public class ConsoleUI {
    private BackupConfig config;
    private BackupManager backupManager;
    private BackupScheduler scheduler;
    private RestoreManager restoreManager;
    private Scanner scanner;

    public ConsoleUI() {
        // Load config from file or create new
        this.config = loadConfig();
        this.backupManager = new BackupManager(config);
        this.scheduler = new BackupScheduler(backupManager);
        this.restoreManager = new RestoreManager();
        this.scanner = new Scanner(System.in);
        
        // Auto-start scheduler if config has it
        if (config.getScheduleIntervalMinutes() > 0) {
            scheduler.startSchedule(config.getScheduleIntervalMinutes());
        }
    }

    public void start() {
        while (true) {
            printMenu();
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    configureBackup();
                    break;
                case "2":
                    backupManager.performBackup();
                    break;
                case "3":
                    performRestore();
                    break;
                case "4":
                    configureSchedule();
                    break;
                case "5":
                    scheduler.stopSchedule();
                    System.out.println("Exiting...");
                    saveConfig();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== Automated Backup Tool ===");
        System.out.println("1. Configure Source/Destination");
        System.out.println("2. Run Backup Now");
        System.out.println("3. Restore Backup");
        System.out.println("4. Set Backup Schedule");
        System.out.println("5. Exit");
        System.out.println("============================");
    }

    private void configureBackup() {
        System.out.println("Current Source Dirs: " + config.getSourceDirectories());
        System.out.print("Add source directory (or press Enter to skip): ");
        String src = scanner.nextLine();
        if (!src.isEmpty()) {
            config.addSourceDirectory(src);
            System.out.println("Added.");
        }

        System.out.println("Current Destination: " + config.getDestinationDirectory());
        System.out.print("Set destination directory (or press Enter to skip): ");
        String dest = scanner.nextLine();
        if (!dest.isEmpty()) {
            config.setDestinationDirectory(dest);
            System.out.println("Set.");
        }
        
        System.out.print("Set Encryption Key (16 char) [Current: " + config.getEncryptionKey() + "]: ");
        String key = scanner.nextLine();
        if (!key.isEmpty()) {
            if (key.length() < 16) { 
                // Pad logic for user experience or just warn
                System.out.println("Key too short, padding will happen internally.");
            }
            config.setEncryptionKey(key);
        }
        
        saveConfig();
    }
    
    private void configureSchedule() {
        System.out.print("Enter interval in minutes (0 to disable): ");
        try {
            int mins = Integer.parseInt(scanner.nextLine());
            config.setScheduleIntervalMinutes(mins);
            if (mins > 0) {
                scheduler.startSchedule(mins);
            } else {
                scheduler.stopSchedule();
            }
            saveConfig();
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        }
    }

    private void performRestore() {
        System.out.print("Enter path to encrypted backup file: ");
        String path = scanner.nextLine();
        File encFile = new File(path);
        if (!encFile.exists()) {
            System.out.println("File not found.");
            return;
        }

        System.out.print("Enter restore destination directory: ");
        String destPath = scanner.nextLine();
        File destDir = new File(destPath);
        
        System.out.print("Enter decryption key: ");
        String key = scanner.nextLine();

        restoreManager.restore(encFile, destDir, key);
    }

    private BackupConfig loadConfig() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("backup_config.ser"))) {
            return (BackupConfig) ois.readObject();
        } catch (Exception e) {
            return new BackupConfig();
        }
    }

    private void saveConfig() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("backup_config.ser"))) {
            oos.writeObject(config);
        } catch (IOException e) {
            System.err.println("Could not save config: " + e.getMessage());
        }
    }
}
