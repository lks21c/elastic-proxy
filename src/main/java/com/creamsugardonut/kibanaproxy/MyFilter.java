package com.creamsugardonut.kibanaproxy;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import javax.servlet.http.HttpServletRequest;

public class MyFilter extends ZuulFilter {

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

        System.out.println("yesyes");

        // additional custom logic goes here

        // return isn't used in current impl, null is fine
        return null;
    }

}
