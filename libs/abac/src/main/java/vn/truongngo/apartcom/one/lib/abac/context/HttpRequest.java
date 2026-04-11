package vn.truongngo.apartcom.one.lib.abac.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import vn.truongngo.apartcom.one.lib.abac.utils.HttpUtils;
import vn.truongngo.apartcom.one.lib.abac.utils.ReflectionUtils;

import java.util.*;

/**
 * Structured abstraction of an HTTP request for ABAC evaluation.
 * @author Truong Ngo
 */
@Data
@Slf4j
public class HttpRequest {

    private Metadata metadata;
    private String method;
    private String contextPath;
    private String servletPattern;
    private String requestedURI;
    private String requestURL;
    private String servletPath;
    private Map<String, List<String>> headers;
    private Map<String, String> pathVariables;
    private Map<String, List<String>> queryParams;
    private Object requestBody;
    private Map<String, String> cookies;
    private Map<String, Object> session;

    public static HttpRequest parse(HttpServletRequest request) {
        HttpRequest httpRequest = new HttpRequest();
        httpRequest.metadata = Metadata.parse(request);
        httpRequest.method = request.getMethod();
        httpRequest.contextPath = request.getContextPath();
        httpRequest.servletPattern = HttpUtils.getServletPattern(request);
        httpRequest.requestedURI = request.getRequestURI();
        httpRequest.requestURL = request.getRequestURL().toString();
        httpRequest.servletPath = request.getServletPath();
        httpRequest.headers = HttpUtils.getHeaders(request);
        httpRequest.pathVariables = HttpUtils.getPathVariables(request);
        httpRequest.queryParams = HttpUtils.getQueryParameters(request);
        httpRequest.requestBody = HttpUtils.getSerializableRequestBody(request);
        httpRequest.cookies = HttpUtils.getAllCookies(request);
        httpRequest.session = HttpUtils.getAllSessionAttributes(request);
        return httpRequest;
    }

    public Object getMetaData(String key) {
        return metadata.getValue(key);
    }

    public List<String> getHeader(String key) {
        return headers.get(key);
    }

    public String getPathVariable(String key) {
        return pathVariables.get(key);
    }

    public List<String> getQueryParam(String key) {
        return queryParams.get(key);
    }

    public Object getRequestBody(String path) {
        return ReflectionUtils.getObjectValue(requestBody, path);
    }

    public String getCookie(String key) {
        return cookies.get(key);
    }

    public Object getSession(String key) {
        return session.get(key);
    }
}
