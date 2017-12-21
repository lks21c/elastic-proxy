package com.creamsugardonut.kibanaproxy;

import com.creamsugardonut.kibanaproxy.service.ElasticSearchServiceService;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HttpClientTest {

    @Autowired
    ElasticSearchServiceService elasticSearchServiceService;

    @Test
    public void test() throws Exception {
        String requestBody = "{\"index\":[\"mel_com_private_music_st_realtime_member_20171219\"],\"ignore_unavailable\":true,\"preference\":1513646426988}\n" +
                "{\"query\":{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"*\",\"analyze_wildcard\":true}},{\"query_string\":{\"query\":\"*\",\"analyze_wildcard\":true}},{\"range\":{\"ts\":{\"gte\":1513646896942,\"lte\":1513647796942,\"format\":\"epoch_millis\"}}}],\"must_not\":[]}},\"size\":0,\"_source\":{\"excludes\":[]},\"aggs\":{\"2\":{\"date_histogram\":{\"field\":\"ts\",\"interval\":\"1m\",\"time_zone\":\"Asia/Tokyo\",\"min_doc_count\":1},\"aggs\":{\"1\":{\"sum\":{\"field\":\"datapoint\"}}}}},\"version\":true,\"highlight\":{\"pre_tags\":[\"@kibana-highlighted-field@\"],\"post_tags\":[\"@/kibana-highlighted-field@\"],\"fields\":{\"*\":{\"highlight_query\":{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"*\",\"analyze_wildcard\":true,\"all_fields\":true}},{\"query_string\":{\"query\":\"*\",\"analyze_wildcard\":true,\"all_fields\":true}},{\"range\":{\"ts\":{\"gte\":1513646896942,\"lte\":1513647796942,\"format\":\"epoch_millis\"}}}],\"must_not\":[]}}}},\"fragment_size\":2147483647}}\n";
        HttpResponse response = elasticSearchServiceService.executeHttpRequest(HttpMethod.POST, "http://alyes.melon.com/_msearch", new StringEntity(requestBody));

        System.out.println(EntityUtils.toString(response.getEntity()));
    }
}
