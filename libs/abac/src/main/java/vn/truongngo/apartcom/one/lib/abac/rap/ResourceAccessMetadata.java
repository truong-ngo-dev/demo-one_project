package vn.truongngo.apartcom.one.lib.abac.rap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * Metadata for resource access in the ABAC system.
 * @author Truong Ngo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceAccessMetadata {

    private Class<?> resourceType;
    private String pathTemplate;
    private HttpMethod httpMethod;
    private Class<?> accessor;
    private String accessorMethod;
    private List<ParameterMapping> parameterMappings;
}
