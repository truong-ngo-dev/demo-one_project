package vn.truongngo.apartcom.one.service.oauth2.infrastructure.crosscutting.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public class IpAddressExtractor {

    private static final List<String> IP_HEADERS = List.of(
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    );

    public static String extract(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();  // lấy IP đầu tiên trong chain
            }
        }
        return request.getRemoteAddr();  // fallback nếu không có proxy
    }
}
