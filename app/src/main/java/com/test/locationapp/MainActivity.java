package com.test.locationapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean tripStart = true;
    private SharedPreferences sharedPreferences;
    private Button tripBtn;
    TextView tv_json;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        tripBtn = findViewById(R.id.tripBtn);
        Button tripDetailsBtn = findViewById(R.id.btnTripDetails);
        tv_json = findViewById(R.id.tv_json);
        tripBtn.setOnClickListener(v -> {
            if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                startStopTrip();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        });

        tripDetailsBtn.setOnClickListener(v -> {
            List<TripDetails> tripDetails = TripDetails.getTripDetails(MainActivity.this);
            String s = new Gson().toJson(tripDetails);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement jsonElement = new JsonParser().parse(s);
            tv_json.setText(gson.toJson(jsonElement));
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            startStopTrip();
        }
    }

    boolean hasPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(this, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy() {
        TripDetails.updateTrip(this, sharedPreferences.getLong("currentTripId", 0), TripDetails.getCurrentDateTime());
        super.onDestroy();
    }

    TripDetails createTrip() {
        TripDetails tripDetails = new TripDetails();
        tripDetails.setStartTime(TripDetails.getCurrentDateTime());
        return tripDetails;
    }

    private void startStopTrip() {
        if (tripStart) {
            TripDetails.createTrip(this, createTrip(), sharedPreferences);
            bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
            tripStart = false;
            tripBtn.setText("End Trip");
        } else {
            tripStart = true;
            TripDetails.updateTrip(this, sharedPreferences.getLong("currentTripId", 0), TripDetails.getCurrentDateTime());
            if (mService != null) {
                mService.removeLocationUpdates();
                unbindService(mServiceConnection);
            }
            tripBtn.setText("Start Trip");
        }
    }


    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            if (mService != null) {
                mService.requestLocationUpdates();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };
}