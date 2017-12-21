package com.creamsugardonut.kibanaproxy.service;

import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;

import java.io.IOException;
import java.util.Map;

public interface ParsingService {

    public Map<String, Object> parseXContent(String str);

    public QueryBuilder parseQuery(String query) throws IOException;

    public AggregatorFactories.Builder parseAggs(String aggs) throws IOException;


}
