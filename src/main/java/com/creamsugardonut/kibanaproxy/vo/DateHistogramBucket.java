package com.creamsugardonut.kibanaproxy.vo;

import org.joda.time.DateTime;

import java.util.Map;

public class DateHistogramBucket {

    private DateTime date;

    private Map<String, Object> bucket;

    public DateHistogramBucket(DateTime dateTime, Map<String, Object> bucket) {
        this.date = dateTime;
        this.bucket = bucket;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public Map<String, Object> getBucket() {
        return bucket;
    }

    public void setBucket(Map<String, Object> bucket) {
        this.bucket = bucket;
    }
}
