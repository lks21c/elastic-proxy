package com.creamsugardonut.kibanaproxy.repository;

import com.creamsugardonut.kibanaproxy.service.ParsingService;
import com.creamsugardonut.kibanaproxy.util.JsonUtil;
import com.creamsugardonut.kibanaproxy.vo.DateHistogramBucket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.hash.MurmurHash3;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lks21c
 */
@Service
public class EsCacheRepositoryImpl implements CacheRepository {
    private static final Logger logger = LogManager.getLogger(EsCacheRepositoryImpl.class);

    @Autowired
    private RestHighLevelClient restClient;

    @Autowired
    private ParsingService parsingService;

    @Override
    public List<DateHistogramBucket> getCache(String indexName, String query, String agg, DateTime startDt, DateTime endDt) throws IOException {
        String key = indexName + query + agg;
        logger.info("get cache " + key);

        List<QueryBuilder> qbList = new ArrayList<>();
        qbList.add(QueryBuilders.termQuery("key", key));
        qbList.add(QueryBuilders.rangeQuery("ts").from(startDt).to(endDt));

        BoolQueryBuilder bq = QueryBuilders.boolQuery();
        bq.must().addAll(qbList);

        SearchSourceBuilder sb = new SearchSourceBuilder();
        sb.query(bq);
        sb.size(10000);
        sb.sort("ts", SortOrder.ASC);

        SearchRequest srch = new SearchRequest().indices("cache").types("info");
        srch.source(sb);

        logger.info("srch = " + sb.toString());
        SearchResponse sr = restClient.search(srch);
        logger.info("sr = " + sr.toString());

        List<DateHistogramBucket> dhbList = new ArrayList<>();
        for (SearchHit hit : sr.getHits().getHits()) {
            Map<String, Object> source = hit.getSourceAsMap();
            Long ts = (Long) source.get("ts");
            String value = (String) source.get("value");
            Map<String, Object> bucket = new Gson().fromJson(value, HashMap.class);
            dhbList.add(new DateHistogramBucket(new DateTime(ts), bucket));
        }

        return dhbList;
    }

    @Override
    public void putCache(String res, String indexName, String query, String agg, String interval) throws JsonProcessingException {
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
            List<DateHistogramBucket> dhbList = new ArrayList<>();
            BulkRequest br = new BulkRequest();
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

                                DateHistogramBucket dhb = new DateHistogramBucket(new DateTime(ts), bucket);
                                dhbList.add(dhb);

                                logger.info("put cache " + key + "_" + ts);
                                String str = key + "_" + ts;
                                MurmurHash3.Hash128 hash = MurmurHash3.hash128(str.getBytes(), 0, str.getBytes().length, 0, new MurmurHash3.Hash128());
                                String id = String.valueOf(hash.h1) + String.valueOf(hash.h2);
                                IndexRequest ir = new IndexRequest("cache", "info", id);
                                Map<String, Object> irMap = new HashMap<>();
                                irMap.put("value", JsonUtil.convertAsString(bucket));
                                irMap.put("key", key);
                                irMap.put("ts", ts);
                                ir.source(irMap);
                                br.add(ir);
                            }
                        } else if ("1m".equals(interval)) {
                            if (Minutes.minutesBetween(new DateTime(ts), new DateTime())
                                    .isGreaterThan(Minutes.minutes(0))) { /* 과거 ~ 현재 시간 1분전까지만 캐시 */

                                DateHistogramBucket dhb = new DateHistogramBucket(new DateTime(ts), bucket);
                                dhbList.add(dhb);

                                logger.info("put cache " + key + "_" + ts);
                                String str = key + "_" + ts;
                                MurmurHash3.Hash128 hash = MurmurHash3.hash128(str.getBytes(), 0, str.getBytes().length, 0, new MurmurHash3.Hash128());
                                String id = String.valueOf(hash.h1) + String.valueOf(hash.h2);
                                IndexRequest ir = new IndexRequest("cache", "info", id);
                                Map<String, Object> irMap = new HashMap<>();
                                irMap.put("value", JsonUtil.convertAsString(bucket));
                                irMap.put("key", key);
                                irMap.put("ts", ts);
                                ir.source(irMap);
                                br.add(ir);
                            }
                        }
                    }
                }
            }
            try {
                restClient.bulk(br);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
