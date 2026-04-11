package vn.truongngo.apartcom.one.lib.abac.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import vn.truongngo.apartcom.one.lib.abac.utils.ReflectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Represents metadata associated with an HTTP request.
 * @author Truong Ngo
 */
@Data
public class Metadata {

    private String characterEncoding;
    private String protocol;
    private String scheme;
    private String serverName;
    private int serverPort;
    private String remoteAddress;
    private String remoteHost;
    private int remotePort;
    private boolean isSecure;
    private String localName;
    private String localAddress;
    private int localPort;
    private String requestedSessionId;
    private Locale locale;
    private List<Locale> locales;

    public static Metadata parse(HttpServletRequest request) {
        Metadata metadata = new Metadata();
        metadata.setCharacterEncoding(request.getCharacterEncoding());
        metadata.setProtocol(request.getProtocol());
        metadata.setScheme(request.getScheme());
        metadata.setServerName(request.getServerName());
        metadata.setServerPort(request.getServerPort());
        metadata.setRemoteAddress(request.getRemoteAddr());
        metadata.setRemoteHost(request.getRemoteHost());
        metadata.setRemotePort(request.getRemotePort());
        metadata.setSecure(request.isSecure());
        metadata.setLocalName(request.getLocalName());
        metadata.setLocalAddress(request.getLocalAddr());
        metadata.setLocalPort(request.getLocalPort());
        metadata.setRequestedSessionId(request.getRequestedSessionId());
        metadata.setLocale(request.getLocale());
        metadata.setLocales(Collections.list(request.getLocales()));
        return metadata;
    }

    public Object getValue(String key) {
        return ReflectionUtils.getFieldValue(this, key);
    }
}
