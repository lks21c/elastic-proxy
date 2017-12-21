package com.creamsugardonut.kibanaproxy.repository;

import java.util.Map;

public class EsCacheRepositoryImpl implements CacheRepository {
    @Override
    public void setCache(String key, int year, int month, Integer day, int hour, int minute, String query, Map<String, Object> cachePeriod) {

    }

    @Override
    public Map<String, Object> getCache(String key, int year, int month, Integer day, int hour, int minute, String query) {
        return null;
    }
}
