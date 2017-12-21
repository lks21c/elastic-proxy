package com.creamsugardonut.kibanaproxy.service;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;

import java.io.IOException;

public interface ParsingService {

    public QueryBuilder parseQuery(String query) throws IOException;

    public AggregatorFactories.Builder parseAggs(String aggs) throws IOException;


}
