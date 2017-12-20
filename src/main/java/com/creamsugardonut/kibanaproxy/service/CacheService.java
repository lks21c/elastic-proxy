package com.creamsugardonut.kibanaproxy.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CacheService {
    private static final Logger logger = LogManager.getLogger(CacheService.class);

    public void manipulateQuery(Map<String, Object> map) {
        for (String key : map.keySet()) {
            logger.info("key = " + key);
        }

        // Get gte, lte
        DateTime startDt = null, endDt = null;
        Map<String, Object> query = (Map<String, Object>) map.get("query");
        Map<String, Object> bool = (Map<String, Object>) query.get("bool");
        List<Map<String, Object>> must = (List<Map<String, Object>>) bool.get("must");
        for (Map<String, Object> obj : must) {

            Map<String, Object> range = (Map<String, Object>) obj.get("range");
            if (range != null) {
                for (String rangeKey : range.keySet()) {
                    Long gte = (Long) ((Map<String, Object>) range.get(rangeKey)).get("gte");
                    Long lte = (Long) ((Map<String, Object>) range.get(rangeKey)).get("lte");
                    startDt = new DateTime(gte);
                    endDt = new DateTime(lte);

                    logger.info("startDt = " + startDt);
                    logger.info("endDt = " + endDt);
                }
            }
        }

        // Get aggs
        Map<String, Object> aggs = (Map<String, Object>) map.get("aggs");
        if (aggs.size() == 1) {
            for (String aggsKey : aggs.keySet()) {
                Map<String, Object> firstDepthAggs = (Map<String, Object>) aggs.get(aggsKey);

                Map<String, Object> date_histogram = (Map<String, Object>) firstDepthAggs.get("date_histogram");
                String interval = null;
                if (date_histogram != null) {
                    interval = (String) date_histogram.get("interval");
                    logger.info("interval = " + interval);
                }

                // Cacheable
                if ((interval.contains("d") && startDt.getSecondOfDay() == 0)
                        || (interval.contains("h") && startDt.getMinuteOfHour() == 0 && startDt.getSecondOfMinute() == 0)
                        || (interval.contains("m") && startDt.getSecondOfMinute() == 0)) {
                    logger.info("cacheable");
                }
            }
        }
    }
}
