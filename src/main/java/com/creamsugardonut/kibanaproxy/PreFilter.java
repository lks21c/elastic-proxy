package com.creamsugardonut.kibanaproxy;

import com.creamsugardonut.kibanaproxy.service.CacheService;
import com.creamsugardonut.kibanaproxy.service.ElasticSearchServiceService;
import com.creamsugardonut.kibanaproxy.service.NativeParsingServiceImpl;
import com.creamsugardonut.kibanaproxy.util.JsonUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lks21c
 */
@Service
public class PreFilter extends ZuulFilter {
    private static Logger logger = LoggerFactory.getLogger(PreFilter.class);

    @Autowired
    ElasticSearchServiceService esService;

    @Autowired
    NativeParsingServiceImpl parsingService;

    @Autowired
    CacheService cacheService;

    @Value("${zuul.routes.proxy.url}")
    private String esUrl;

    @Value("${esc.cache}")
    private Boolean esCache;

    private static final String PROXY = "proxy";

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

        //TODO: 임시로 처리, 지울 예정
        if (request.getRequestURI().equals("/proxy/.kibana/config/_search") ||
                request.getRequestURI().equals("/proxy/_mget") ||
                request.getRequestURI().equals("/proxy/_cluster/settings") ||
                request.getRequestURI().equals("/proxy/_nodes/_local") ||
                request.getRequestURI().equals("/proxy/_nodes") ||
                request.getRequestURI().equals("/proxy/") ||
                request.getRequestURI().equals("/proxy/_mapping") ||
                request.getRequestURI().equals("/proxy/_aliases") ||
                request.getRequestURI().equals("/proxy/_cluster/health/.kibana")) {
            return null;
        }

        try {
            String url;
            if (!StringUtils.isEmpty(request.getQueryString())) {
                url = request.getRequestURI().replace("/" + PROXY, "") + "?" + request.getQueryString();
            } else {
                url = request.getRequestURI().replace("/" + PROXY, "");
            }
            String targetUrl = esUrl + url;
            logger.info("request = " + targetUrl);

            if ("POST".equals(request.getMethod())) {
                String reqBody = getRequestBody(request);

                logger.info("original curl -X POST -L '" + targetUrl + "' " + " --data '" + reqBody + "'");

                logger.info("reqBody = " + reqBody);

                if (request.getRequestURI().contains("/_msearch")) {
//                    Enumeration<String> headers = request.getHeaderNames();
//                    while (headers.hasMoreElements()) {
//                        String header = headers.nextElement();
//                        System.out.println("header = " + header + " " + request.getHeader(header));
//                    }

                    String[] reqs = reqBody.split("\n");

                    StringBuilder sb = new StringBuilder();
                    if(esCache && !reqBody.contains(".kibana")) {
                        List<Map<String, Object>> rb = new ArrayList<>();
                        // Handles multi search
                        if (reqs.length > 2) {
                            for (int i = 0; i < reqs.length; i = i + 2) {
                                String body = cacheService.manipulateQuery(reqs[i] + "\n" + reqs[i + 1] + "\n");
                                Map<String, Object> resMap = parsingService.parseXContent(body);
                                List<Map<String, Object>> responses = (List<Map<String, Object>>) resMap.get("responses");
                                rb.add(responses.get(0));
                            }
                            Map<String, Object> mergedRes = new HashMap<>();
                            mergedRes.put("responses", rb);
                            sb.append(JsonUtil.convertAsString(mergedRes));
                        } else {
                            for (int i = 0; i < reqs.length; i = i + 2) {
                                String body = cacheService.manipulateQuery(reqs[i] + "\n" + reqs[i + 1] + "\n");
                                sb.append(body + "\n");
                            }
                        }
                    } else {
                        HttpResponse res = esService.executeQuery(targetUrl, reqBody);
                        sb.append(EntityUtils.toString(res.getEntity()));
                    }

                    if (sb.length() > 0) {
                        logger.info("sc ok ");
                        logger.info("resBody = " + sb.toString());
                        ctx.addZuulResponseHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                        ctx.addZuulResponseHeader(HttpHeaders.VARY, "Accept-Encoding");
                        ctx.addZuulResponseHeader(HttpHeaders.CONNECTION, "Keep-Alive");
                        ctx.setResponseStatusCode(HttpStatus.SC_OK);
                        ctx.setResponseBody(sb.toString());
                        ctx.setSendZuulResponse(false);
                    } else {
                        logger.error("original request invoked.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        return request.getReader().lines().collect(Collectors.joining(System.lineSeparator())) + "\n";
    }
}
