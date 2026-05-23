package com.security.shield.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Device Admin Receiver for Shield Security
 * Handles Device Admin state changes and security events
 */
public class MyAdminReceiver extends DeviceAdminReceiver {

    private static final String TAG = "MyAdminReceiver";

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Log.i(TAG, "Shield Security Device Admin enabled");
        // Device Admin rights are now active
        // TODO: Start initial sync with Supabase
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Log.i(TAG, "Shield Security Device Admin disabled");
        // Device Admin rights have been revoked
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        super.onPasswordChanged(context, intent);
        Log.i(TAG, "Device password changed");
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        super.onPasswordFailed(context, intent);
        Log.i(TAG, "Password attempt failed");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        super.onPasswordSucceeded(context, intent);
        Log.i(TAG, "Password successful");
    }

    @Override
    public void onLockTaskModeEntering(Context context, Intent intent) {
        super.onLockTaskModeEntering(context, intent);
        Log.i(TAG, "Lock task mode entering");
    }

    @Override
    public void onLockTaskModeExiting(Context context, Intent intent) {
        super.onLockTaskModeExiting(context, intent);
        Log.i(TAG, "Lock task mode exiting");
    }
}