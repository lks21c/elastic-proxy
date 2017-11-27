package com.creamsugardonut.kibanaproxy;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
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
            Enumeration<String> headers = request.getHeaderNames();

            StringBuilder sb = new StringBuilder();
            while (headers.hasMoreElements()) {
                String header = headers.nextElement();
                System.out.println("header = " + header + " " + request.getHeader(header));
                sb.append(" --header '" + header + ": " + request.getHeader(header) + "' ");
            }

            String url = request.getRequestURI() + "?" + request.getQueryString();
            System.out.println("request = " + url);

            if ("POST".equals(request.getMethod())) {
                String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                System.out.println("request body = " + body);
                System.out.println();

                System.out.println("curl -X POST -L '" + "http://alyes.melon.com" + url.replace("/proxy", "") + "' " + sb.toString() + " --data '" + body + "'");
            }
        } catch (Exception e) {
        }

        return null;
    }
}
