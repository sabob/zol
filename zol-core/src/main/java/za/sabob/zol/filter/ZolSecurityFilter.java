package za.sabob.zol.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ZolSecurityFilter implements Filter {

    private static ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    @Override
    public void init( FilterConfig filterConfig ) throws ServletException {

    }

    @Override
    public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain ) throws IOException, ServletException {

        HttpServletRequest req = ( HttpServletRequest ) servletRequest;
        HttpServletResponse resp = ( HttpServletResponse ) servletResponse;

        if ( isAuthorized( req, resp ) ) {
            chain.doFilter( servletRequest, servletResponse );
        } else {
            redirectToRoot( req, resp );
        }
    }

    public boolean isAuthorized( HttpServletRequest req, HttpServletResponse resp ) {
        String remoteUser = req.getRemoteUser();
        if ( remoteUser == null ) {
            return false;
        }
        return true;
    }

    @Override
    public void destroy() {
    }

    public void redirectToRoot( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
        String url = resp.encodeRedirectURL( req.getContextPath() );
        resp.sendRedirect( url );
    }

}

