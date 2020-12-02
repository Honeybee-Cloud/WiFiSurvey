package com.example.wifisurvey;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;

import java.util.List;

@Dao
public interface WifiObservationDAO {
    @Insert
    void insertWifiObservation(WifiObservation obs);

    @Query("SELECT * FROM WifiObservations")
    List<WifiObservation> getWifiObservations();

    @Query("SELECT * FROM WifiObservations LIMIT 100")
    List<WifiObservation> get100WifiObservations();

    @Query("SELECT * FROM WifiObservations WHERE timeSinceBootMicros <= :micros ORDER BY timeSinceBootMicros DESC LIMIT 1")
    WifiObservation getPreviousLocationFromTime(long micros);

    @Query("SELECT * FROM WifiObservations WHERE timeSinceBootMicros >= :micros ORDER BY timeSinceBootMicros ASC LIMIT 1")
    WifiObservation getNextObservationFromTime(long micros);

    @Query("SELECT * FROM WifiObservations ORDER BY timeSinceBootMicros DESC")
    List<WifiObservation> getObservationByTimeDesc();

    @Query("SELECT * FROM WifiObservations ORDER BY timeSinceBootMicros ASC")
    List<WifiObservation> getObservationByTimeAsc();

    @Query("SELECT * FROM WifiObservations WHERE (timeSinceBootMicros >= :micros0 AND timeSinceBootMicros < :micros1) ORDER BY timeSinceBootMicros ASC")
    List<WifiObservation> getObservationsBetweenTimesAsc(long micros0, long micros1);
}
