package com.test.locationapp;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationUpdatesService extends Service {

    private static final String PACKAGE_NAME =
            "com.test.locationapp";

    private static final String TAG = LocationUpdatesService.class.getSimpleName();

    private static final String CHANNEL_ID = "channel_01";

    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static final int NOTIFICATION_ID = 12345678;

    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    public LocationUpdatesService() {
    }

    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        if (!mChangingConfiguration) {
            Log.i(TAG, "Starting foreground service");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, getNotification(), FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(NOTIFICATION_ID, getNotification());
            }
        }
        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }


    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");
        startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            stopSelf();
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, LocationUpdatesService.class);

        CharSequence text = "Getting Location Updates";

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText("TicketPRO is accessing your location")
                .setOngoing(true)
                .setSilent(true)
                .setPriority(Notification.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful() || task.getResult() == null) {
                            Log.w(TAG, "Failed to get location.");
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);

        LocationData locationData = new LocationData();
        locationData.setAccuracy((double) location.getAccuracy());
        locationData.setLatitude(location.getLatitude());
        locationData.setLongitide(location.getLongitude());
        locationData.setTimestamp(TripDetails.getCurrentDateTime());
        locationData.setTripId(getSharedPreferences(getPackageName(), MODE_PRIVATE).getLong("currentTripId", 0));
        LocationData.insertLocation(getApplicationContext(), locationData);

        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    private void createLocationRequest() {


        mLocationRequest = LocationRequest.create()
                .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }


    public class LocalBinder extends Binder {
        public LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }


    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }
}
