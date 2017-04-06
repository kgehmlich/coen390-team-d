package com.amazonaws.models.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "coenteamd-mobilehub-273918624-HeartRates")

public class HeartRatesDO {
    private String _userId;
    private Double _age;
    private Boolean _alert;
    private Double _heartRate;
    private String _lastTimeStamp;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBAttribute(attributeName = "age")
    public Double getAge() {
        return _age;
    }

    public void setAge(final Double _age) {
        this._age = _age;
    }
    @DynamoDBAttribute(attributeName = "alert")
    public Boolean getAlert() {
        return _alert;
    }

    public void setAlert(final Boolean _alert) {
        this._alert = _alert;
    }
    @DynamoDBAttribute(attributeName = "heartRate")
    public Double getHeartRate() {
        return _heartRate;
    }

    public void setHeartRate(final Double _heartRate) {
        this._heartRate = _heartRate;
    }
    @DynamoDBAttribute(attributeName = "lastTimeStamp")
    public String getLastTimeStamp() {
        return _lastTimeStamp;
    }

    public void setLastTimeStamp(final String _lastTimeStamp) {
        this._lastTimeStamp = _lastTimeStamp;
    }

}
