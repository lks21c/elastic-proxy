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
    public List<DateHistogramBucket> getCache(String indexName, String query, String agg, DateTime startDt, DateTime endDt) throws IOException;

    public void putCache(String res, String indexName, String query, String agg, String interval) throws IOException;
}
