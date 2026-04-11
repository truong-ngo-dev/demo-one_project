package vn.truongngo.apartcom.one.lib.abac.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that wraps the incoming HttpServletRequest in a CacheBodyHttpServletRequest
 * to allow the request body to be read multiple times.
 * @author Truong Ngo
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CacheRequestBodyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        CacheBodyHttpServletRequest cacheBodyHttpServletRequest = new CacheBodyHttpServletRequest(request);
        filterChain.doFilter(cacheBodyHttpServletRequest, response);
    }
}
