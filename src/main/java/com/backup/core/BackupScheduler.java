package com.backup.core;

import java.util.Timer;
import java.util.TimerTask;

public class BackupScheduler {
    private Timer timer;
    private BackupManager backupManager;

    public BackupScheduler(BackupManager backupManager) {
        this.backupManager = backupManager;
    }

    public void startSchedule(int minutes) {
        stopSchedule();
        if (minutes <= 0) return;

        timer = new Timer(true); // Daemon thread
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("\n[Scheduler] Triggering scheduled backup...");
                backupManager.performBackup();
                System.out.println("[Scheduler] Backup task finished.");
            }
        }, 0, minutes * 60 * 1000L);
        System.out.println("Scheduler started. Backup every " + minutes + " minutes.");
    }

    public void stopSchedule() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            System.out.println("Scheduler stopped.");
        }
    }
}
