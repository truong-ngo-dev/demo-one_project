package vn.truongngo.apartcom.one.lib.common.infrastructure.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.truongngo.apartcom.one.lib.common.domain.context.RequestContextHolder;

import java.io.IOException;

public class RequestContextFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestContextFilter.class);

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        String uri = ((HttpServletRequest) request).getRequestURI();
        try {
            RequestContextHolder.init();
            log.debug("RequestContext initialized for [{}]", uri);
            chain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
            log.debug("RequestContext cleared for [{}]", uri);
        }
    }
}