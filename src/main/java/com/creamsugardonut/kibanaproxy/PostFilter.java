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

public class PostFilter extends ZuulFilter {
    private static Logger logger = LoggerFactory.getLogger(PostFilter.class);


    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();

        if (ctx.getResponseDataStream() != null) {
            try (final InputStream responseDataStream = ctx.getResponseDataStream()) {
                final String responseData = CharStreams.toString(new InputStreamReader(responseDataStream, "UTF-8"));
                ctx.setResponseBody(responseData);
            } catch (IOException e) {
                System.out.println("Error reading body" + e);
            }

            System.out.println("response body = " + ctx.getResponseBody());
            System.out.println();
        }

        return null;
    }
}
