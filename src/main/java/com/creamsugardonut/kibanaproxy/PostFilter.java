package com.creamsugardonut.kibanaproxy;

import com.google.common.io.CharStreams;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
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
//        System.out.println("postpost");
//        RequestContext ctx = RequestContext.getCurrentContext();
//
//        try (final InputStream responseDataStream = ctx.getResponseDataStream()) {
//            if (responseDataStream != null) {
//                final String responseData = CharStreams.toString(new InputStreamReader(responseDataStream));
//                System.out.println("uri = " + ctx.getRequest().getRequestURI());
//                for (String resName : ctx.getResponse().getHeaderNames()) {
//                    System.out.println("res header = " + resName + " " + ctx.getResponse().getHeader(resName));
//                }
//                System.out.println("responseData = " + responseData);
//                ctx.setResponseBody(responseData);
//            }
//        } catch (IOException e) {
//            logger.warn("Error reading body", e);
//        }

        return null;
    }
}
