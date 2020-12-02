package com.example.wifisurvey;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocationPingDAO {
    @Insert
    void insertLocationPing(LocationPing ping);

    @Query("SELECT * FROM LocationPings")
    List<LocationPing> getLocationPings();

    @Query("SELECT * FROM LocationPings LIMIT 100")
    List<LocationPing> get100LocationPings();

    @Query("SELECT * FROM LocationPings WHERE timeSinceBootNanos <= :nanos ORDER BY timeSinceBootNanos DESC LIMIT 1")
    LocationPing getPreviousLocationFromTime(long nanos);

    @Query("SELECT * FROM LocationPings WHERE timeSinceBootNanos >= :nanos ORDER BY timeSinceBootNanos ASC LIMIT 1")
    LocationPing getNextLocationFromTime(long nanos);

    @Query("SELECT * FROM LocationPings ORDER BY timeSinceBootNanos DESC")
    List<LocationPing> getLocationsByTimeDesc();

    @Query("SELECT * FROM LocationPings ORDER BY timeSinceBootNanos ASC")
    List<LocationPing> getLocationsByTimeAsc();
}
