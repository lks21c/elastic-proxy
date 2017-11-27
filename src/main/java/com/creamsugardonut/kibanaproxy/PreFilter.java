package com.creamsugardonut.kibanaproxy;

import com.google.common.io.CharStreams;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class PreFilter extends ZuulFilter {
    private static Logger logger = LoggerFactory.getLogger(PreFilter.class);


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
        // RequestContext is shared by all ZuulFilters
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        // add custom headers
        ctx.addZuulRequestHeader("x-custom-header", "foobar");

        try {
            String url = request.getRequestURI() + "?" + request.getQueryString();
            System.out.println("request = " + url);

            if ("POST".equals(request.getMethod())) {
                String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                System.out.println("request body = " + body);
                System.out.println();
            }
        } catch (Exception e) {
        }

        return null;
    }
}
