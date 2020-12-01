package com.example.wifisurvey;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LocationPing.class, WifiObservation.class}, version = 1)
public abstract class WifiSurveyDatabase extends RoomDatabase {
    private static String DATABASE = "WifiSurveyDatabase";
    private static WifiSurveyDatabase instance;

    static WifiSurveyDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    WifiSurveyDatabase.class,
                    DATABASE)
                    .allowMainThreadQueries()
                    .build();
        }

        return instance;
    }

    public abstract WifiObservationDAO getWifiObservationDao();
    public abstract LocationPingDAO getLocationPingDao();
}
