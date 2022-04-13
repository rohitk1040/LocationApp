package com.test.locationapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Entity
public class TripDetails {

    @ColumnInfo
    @PrimaryKey(autoGenerate = true)
    private int tripId;
    @ColumnInfo
    private String startTime;
    @ColumnInfo
    private String endTime;
    @Ignore
    private List<LocationData> locationData = null;

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public static void createTrip(Context context, TripDetails trip, SharedPreferences sharedPreferences) {
        AppDatabase.getInstance(context).getTripdetails().createTrip(trip).subscribeOn(Schedulers.io()).subscribeWith(new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(Long integer) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("currentTripId", integer);
                editor.apply();
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    public static void updateTrip(Context context, long tripid, String endTime) {
        AppDatabase.getInstance(context).getTripdetails().updateTrip(endTime, tripid).subscribeOn(Schedulers.io()).subscribeWith(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }



    public static List<TripDetails> getTripDetails(Context context) {
        List<TripDetails> tripDetails = AppDatabase.getInstance(context).getTripdetails().getTripDetails();
        for (TripDetails tripDetails1 : tripDetails) {
            tripDetails1.setLocationData(AppDatabase.getInstance(context).getTripdetails().getLocations(tripDetails1.getTripId()));
        }
        return tripDetails;
    }

    public static String getCurrentDateTime() {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        Date date = Calendar.getInstance().getTime();
        return dateFormat.format(date);
    }

    public List<LocationData> getLocationData() {
        return locationData;
    }

    public void setLocationData(List<LocationData> locationData) {
        this.locationData = locationData;
    }
}
