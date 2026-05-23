package com.security.shield.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.security.shield.MainActivity;
import com.security.shield.R;
import com.security.shield.api.SupabaseClient;
import com.security.shield.receivers.MyAdminReceiver;

/**
 * Shield Check Service
 * Background service that queries Supabase for lock commands
 * and executes device admin operations
 */
public class ShieldCheckService extends Service {

    private static final String TAG = "ShieldCheckService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "shield_security_channel";
    private static final long POLL_INTERVAL = 5000; // 5 seconds

    private DevicePolicyManager devicePolicyManager;
    private ComponentName deviceAdminReceiver;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private Runnable pollRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ShieldCheckService created");

        // Initialize Device Policy Manager
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        deviceAdminReceiver = new ComponentName(this, MyAdminReceiver.class);

        // Create background thread for polling
        backgroundThread = new HandlerThread("ShieldPollThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        // Create notification channel for Android O+
        createNotificationChannel();

        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand - Service started");

        // Start polling for Supabase commands
        startPolling();

        return START_STICKY;
    }

    /**
     * Start polling Supabase for lock commands
     */
    private void startPolling() {
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // Query Supabase for pending commands
                    querySupabaseForCommands();
                } catch (Exception e) {
                    Log.e(TAG, "Error polling Supabase", e);
                }

                // Schedule next poll
                backgroundHandler.postDelayed(this, POLL_INTERVAL);
            }
        };

        // Start polling
        backgroundHandler.post(pollRunnable);
    }

    /**
     * Query Supabase for lock commands
     */
    private void querySupabaseForCommands() {
        Log.d(TAG, "Querying Supabase for commands");

        try {
            // Get Supabase client
            SupabaseClient supabaseClient = SupabaseClient.getInstance();

            // Check for pending commands
            boolean shouldLock = supabaseClient.checkForLockCommand();

            if (shouldLock && isDeviceAdminActive()) {
                Log.i(TAG, "Lock command received from Supabase, locking device");
                devicePolicyManager.lockNow();

                // Update command status in Supabase
                supabaseClient.updateCommandStatus("executed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying Supabase", e);
        }
    }

    /**
     * Check if Device Admin is active
     */
    private boolean isDeviceAdminActive() {
        return devicePolicyManager.isAdminActive(deviceAdminReceiver);
    }

    /**
     * Create foreground notification
     */
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Shield Security")
                .setContentText(getString(R.string.service_running))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    /**
     * Create notification channel for Android O+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Shield Security Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for Shield Security background service");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ShieldCheckService destroyed");

        if (pollRunnable != null) {
            backgroundHandler.removeCallbacks(pollRunnable);
        }

        if (backgroundThread != null) {
            backgroundThread.quit();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}