package com.security.shield.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Supabase Client for Shield Security
 * Handles secure communication with Supabase backend
 */
public class SupabaseClient {

    private static final String TAG = "SupabaseClient";
    private static SupabaseClient instance;

    private String supabaseUrl;
    private String supabaseKey;
    private OkHttpClient httpClient;
    private Gson gson;

    private SupabaseClient() {
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
        // URLs and keys are injected via GitHub Secrets
        this.supabaseUrl = System.getenv("SUPABASE_URL");
        this.supabaseKey = System.getenv("SUPABASE_KEY");

        if (this.supabaseUrl == null || this.supabaseKey == null) {
            Log.w(TAG, "Supabase credentials not configured via environment variables");
        }
    }

    /**
     * Get singleton instance
     */
    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    /**
     * Check for lock commands from Supabase
     */
    public boolean checkForLockCommand() {
        try {
            if (supabaseUrl == null || supabaseKey == null) {
                Log.w(TAG, "Supabase not configured, skipping check");
                return false;
            }

            String endpoint = supabaseUrl + "/rest/v1/commands?status=eq.pending&limit=1";

            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer " + supabaseKey)
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Response: " + responseBody);

                    // Parse response and check for lock command
                    return responseBody.contains("lock");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking for lock command", e);
        }
        return false;
    }

    /**
     * Update command status in Supabase
     */
    public void updateCommandStatus(String status) {
        try {
            if (supabaseUrl == null || supabaseKey == null) {
                Log.w(TAG, "Supabase not configured, skipping update");
                return;
            }

            String endpoint = supabaseUrl + "/rest/v1/commands?status=eq.pending";

            JsonObject updateData = new JsonObject();
            updateData.addProperty("status", status);

            RequestBody body = RequestBody.create(
                    gson.toJson(updateData),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer " + supabaseKey)
                    .addHeader("Content-Type", "application/json")
                    .patch(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Command status updated to: " + status);
                } else {
                    Log.e(TAG, "Failed to update command status: " + response.code());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating command status", e);
        }
    }

    /**
     * Set Supabase credentials (called from BuildConfig or BuildVariants)
     */
    public void setCredentials(String url, String key) {
        this.supabaseUrl = url;
        this.supabaseKey = key;
        Log.d(TAG, "Supabase credentials set");
    }
}