package com.creamsugardonut.kibanaproxy;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class MyFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(MyFilter.class);


    @Override
    public String filterType() {
        // can be pre, route, post, and error
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

        log.info("yesyes " + request.getQueryString());

        // additional custom logic goes here

        // return isn't used in current impl, null is fine
        return null;
    }

}
