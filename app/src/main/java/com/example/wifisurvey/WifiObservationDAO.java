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

}
