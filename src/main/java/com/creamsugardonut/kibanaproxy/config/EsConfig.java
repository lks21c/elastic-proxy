package com.creamsugardonut.kibanaproxy.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;

/**
 * ES 클라이언트 설정
 *
 * @author lks21c
 */
@ComponentScan("com.creamsugardonut.kibanaproxy")
@Configuration
public class EsConfig {
    private Logger logger = LoggerFactory.getLogger(EsConfig.class);

    private static String profile = System.getProperty("spring.profile.active");

    @Bean(destroyMethod = "close")
    public RestHighLevelClient transportClient() throws UnknownHostException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("alyes.melon.com", 80, "http")));
        return client;
    }
}
