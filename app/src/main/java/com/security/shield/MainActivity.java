package com.security.shield;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.security.shield.receivers.MyAdminReceiver;
import com.security.shield.services.ShieldCheckService;

/**
 * Main Activity for Shield Security Application
 * Manages Device Admin activation and displays security status
 */
public class MainActivity extends AppCompatActivity {

    private DevicePolicyManager devicePolicyManager;
    private ComponentName deviceAdminReceiver;
    private TextView statusTextView;
    private Button enableAdminButton;
    private Button lockButton;
    private Button startServiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Device Policy Manager
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        deviceAdminReceiver = new ComponentName(this, MyAdminReceiver.class);

        // Initialize UI components
        statusTextView = findViewById(R.id.statusTextView);
        enableAdminButton = findViewById(R.id.enableAdminButton);
        lockButton = findViewById(R.id.lockButton);
        startServiceButton = findViewById(R.id.startServiceButton);

        // Update initial status
        updateStatus();

        // Setup button listeners
        enableAdminButton.setOnClickListener(v -> requestDeviceAdminPermission());
        lockButton.setOnClickListener(v -> lockDevice());
        startServiceButton.setOnClickListener(v -> startShieldService());
    }

    /**
     * Request Device Admin permissions
     */
    private void requestDeviceAdminPermission() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminReceiver);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable Shield Security admin rights for device protection");
        startActivity(intent);
    }

    /**
     * Lock the device screen
     */
    private void lockDevice() {
        if (isDeviceAdminActive()) {
            devicePolicyManager.lockNow();
            Toast.makeText(this, "Device locked successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Device Admin is not enabled. Please enable it first.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Start Shield Check Service in background
     */
    private void startShieldService() {
        Intent serviceIntent = new Intent(this, ShieldCheckService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Toast.makeText(this, "Shield Service started", Toast.LENGTH_SHORT).show();
    }

    /**
     * Check if Device Admin is active
     */
    private boolean isDeviceAdminActive() {
        return devicePolicyManager.isAdminActive(deviceAdminReceiver);
    }

    /**
     * Update UI status based on admin state
     */
    private void updateStatus() {
        if (isDeviceAdminActive()) {
            statusTextView.setText(R.string.admin_enabled);
            statusTextView.setTextColor(getResources().getColor(R.color.primary_color));
            enableAdminButton.setEnabled(false);
            lockButton.setEnabled(true);
            startServiceButton.setEnabled(true);
        } else {
            statusTextView.setText(R.string.admin_disabled);
            statusTextView.setTextColor(getResources().getColor(R.color.error_color));
            enableAdminButton.setEnabled(true);
            lockButton.setEnabled(false);
            startServiceButton.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}