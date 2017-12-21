package com.creamsugardonut.kibanaproxy.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InMemoryCacheRepositoryImpl {
    private Logger logger = LogManager.getLogger(InMemoryCacheRepositoryImpl.class);

    public static final String CACHE_KEY = "cache";

    @Autowired
    CacheManager cacheManager;

    public void setCache(String key, int year
            , int month, Integer day, int hour, int minute, String query, Map<String, Object> cachePeriod) {
        String cacheKey = key + year + "_" + month + "_" + day + "_" + hour + "_" + minute + "_" + query;
//        logger.info("set key = " + key);
        cacheManager.getCache(CACHE_KEY).put(key, cachePeriod);
    }

    public Map<String, Object> getCache(String key, int year, int month, Integer day, int hour, int minute, String query) {
        String cacheKey = key + year + "_" + month + "_" + day + "_" + hour + "_" + minute + "_" + query;
        //        logger.info("get key = " + key);
        Map<String, Object> cachePeriod = (Map<String, Object>) cacheManager.getCache(CACHE_KEY).get(key);

//        logger.info("cache : " + JsonUtil.convertAsString(cachePeriod));
        return cachePeriod;
    }
}
