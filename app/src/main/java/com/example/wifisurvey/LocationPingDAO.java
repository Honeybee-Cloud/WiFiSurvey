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
}
