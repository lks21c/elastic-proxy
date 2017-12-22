package com.creamsugardonut.kibanaproxy.repository;

import com.creamsugardonut.kibanaproxy.vo.DateHistogramBucket;
import org.apache.http.HttpResponse;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

/**
 * @author lks21c
 */
public interface CacheRepository {
    public List<DateHistogramBucket> getCache(String indexName, String agg, DateTime startDt, DateTime endDt) throws IOException;

    public void putCache(HttpResponse res, String indexName, String agg, String interval) throws IOException;
}
