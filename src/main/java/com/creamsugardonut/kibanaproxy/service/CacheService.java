package com.creamsugardonut.kibanaproxy.service;

import com.creamsugardonut.kibanaproxy.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CacheService {
    private static final Logger logger = LogManager.getLogger(CacheService.class);

    @Autowired
    private HttpService httpService;

    @Autowired
    private ParsingService parsingService;

    private ObjectMapper mapper = new ObjectMapper();

    public void manipulateQuery(Map<String, Object> map) throws IOException {
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
        AggregatorFactories.Builder af = parsingService.parseAggs(mapper.writeValueAsString(aggs));

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
                }
            }
        }
    }

    public HttpResponse executeQuery(String targetUrl, String reqBody) throws IOException, MethodNotSupportedException {
        HttpResponse res = httpService.executeHttpRequest(HttpMethod.POST, targetUrl, new StringEntity(reqBody));
        Map<String, Object> map = parsingService.parseXContent(EntityUtils.toString(res.getEntity()));
        logger.info("res = " + JsonUtil.convertAsString(map));
        return res;
    }
}
