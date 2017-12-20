package com.creamsugardonut.kibanaproxy.service;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CacheService {
    public void manipulateQuery(Map<String, Object> map) {
        for (String key : map.keySet()) {
            System.out.println("key = " + key);
        }

        // Get gte, lte
        DateTime startDt, endDt;
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

                    System.out.println("startDt = " + startDt);
                    System.out.println("endDt = " + endDt);
                }
            }
        }

        // Get aggs
        Map<String, Object> aggs = (Map<String, Object>) map.get("aggs");
        if (aggs.size() == 1) {
            for (String aggsKey : aggs.keySet()) {
                Map<String, Object> firstDepthAggs = (Map<String, Object>) aggs.get(aggsKey);

                // Cacheable
                if (firstDepthAggs.containsKey("date_histogram")) {
                    Map<String, Object> date_histogram = (Map<String, Object>) firstDepthAggs.get("date_histogram");
                    String interval = (String) date_histogram.get("interval");
                    System.out.println("interval = " + interval);
                }
            }
        }
    }
}
