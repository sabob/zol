package com.github.phillipkruger.stompee.util;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter( urlPatterns = "/*" )
public class ContextFilter implements Filter {

    private static ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    @Override
    public void init( FilterConfig filterConfig ) throws ServletException {

    }

    @Override
    public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain ) throws IOException, ServletException {

        try {


            HttpServletRequest req = ( HttpServletRequest ) servletRequest;
            HttpServletResponse resp = ( HttpServletResponse ) servletResponse;

            String remoteUser = req.getRemoteUser();
            Context ctx = new Context();
            ctx.setRemoteUser( remoteUser );

            bindThreadLocalContext( ctx );

            chain.doFilter( servletRequest, servletResponse );

        } finally {
            bindThreadLocalContext( null );

        }
    }

    @Override
    public void destroy() {
    }

    public static boolean hasThreadLocalContext() {
        return getThreadLocalContext() != null;
    }

    public static Context getThreadLocalContext() {
        return CONTEXT.get();
    }

    public static void bindThreadLocalContext( Context ctx ) {
        CONTEXT.set( ctx );
    }
}
