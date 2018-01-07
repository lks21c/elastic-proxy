package com.creamsugardonut.kibanaproxy.repository;

import com.creamsugardonut.kibanaproxy.service.ParsingService;
import com.creamsugardonut.kibanaproxy.util.JsonUtil;
import com.creamsugardonut.kibanaproxy.vo.DateHistogramBucket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: This implementation needs to be completed.
 *
 * @author lks21c
 */
public class InMemoryCacheRepositoryImpl implements CacheRepository {
    private Logger logger = LogManager.getLogger(InMemoryCacheRepositoryImpl.class);

    public static final String CACHE_KEY = "cache";

    @Autowired
    CacheManager cacheManager;

    @Autowired
    private ParsingService parsingService;

    public Map<String, Object> getCache(String key, int year, int month, Integer day, int hour, int minute, String query) {
        String cacheKey = key + year + "_" + month + "_" + day + "_" + hour + "_" + minute + "_" + query;
        //        logger.info("get key = " + key);
        Map<String, Object> cachePeriod = (Map<String, Object>) cacheManager.getCache(CACHE_KEY).get(key);

//        logger.info("cache : " + JsonUtil.convertAsString(cachePeriod));
        return cachePeriod;
    }

    @Override
    public List<DateHistogramBucket> getCache(String indexName, String query, String agg, DateTime startDt, DateTime endDt) throws IOException {
        String key = indexName + agg;
        logger.info("get cache " + key);

        List<DateHistogramBucket> dhbList = new ArrayList<>();

        return dhbList;
    }

    @Override
    public void putCache(String res, String indexName, String query, String agg, String interval) throws IOException {
        String key = indexName + query + agg;
        Map<String, Object> resMap = null;
        try {
            logger.info("before res map");
            resMap = parsingService.parseXContent(res);
            logger.info("resMap = " + JsonUtil.convertAsString(resMap));
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("exception occurred");
        }

        List<Map<String, Object>> respes = (List<Map<String, Object>>) resMap.get("responses");
        for (Map<String, Object> resp : respes) {
            Map<String, Object> aggrs = (Map<String, Object>) resp.get("aggregations");
            for (String aggKey : aggrs.keySet()) {
                logger.info("aggKey = " + aggrs.get(aggKey));

                LinkedHashMap<String, Object> buckets = (LinkedHashMap<String, Object>) aggrs.get(aggKey);

                for (String bucketsKey : buckets.keySet()) {
                    List<Map<String, Object>> bucketList = (List<Map<String, Object>>) buckets.get(bucketsKey);
                    for (Map<String, Object> bucket : bucketList) {
                        String key_as_string = (String) bucket.get("key_as_string");
                        Long ts = (Long) bucket.get("key");
                        logger.info("for key_as_string = " + key_as_string);

                        if ("1d".equals(interval)) {
                            if (Days.daysBetween(new DateTime(ts), new DateTime())
                                    .isGreaterThan(Days.days(0))) { /* 과거 ~ 오늘전까지만 캐시 */

                                logger.info("put cache " + key + "_" + ts);
                                setCache(key + "_" + ts, JsonUtil.convertAsString(bucket));

                                //TODO:
                            }
                        }
                    }
                }
            }
        }
    }

    public void setCache(String key, String value) {
        cacheManager.getCache(CACHE_KEY).put(key, value);
    }
}
