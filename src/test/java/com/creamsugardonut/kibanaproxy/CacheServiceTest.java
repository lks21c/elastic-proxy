package com.creamsugardonut.kibanaproxy;

import com.creamsugardonut.kibanaproxy.service.CacheService;
import com.creamsugardonut.kibanaproxy.service.NativeParsingServiceImpl;
import org.apache.http.MethodNotSupportedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CacheServiceTest {
    @Autowired
    NativeParsingServiceImpl parsingService;

    @Autowired
    CacheService cacheService;

    @Test
    public void test() throws IOException, MethodNotSupportedException {
        String i = "{\"index\":[\"mel_com_private_music_st_realtime_member_*\"],\"ignore_unavailable\":true,\"preference\":1513646426988}\n";
        String q = "{\"query\":{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"*\",\"analyze_wildcard\":true}},{\"query_string\":{\"analyze_wildcard\":true,\"query\":\"*\"}},{\"range\":{\"ts\":{\"gte\":1513263600000,\"lte\":1513752110170,\"format\":\"epoch_millis\"}}}],\"must_not\":[]}},\"size\":0,\"_source\":{\"excludes\":[]},\"aggs\":{\"2\":{\"date_histogram\":{\"field\":\"ts\",\"interval\":\"1d\",\"time_zone\":\"Asia/Tokyo\",\"min_doc_count\":1},\"aggs\":{\"3\":{\"terms\":{\"field\":\"log_type\",\"size\":10,\"order\":{\"1\":\"desc\"}},\"aggs\":{\"1\":{\"sum\":{\"field\":\"datapoint\"}}}}}}},\"version\":true,\"highlight\":{\"pre_tags\":[\"@kibana-highlighted-field@\"],\"post_tags\":[\"@/kibana-highlighted-field@\"],\"fields\":{\"*\":{\"highlight_query\":{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"*\",\"analyze_wildcard\":true,\"all_fields\":true}},{\"query_string\":{\"analyze_wildcard\":true,\"query\":\"*\",\"all_fields\":true}},{\"range\":{\"ts\":{\"gte\":1513263600000,\"lte\":1513752110170,\"format\":\"epoch_millis\"}}}],\"must_not\":[]}}}},\"fragment_size\":2147483647}}\n";
//        System.out.println(q);
        cacheService.manipulateQuery(i + q);
    }

    @Test
    public void test2() throws IOException, MethodNotSupportedException {
        String info = "{\"index\":[\"mel_com_private_app_event_realtime_20171220\",\"mel_com_private_app_event_realtime_20171221\",\"mel_com_private_app_event_realtime_20171217\",\"mel_com_private_app_event_realtime_20171219\",\"mel_com_private_app_event_realtime_20171218\"],\"ignore_unavailable\":true,\"preference\":1513927712118}\n" +
                "{\"query\":{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"*\",\"analyze_wildcard\":true}},{\"query_string\":{\"analyze_wildcard\":true,\"query\":\"*\"}},{\"range\":{\"ts\":{\"gte\":1513436400000,\"lte\":1513846380354,\"format\":\"epoch_millis\"}}}],\"must_not\":[]}},\"size\":0,\"_source\":{\"excludes\":[]},\"aggs\":{\"2\":{\"date_histogram\":{\"field\":\"ts\",\"interval\":\"1d\",\"time_zone\":\"Asia/Tokyo\",\"min_doc_count\":1},\"aggs\":{\"3\":{\"terms\":{\"field\":\"log_type\",\"size\":10,\"order\":{\"1\":\"desc\"}},\"aggs\":{\"1\":{\"sum\":{\"field\":\"datapoint\"}}}}}}},\"version\":true,\"highlight\":{\"pre_tags\":[\"@kibana-highlighted-field@\"],\"post_tags\":[\"@/kibana-highlighted-field@\"],\"fields\":{\"*\":{\"highlight_query\":{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"*\",\"analyze_wildcard\":true,\"all_fields\":true}},{\"query_string\":{\"analyze_wildcard\":true,\"query\":\"*\",\"all_fields\":true}},{\"range\":{\"ts\":{\"gte\":1513436400000,\"lte\":1513846380354,\"format\":\"epoch_millis\"}}}],\"must_not\":[]}}}},\"fragment_size\":2147483647}}\n";
        String body = cacheService.manipulateQuery(info);
        System.out.println("final body = " + body);
    }
}
