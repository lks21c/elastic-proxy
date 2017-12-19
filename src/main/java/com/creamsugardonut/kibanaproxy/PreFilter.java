package com.creamsugardonut.kibanaproxy;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Service
public class PreFilter extends ZuulFilter {
    private static Logger logger = LoggerFactory.getLogger(PreFilter.class);

    @Autowired
    HttpService httpService;

    @Value("${zuul.routes.proxy.url}")
    private String esUrl;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        if (request.getRequestURI().equals("/proxy/.kibana/config/_search") ||
                request.getRequestURI().equals("/proxy/_mget") ||
                request.getRequestURI().equals("/proxy/_cluster/settings") ||
                request.getRequestURI().equals("/proxy/_nodes/_local") ||
                request.getRequestURI().equals("/proxy/_nodes") ||
                request.getRequestURI().equals("/proxy/") ||
                request.getRequestURI().equals("/proxy/_cluster/health/.kibana")) {
            return null;
        }

        try {
//            Enumeration<String> headers = request.getHeaderNames();
//
//            StringBuilder sb = new StringBuilder();
//            while (headers.hasMoreElements()) {
//                String header = headers.nextElement();
//                logger.info("header = " + header + " " + request.getHeader(header));
//                sb.append(" -H '" + header + ": " + request.getHeader(header) + "' ");
//            }

            String url = request.getRequestURI() + "?" + request.getQueryString();
            logger.info("request = " + request.getRequestURI());

            if ("POST".equals(request.getMethod())) {
                String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator())) + "\n";
                logger.info("request body = " + body);

                logger.info("curl -X POST -L '" + "http://alyes.melon.com" + url.replace("/proxy", "") + "' " + " --data '" + body + "'");

                if (request.getRequestURI().equals("/proxy/_msearch")) {
                    HttpResponse res = httpService.executeHttpRequest(HttpMethod.POST, esUrl + url.replace("/proxy", ""), new StringEntity(body));
                    logger.info("res = " + EntityUtils.toString(res.getEntity()));
                    ctx.setResponseBody(EntityUtils.toString(res.getEntity()));
                    ctx.setSendZuulResponse(false);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
}
