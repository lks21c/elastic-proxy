package com.creamsugardonut.kibanaproxy.service;

import com.creamsugardonut.kibanaproxy.util.JsonUtil;
import com.creamsugardonut.kibanaproxy.vo.DateHistogramBucket;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticSearchServiceService {
    private static final Logger logger = LogManager.getLogger(ElasticSearchServiceService.class);

    private CloseableHttpClient client = HttpClientBuilder.create().build();

    @Autowired
    private ParsingService parsingService;

    public HttpResponse executeHttpRequest(HttpMethod requestType, String url, StringEntity entity) throws IOException, MethodNotSupportedException {
        HttpResponse httpResponse = null;

        if (entity != null) {
            entity.setContentType("application/json");
        }
        switch (requestType) {
            case POST:

                HttpPost post = new HttpPost(url);

                post.setEntity(entity);
                httpResponse = client.execute(post);
                break;
            case GET:

                HttpGet httpGet = new HttpGet(url);

                httpResponse = client.execute(httpGet);
                break;
            case DELETE:

                HttpDelete httpDelete = new HttpDelete(url);

                httpResponse = client.execute(httpDelete);
                break;
            case PUT:

                HttpPut httpput = new HttpPut(url);

                httpput.setEntity(entity);
                httpResponse = client.execute(httpput);
                break;
            default:
                throw new MethodNotSupportedException(requestType.toString());
        }

        return httpResponse;
    }

    public HttpResponse executeQuery(String targetUrl, String reqBody) throws IOException, MethodNotSupportedException {
        logger.info("executeQuery");
        logger.info(targetUrl);
        logger.info(reqBody);

        HttpResponse res = executeHttpRequest(HttpMethod.POST, targetUrl, new StringEntity(reqBody));
        return res;
    }
}
