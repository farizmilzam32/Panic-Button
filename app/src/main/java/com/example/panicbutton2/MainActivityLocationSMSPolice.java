package com.example.panicbutton2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.MessageFormat;
import java.util.Locale;


public class MainActivityLocationSMSPolice extends AppCompatActivity {

    private final static int PERMISSION_REQUEST = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button gpsButton;
    private TextView progressTitle;
    private ProgressBar progressBar;
    private TextView detailsText;

    private Button shareButton;


    private LocationManager locManager;
    private Location lastLocation;

    private final LocationListener locListener = new LocationListener() {
        public void onLocationChanged(Location loc) {
            updateLocation(loc);
        }

        public void onProviderEnabled(String provider) {
            updateLocation();
        }

        public void onProviderDisabled(String provider) {
            updateLocation();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    // ----------------------------------------------------
    // Android Lifecycle
    // ----------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_location_sms);
        setTitle(R.string.app_name);

        // Display area
        gpsButton = findViewById(R.id.gpsButton);
        progressTitle = findViewById(R.id.progressTitle);
        progressBar = findViewById(R.id.progressBar);
        detailsText = findViewById(R.id.detailsText);

        // Button area
        shareButton = findViewById(R.id.shareButton);


        // Set default values for preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            locManager.removeUpdates(locListener);
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to stop listening for location updates", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startRequestingLocation();
        updateLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST &&
                grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRequestingLocation();
        } else {
            Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ----------------------------------------------------
    // UI
    // ----------------------------------------------------
    private void updateLocation() {
        // Trigger a UI update without changing the location
        updateLocation(lastLocation);
    }

    private void updateLocation(Location location) {
        boolean locationEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean waitingForLocation = locationEnabled && !validLocation(location);
        boolean haveLocation = locationEnabled && !waitingForLocation;

        // Update display area
        gpsButton.setVisibility(locationEnabled ? View.GONE : View.VISIBLE);
        progressTitle.setVisibility(waitingForLocation ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(waitingForLocation ? View.VISIBLE : View.GONE);
        detailsText.setVisibility(haveLocation ? View.VISIBLE : View.GONE);

        // Update buttons
        shareButton.setEnabled(haveLocation);


        if (haveLocation) {
            String newline = System.getProperty("line.separator");
            detailsText.setText(String.format("%s: %s%s%s: %s (%s)%s%s: %s (%s)",
                    getString(R.string.accuracy), getAccuracy(location), newline,
                    getString(R.string.latitude), getLatitude(location), getDMSLatitude(location), newline,
                    getString(R.string.longitude), getLongitude(location), getDMSLongitude(location)));

            lastLocation = location;
        }
    }



    // ----------------------------------------------------
    // Actions send sms (You can change it by change "00000000")
    // ----------------------------------------------------
    public void shareLocation(View view) {
        if (!validLocation(lastLocation)) {
            return;
        }

        Log.i("Send SMS", "");
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);

        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address"  , ("00000000"));
        smsIntent.putExtra("sms_body"  , "Tolong dicek daerah ini" +"\n"+"https://www.google.com/maps/place/"+lastLocation.getLatitude()+","+lastLocation.getLongitude());


        try {
            startActivity(smsIntent);
            finish();
            Log.i("Finished sending SMS...", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivityLocationSMSPolice.this,
                    "SMS failed, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }


    public void openLocationSettings(View view) {
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    // ----------------------------------------------------
    // Helper functions
    // ----------------------------------------------------

    private void startRequestingLocation() {
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST);
            return;
        }

        // GPS enabled and have permission - start requesting location updates
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
    }

    private boolean validLocation(Location location) {
        if (location == null) {
            return false;
        }

        // Location must be from less than 30 seconds ago to be considered valid
        if (Build.VERSION.SDK_INT < 17) {
            return System.currentTimeMillis() - location.getTime() < 30e3;
        } else {
            return SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos() < 30e9;
        }
    }

    private String getAccuracy(Location location) {
        float accuracy = location.getAccuracy();
        if (accuracy < 0.01) {
            return "?";
        } else if (accuracy > 99) {
            return "99+";
        } else {
            return String.format(Locale.US, "%2.0fm", accuracy);
        }
    }

    public String getLatitude(Location location) {
        return String.format(Locale.US, "%2.5f", location.getLatitude());
    }

    private String getDMSLatitude(Location location) {
        double val = location.getLatitude();
        return String.format(Locale.US, "%.0f° %2.0f′ %2.3f″ %s",
                Math.floor(Math.abs(val)),
                Math.floor(Math.abs(val * 60) % 60),
                (Math.abs(val) * 3600) % 60,
                val > 0 ? "N" : "S"
        );
    }

    private String getDMSLongitude(Location location) {
        double val = location.getLongitude();
        return String.format(Locale.US, "%.0f° %2.0f′ %2.3f″ %s",
                Math.floor(Math.abs(val)),
                Math.floor(Math.abs(val * 60) % 60),
                (Math.abs(val) * 3600) % 60,
                val > 0 ? "E" : "W"
        );
    }

    private String getLongitude(Location location) {
        return String.format(Locale.US, "%3.5f", location.getLongitude());
    }

    public String formatLocation(Location location, String format) {
        return MessageFormat.format(format,
                getLatitude(location), getLongitude(location));
    }
}
