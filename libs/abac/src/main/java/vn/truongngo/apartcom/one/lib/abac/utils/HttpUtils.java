package vn.truongngo.apartcom.one.lib.abac.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.springframework.web.servlet.HandlerMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for HTTP-related operations.
 * @author Truong Ngo
 */
public class HttpUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    public static String getUrl(HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    public static String getQueryString(HttpServletRequest request) {
        return request.getQueryString();
    }

    public static String getFullUrl(HttpServletRequest request) {
        return getUrl(request) + "?" + getQueryString(request);
    }

    public static String getUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    public static String getUriPattern(HttpServletRequest request) {
        return getContextPath(request) + getServletPattern(request);
    }

    public static String getContextPath(HttpServletRequest request) {
        return request.getContextPath();
    }

    public static String getServletPattern(HttpServletRequest request) {
        return (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    }

    public static Map<String, List<String>> parseQueryString(String queryString) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        if (Objects.nonNull(queryString)) {
            String decodeString = URLDecoder.decode(queryString, StandardCharsets.UTF_8);
            String[] params = decodeString.split("&");
            for (String param : params) {
                String[] pair = param.split("=", 2);
                String key = pair[0];
                String value = pair.length > 1 ? pair[1] : null;
                result.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }
        return result;
    }

    public static String parseRequestBody(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = request.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read request body: " + e.getMessage());
        }
        return sb.toString();
    }

    public static Object parseJsonBody(String body) {
        try {
            return new ObjectMapper().readValue(body, Object.class);
        } catch (JacksonException e) {
            log.error("Cannot deserialize JSON request body: {}", e.getMessage());
            throw new IllegalStateException("Cannot deserialize JSON request body: " + e.getMessage());
        }
    }

    public static Map<String, List<String>> getHeaders(HttpServletRequest request) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, Collections.list(request.getHeaders(headerName)));
        }
        return headers;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getPathVariables(HttpServletRequest request) {
        return (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }

    public static Map<String, List<String>> getQueryParameters(HttpServletRequest request) {
        return parseQueryString(getQueryString(request));
    }

    public static Collection<Part> getRequestParts(HttpServletRequest request) {
        try {
            return request.getParts();
        } catch (ServletException | IOException e) {
            log.error("Can not parse request parts: {}", e.getMessage());
            return null;
        }
    }

    public static Object getSerializableRequestBody(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (Objects.nonNull(contentType) && contentType.contains("multipart/form-data")) {
            Collection<Part> parts = getRequestParts(request);
            if (Objects.isNull(parts) || parts.isEmpty()) return null;
            return parts.stream().map(HttpUtils::getPartMetadata).toList();
        }
        String body = parseRequestBody(request);
        if (body.trim().isEmpty()) {
            return null;
        }
        if (Objects.nonNull(contentType) && contentType.contains("application/x-www-form-urlencoded")) {
            return parseQueryString(body);
        }
        return parseJsonBody(body);
    }

    public static Map<String, String> getAllCookies(HttpServletRequest request) {
        Map<String, String> cookiesMap = new LinkedHashMap<>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookiesMap.put(cookie.getName(), cookie.getValue());
            }
        }
        return cookiesMap;
    }

    public static Map<String, Object> getAllSessionAttributes(HttpServletRequest request) {
        Map<String, Object> sessionAttributes = new LinkedHashMap<>();
        HttpSession session = request.getSession(false);
        if (session != null) {
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                sessionAttributes.put(attributeName, session.getAttribute(attributeName));
            }
        }
        return sessionAttributes;
    }

    public static Map<String, Object> getPartMetadata(Part part) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("contentType", part.getContentType());
        metadata.put("headers", part.getHeaderNames().stream().collect(Collectors.toMap(h -> h, part::getHeader)));
        metadata.put("name", part.getName());
        if (Objects.isNull(part.getSubmittedFileName())) {
            try (InputStream is = part.getInputStream()) {
                metadata.put("value", new String(is.readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.error("Can not read part: {}", e.getMessage());
                throw new IllegalStateException("Can not read part: " + e.getMessage());
            }
        } else {
            metadata.put("fileName", part.getSubmittedFileName());
            metadata.put("size", part.getSize());
            metadata.put("encoding", part.getHeader("Content-Transfer-Encoding"));
            metadata.put("contentDisposition", part.getHeader("Content-Disposition"));
            metadata.put("extension", getFileExtension(part.getSubmittedFileName()));
        }
        return metadata;
    }

    private static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index > 0 ? fileName.substring(index + 1).toLowerCase() : "";
    }
}
