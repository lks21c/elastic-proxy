package com.creamsugardonut.kibanaproxy.service;

import com.creamsugardonut.kibanaproxy.conts.CacheMode;
import com.creamsugardonut.kibanaproxy.repository.CacheRepository;
import com.creamsugardonut.kibanaproxy.util.IndexNameUtil;
import com.creamsugardonut.kibanaproxy.util.JsonUtil;
import com.creamsugardonut.kibanaproxy.vo.DateHistogramBucket;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
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

    public void manipulateQuery(String info) throws IOException, MethodNotSupportedException {
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

        // Get aggs
        Map<String, Object> aggs = (Map<String, Object>) qMap.get("aggs");
        AggregatorFactories.Builder af = parsingService.parseAggs(JsonUtil.convertAsString(aggs));

        List<DateHistogramBucket> dhbList = cacheRepository.getCache(indexName, JsonUtil.convertAsString(aggs), startDt, endDt);

        // Parse 1 depth aggregation
        String interval = null;
        if (af.getAggregatorFactories().size() == 1) {
            for (AggregationBuilder ab : af.getAggregatorFactories()) {

                if (ab instanceof DateHistogramAggregationBuilder) {
                    DateHistogramAggregationBuilder dhb = (DateHistogramAggregationBuilder) ab;
                    interval = dhb.dateHistogramInterval().toString();
                }
            }
        }

        String cacheMode = checkCacheMode(interval, startDt, endDt, dhbList);
        logger.info("cacheMode = " + cacheMode);

        if (CacheMode.ALL.equals(cacheMode)) {

        } else {
            // Cacheable
            if ((interval.contains("d") && startDt.getSecondOfDay() == 0)
                    || (interval.contains("h") && startDt.getMinuteOfHour() == 0 && startDt.getSecondOfMinute() == 0)
                    || (interval.contains("m") && startDt.getSecondOfMinute() == 0)) {
                logger.info("cacheable");

                HttpResponse res = esService.executeQuery(esUrl + "/_msearch", info);
                cacheRepository.putCache(res, indexName, JsonUtil.convertAsString(aggs));
            }
        }
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
