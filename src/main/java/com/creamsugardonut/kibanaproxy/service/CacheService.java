package com.creamsugardonut.kibanaproxy.service;

import com.creamsugardonut.kibanaproxy.conts.CacheMode;
import com.creamsugardonut.kibanaproxy.repository.CacheRepository;
import com.creamsugardonut.kibanaproxy.util.IndexNameUtil;
import com.creamsugardonut.kibanaproxy.util.JsonUtil;
import com.creamsugardonut.kibanaproxy.vo.DateHistogramBucket;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author lks21c
 */
@Service
public class CacheService {
    private static final Logger logger = LogManager.getLogger(CacheService.class);

    @Autowired
    private ElasticSearchServiceService esService;

    @Autowired
    private ParsingService parsingService;

    @Autowired
    private CacheRepository cacheRepository;

    @Value("${zuul.routes.proxy.url}")
    private String esUrl;

    public String manipulateQuery(String info) throws IOException, MethodNotSupportedException {
        logger.info("info = " + info);

        String[] arr = info.split("\n");
        Map<String, Object> iMap = parsingService.parseXContent(arr[0]);
        Map<String, Object> qMap = parsingService.parseXContent(arr[1]);

        List<String> idl = (List<String>) iMap.get("index");
        String indexName = IndexNameUtil.getIndexName(idl);

        for (String key : qMap.keySet()) {
            logger.info("key = " + key);
        }

        // Get gte, lte
        DateTime startDt = null, endDt = null;
        Map<String, Object> query = (Map<String, Object>) qMap.get("query");
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

        logger.info("invoked here");

        // Get aggs
        Map<String, Object> aggs = (Map<String, Object>) qMap.get("aggs");


        List<DateHistogramBucket> dhbList = cacheRepository.getCache(indexName, JsonUtil.convertAsString(aggs), startDt, endDt);
        logger.info("dhbList = " + JsonUtil.convertAsString(dhbList));

        // Parse 1 depth aggregation
        // Get aggs
        String interval = null;
        if (aggs.size() == 1) {
            for (String aggsKey : aggs.keySet()) {
                Map<String, Object> firstDepthAggs = (Map<String, Object>) aggs.get(aggsKey);

                Map<String, Object> date_histogram = (Map<String, Object>) firstDepthAggs.get("date_histogram");

                if (date_histogram != null) {
                    interval = (String) date_histogram.get("interval");
                    logger.info("interval = " + interval);
                }
            }
        }

        String cacheMode = checkCacheMode(interval, startDt, endDt, dhbList);
        logger.info("cacheMode = " + cacheMode);

        if (CacheMode.ALL.equals(cacheMode)) {
            String res = "{\n" +
                    "  \"responses\": [\n" +
                    "    {\n" +
                    "      \"took\": 3,\n" +
                    "      \"timed_out\": false,\n" +
                    "      \"_shards\": {\n" +
                    "        \"total\": 55,\n" +
                    "        \"successful\": 55,\n" +
                    "        \"failed\": 0\n" +
                    "      },\n" +
                    "      \"hits\": {\n" +
                    "        \"total\": 2718971,\n" +
                    "        \"max_score\": 0,\n" +
                    "        \"hits\": []\n" +
                    "      },\n" +
                    "      \"aggregations\": {\n" +
                    "        \"2\": {\n" +
                    "          \"buckets\": \n";

            res += JsonUtil.convertAsString(dhbList);

            res += "            \n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"status\": 200\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            logger.info("final res = " + res);

            /* 임시 */
            esService.executeQuery(esUrl + "/_msearch", info);
            /* 임시 */

            return res;
        } else {
            // Cacheable
            if ((interval.contains("d") && startDt.getSecondOfDay() == 0)
                    || (interval.contains("h") && startDt.getMinuteOfHour() == 0 && startDt.getSecondOfMinute() == 0)
                    || (interval.contains("m") && startDt.getSecondOfMinute() == 0)) {
                logger.info("cacheable");

                HttpResponse res = esService.executeQuery(esUrl + "/_msearch", info);

                String entity = null;
                try {
                    entity = EntityUtils.toString(res.getEntity());
                } catch (Exception e) {
                    logger.info("exception occurred");
                    e.printStackTrace();
                }
                logger.info("before put cache");
                cacheRepository.putCache(entity, indexName, JsonUtil.convertAsString(aggs), interval);
                return entity;
            }
        }

        return null;
    }

    private String checkCacheMode(String interval, DateTime startDt, DateTime endDt, List<DateHistogramBucket> dhbList) {
        int startTimeFirstCacheGap = -1;

        if (dhbList.size() > 0) {
            startTimeFirstCacheGap = Days.daysBetween(startDt, dhbList.get(0).getDate()).getDays();
        }

        if ("1d".equals(interval)) {
            if (Days.daysBetween(startDt, endDt).getDays() + 1 == dhbList.size()) {
                return CacheMode.ALL;
            } else if (dhbList.size() > 0 && startTimeFirstCacheGap == 0) {
                return CacheMode.PARTIAL;
            }
        }
        return CacheMode.NOCACHE;
    }
}
