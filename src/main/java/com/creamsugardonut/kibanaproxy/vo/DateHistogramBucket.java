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
}
