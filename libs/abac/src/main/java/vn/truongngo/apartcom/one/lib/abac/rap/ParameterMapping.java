package vn.truongngo.apartcom.one.lib.abac.rap;

import lombok.Data;

/**
 * Represents the mapping of a method parameter to its corresponding HTTP source.
 * @author Truong Ngo
 */
@Data
public class ParameterMapping {

    private String parameterName;
    private HttpSource source;
    private Class<?> parameterType;
    private Integer index;

    public enum HttpSource {
        QUERY_PARAM,
        PATH_VARIABLE,
        REQUEST_BODY,
        REQUEST_HEADER,
        COOKIE,
        SESSION
    }
}
