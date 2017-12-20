package com.creamsugardonut.kibanaproxy.service;

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
        Map<String, Object> query = (Map<String, Object>) map.get("query");
        Map<String, Object> bool = (Map<String, Object>) query.get("bool");
        List<Map<String, Object>> must = (List<Map<String, Object>>) bool.get("must");
        for (Map<String, Object> obj : must) {

            Map<String, Object> range = (Map<String, Object>) obj.get("range");
            if (range != null) {
                for (String rangeKey : range.keySet()) {
                    Long gte = (Long) ((Map<String, Object>) range.get(rangeKey)).get("gte");
                    Long lte = (Long) ((Map<String, Object>) range.get(rangeKey)).get("lte");
                    System.out.println("gte = " + gte);
                    System.out.println("lte = " + lte);
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

                }
            }
        }
    }
}
