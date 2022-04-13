package com.test.locationapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {TripDetails.class, LocationData.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "locationsample.db";
    public static AppDatabase instance;

    public static AppDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class,
                            DB_NAME)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract TripDetailsDao getTripdetails();
}
