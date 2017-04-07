package com.coen390.team_d.heartratemonitor;

import android.util.Log;

import com.amazonaws.models.nosql.HeartRatesDO;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Kyle on 2017-04-06.
 */

public class HeartRateLog {
    private static final String TAG = "HeartRateLog";

    public static HashMap<String, LineGraphSeries> userHRLogs = new HashMap<>();

    public static void addHeartRate(HeartRatesDO newHR, boolean GraphScroll) {
        String userName = newHR.getUserId();
        Double heartRate = newHR.getHeartRate();
        Date timestamp = java.sql.Timestamp.valueOf(newHR.getLastTimeStamp());

        Log.d(TAG, "Adding heart rate to log: " + userName);

        if (!userHRLogs.containsKey(userName)) {
            userHRLogs.put(userName, new LineGraphSeries<>());
        }

        userHRLogs.get(userName).appendData(new DataPoint(timestamp, heartRate.intValue()), GraphScroll, 3600);
    }
}
