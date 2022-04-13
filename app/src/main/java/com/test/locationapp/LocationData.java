package com.test.locationapp;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Entity
public class LocationData {
    @ColumnInfo
    private Double latitude;
    @ColumnInfo
    private Double longitide;
    @ColumnInfo
    private String timestamp;
    @ColumnInfo
    private Double accuracy;
    @ColumnInfo
    private long tripId;
    @ColumnInfo
    @PrimaryKey(autoGenerate = true)
    private int id;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitide() {
        return longitide;
    }

    public void setLongitide(Double longitide) {
        this.longitide = longitide;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public static void insertLocation(Context context, LocationData locationData) {
        AppDatabase.getInstance(context).getTripdetails().insertLocation(locationData).subscribeOn(Schedulers.io()).subscribeWith(new CompletableObserver() {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
