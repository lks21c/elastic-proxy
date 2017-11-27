package com.creamsugardonut.kibanaproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableZuulProxy
@Configuration
public class KibanaProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(KibanaProxyApplication.class, args);
	}

    @Bean
    public PreFilter preFilter() {
        return new PreFilter();
    }

//    @Bean
//    public PostFilter postFilter() {
//        return new PostFilter();
//    }
}
