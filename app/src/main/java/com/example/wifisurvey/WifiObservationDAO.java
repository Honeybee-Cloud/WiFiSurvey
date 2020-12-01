package com.example.wifisurvey;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WifiObservationDAO {
    @Insert
    void insertWifiObservation(WifiObservation obs);

    @Query("SELECT * FROM WifiObservations")
    List<WifiObservation> getWifiObservations();

}
