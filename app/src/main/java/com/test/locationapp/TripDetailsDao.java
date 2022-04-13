package com.test.locationapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface TripDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> createTrip(TripDetails data);

    @Query("Update tripdetails set endTime=:endTime where tripId=:tripId")
    Completable updateTrip(String endTime, long tripId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertLocation(LocationData locationData);

    @Query("select * from tripdetails ")
    List<TripDetails> getTripDetails();

    @Query("select * from locationdata where tripId=:id")
    List<LocationData> getLocations(int id);

}
