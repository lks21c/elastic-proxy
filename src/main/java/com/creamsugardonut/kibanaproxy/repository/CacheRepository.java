package com.creamsugardonut.kibanaproxy.repository;

import java.util.Map;

public interface CacheRepository {
    public static final String CACHE_KEY = "cache";

    public void setCache(String key, int year
            , int month, Integer day, int hour, int minute, String query, Map<String, Object> cachePeriod);

    public Map<String, Object> getCache(String key, int year, int month, Integer day, int hour, int minute, String query);
}
