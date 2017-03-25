package com.coen390.team_d.heartratemonitor;

import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.models.nosql.HeartRatesDO;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by kgehm on 2017-03-20.
 */

public class AWSDatabaseHelper {

    // Log tag
    private final static String TAG = "AWSDatabaseHelper";

    private String tempUserId = "tempUserId";

    private HeartRatesDO _item;

    /**
     * Constructor
     */
    public AWSDatabaseHelper() {
        _item = new HeartRatesDO();

    }


    public void sendAlert(int heartRate) {
        _item.setHeartRate((double) heartRate);
        _item.setAlert(true);

        this.send();
    }

    public void updateHeartRate(int heartRate) {
        _item.setHeartRate((double) heartRate);
        _item.setAlert(false);

        this.send();
    }


    /**
     * Sends _item to the database.
     * This method fills in userId and timestamp, so they are not required to be set beforehand.
     */
    private void send() {

        final DynamoDBMapper dynamoDBMapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

        // Check that alert was set (set it to false if it wasn't)
        if (_item.getAlert() == null) {
            _item.setAlert(false);
        }

        // Check that heart rate was set (set it to -1 if it wasn't)
        // TODO: throw exception if heart rate was not set
        if (_item.getHeartRate() == null) {
            _item.setHeartRate(-1d);
        }

        // TODO: replace the following line with a call to get the userID from SharedPrefs (once they are implemented)
        _item.setUserId(tempUserId);

        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
        _item.setLastTimeStamp(currentTimestamp.toString());


        // Write to DB
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                AmazonClientException lastException = null;
                try {
                    dynamoDBMapper.save(_item);
                } catch (final AmazonClientException ex) {
                    Log.e(TAG, "Failed saving _item : " + ex.getMessage(), ex);
                    lastException = ex;
                }
            }
        };

        Thread networkThread = new Thread(runnable);
        networkThread.start();
    }


    public ArrayList<HeartRatesDO> getListOfHeartRates() {

        ArrayList<HeartRatesDO> heartRatesList = new ArrayList<>();

        final DynamoDBMapper dynamoDBMapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

        PaginatedScanList<HeartRatesDO> scanList = dynamoDBMapper.scan(HeartRatesDO.class, new DynamoDBScanExpression());

        if (scanList.isEmpty()) {
            return null;
        }

        for (int i = 0; i < scanList.size(); i++) {
            heartRatesList.add(scanList.get(i));
        }
        return heartRatesList;
    }

}
