package com.creamsugardonut.kibanaproxy.service;

import com.creamsugardonut.kibanaproxy.util.JsonUtil;
import com.creamsugardonut.kibanaproxy.vo.DateHistogramBucket;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CacheService {
    private static final Logger logger = LogManager.getLogger(CacheService.class);

    @Autowired
    private ElasticSearchServiceService esService;

    @Autowired
    private NativeParsingServiceImpl parsingService;

    @Autowired
    private RestHighLevelClient restClient;

    @Value("${zuul.routes.proxy.url}")
    private String esUrl;

    public void manipulateQuery(String info) throws IOException, MethodNotSupportedException {

        String[] arr = info.split("\n");
        Map<String, Object> map = parsingService.parseXContent(arr[1]);

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
        AggregatorFactories.Builder af = parsingService.parseAggs(JsonUtil.convertAsString(aggs));

        if (af.getAggregatorFactories().size() == 1) {
            for (AggregationBuilder ab : af.getAggregatorFactories()) {

                String interval = null;
                if (ab instanceof DateHistogramAggregationBuilder) {
                    DateHistogramAggregationBuilder dhb = (DateHistogramAggregationBuilder) ab;
                    interval = dhb.dateHistogramInterval().toString();
                }

                // Cacheable
                if ((interval.contains("d") && startDt.getSecondOfDay() == 0)
                        || (interval.contains("h") && startDt.getMinuteOfHour() == 0 && startDt.getSecondOfMinute() == 0)
                        || (interval.contains("m") && startDt.getSecondOfMinute() == 0)) {
                    logger.info("cacheable");

                    HttpResponse res = esService.executeQuery(esUrl + "/_msearch", info);
                    putCache(res);
                }
            }
        }
    }

    private void putCache(HttpResponse res) throws IOException {
        Map<String, Object> resMap = parsingService.parseXContent(EntityUtils.toString(res.getEntity()));
        List<Map<String, Object>> respes = (List<Map<String, Object>>) resMap.get("responses");
        for (Map<String, Object> resp : respes) {
            List<DateHistogramBucket> dhbList = new ArrayList<>();
            BulkRequest br = new BulkRequest();
            Map<String, Object> aggrs = (Map<String, Object>) resp.get("aggregations");
            for (String aggKey : aggrs.keySet()) {
                logger.info(aggrs.get(aggKey));

                LinkedHashMap<String, Object> buckets = (LinkedHashMap<String, Object>) aggrs.get(aggKey);

                for (String bucketsKey : buckets.keySet()) {
                    List<Map<String, Object>> bucketList = (List<Map<String, Object>>) buckets.get(bucketsKey);
                    for (Map<String, Object> bucket : bucketList) {
                        String key_as_string = (String) bucket.get("key_as_string");
                        Long key = (Long) bucket.get("key");
                        logger.info("key_as_string = " + key_as_string);

                        DateHistogramBucket dhb = new DateHistogramBucket(new DateTime(key), bucket);
                        dhbList.add(dhb);

                        IndexRequest ir = new IndexRequest("cache", "info", key_as_string);
                        Map<String, Object> irMap = new HashMap<>();
                        irMap.put("key", key_as_string);
                        irMap.put("value", JsonUtil.convertAsString(bucket));
                        irMap.put("ts", key);
                        ir.source(irMap);
                        br.add(ir);
                    }
                }
            }
            restClient.bulk(br);
        }
    }
}
