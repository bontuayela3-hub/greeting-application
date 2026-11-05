
package com.example.bonycallgreetings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS = "BonySettings";
    private static final String ENABLED = "greeting_enabled";

    private TextView statusText;
    private Button toggleButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        toggleButton = findViewById(R.id.toggleButton);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        toggleButton.setOnClickListener(v -> {
            if (!hasPhonePermission()) {
                requestPhonePermissions();
                return;
            }

            boolean enabled = prefs.getBoolean(ENABLED, true);
            prefs.edit().putBoolean(ENABLED, !enabled).apply();

            updateScreen();
        });

        if (!hasPhonePermission()) {
            requestPhonePermissions();
        } else {
            updateScreen();
        }
    }

    private boolean hasPhonePermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPhonePermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.READ_PHONE_STATE
                },
                PERMISSION_REQUEST_CODE
        );
    }

    private void updateScreen() {
        if (!hasPhonePermission()) {
            statusText.setText("Permission required");
            toggleButton.setText("Grant Phone Permission");
            return;
        }

        boolean enabled = prefs.getBoolean(ENABLED, true);

        if (enabled) {
            statusText.setText("● Greeting is ENABLED");
            toggleButton.setText("Disable Greeting");
        } else {
            statusText.setText("● Greeting is DISABLED");
            toggleButton.setText("Enable Greeting");
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (hasPhonePermission()) {
                Toast.makeText(
                        this,
                        "Phone permission granted!",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        this,
                        "Phone permission is required.",
                        Toast.LENGTH_LONG
                ).show();
            }

            updateScreen();
        }
    }
}


