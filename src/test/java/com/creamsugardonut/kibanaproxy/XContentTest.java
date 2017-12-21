package com.creamsugardonut.kibanaproxy;

import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.plugins.SearchPlugin;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XContentTest {

    private final List<NamedWriteableRegistry.Entry> namedWriteables = new ArrayList<>();
    private final List<NamedXContentRegistry.Entry> namedXContents = new ArrayList<>();

    @Test
    public void term() throws IOException {
        String term = " {\"term\": {\n" +
                "      \"poc_category\": {\n" +
                "        \"value\": \"sample\"\n" +
                "      }\n" +
                "    }}";

        registerQuery(new SearchPlugin.QuerySpec<>(TermQueryBuilder.NAME, TermQueryBuilder::new, TermQueryBuilder::fromXContent));
        NamedXContentRegistry registry = new NamedXContentRegistry(namedXContents);

        XContentParser parser = JsonXContent.jsonXContent.createParser(registry, term);
        QueryBuilder qb = AbstractQueryBuilder.parseInnerQueryBuilder(parser);
        System.out.println("qb = " + qb.toString());
    }

    @Test
    public void rangeQuery() throws IOException {
        String range = "{\n" +
                "          \"range\": {\n" +
                "            \"ts\": {\n" +
                "              \"gte\": 1513263600000,\n" +
                "              \"lte\": 1513758227507,\n" +
                "              \"format\": \"epoch_millis\"\n" +
                "            }\n" +
                "          }\n" +
                "        }";

        registerQuery(new SearchPlugin.QuerySpec<>(RangeQueryBuilder.NAME, RangeQueryBuilder::new, RangeQueryBuilder::fromXContent));
        NamedXContentRegistry registry = new NamedXContentRegistry(namedXContents);

        XContentParser parser = JsonXContent.jsonXContent.createParser(registry, range);
        QueryBuilder qb = AbstractQueryBuilder.parseInnerQueryBuilder(parser);
        System.out.println("qb = " + qb.toString());
    }

    @Test
    public void aggs() throws IOException {
        String aggs = "{\"aggs\": {\n" +
                "    \"2\": {\n" +
                "      \"date_histogram\": {\n" +
                "        \"field\": \"ts\",\n" +
                "        \"interval\": \"1d\",\n" +
                "        \"time_zone\": \"Asia/Tokyo\",\n" +
                "        \"min_doc_count\": 1\n" +
                "      },\n" +
                "      \"aggs\": {\n" +
                "        \"3\": {\n" +
                "          \"terms\": {\n" +
                "            \"field\": \"log_type\",\n" +
                "            \"size\": 10,\n" +
                "            \"order\": {\n" +
                "              \"1\": \"desc\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"aggs\": {\n" +
                "            \"1\": {\n" +
                "              \"sum\": {\n" +
                "                \"field\": \"datapoint\"\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }}";


        registerQuery(new SearchPlugin.QuerySpec<>(TermQueryBuilder.NAME, TermQueryBuilder::new, TermQueryBuilder::fromXContent));
        NamedXContentRegistry registry = new NamedXContentRegistry(namedXContents);

        XContentParser parser = JsonXContent.jsonXContent.createParser(registry, aggs);
        QueryBuilder qb = AbstractQueryBuilder.parseInnerQueryBuilder(parser);
        System.out.println("qb = " + qb.toString());
    }

    private void registerQuery(SearchPlugin.QuerySpec<?> spec) {
        namedWriteables.add(new NamedWriteableRegistry.Entry(QueryBuilder.class, spec.getName().getPreferredName(), spec.getReader()));
        namedXContents.add(new NamedXContentRegistry.Entry(QueryBuilder.class, spec.getName(),
                (p, c) -> spec.getParser().fromXContent(p)));
    }
}
