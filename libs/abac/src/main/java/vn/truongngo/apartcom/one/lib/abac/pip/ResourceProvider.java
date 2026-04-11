package vn.truongngo.apartcom.one.lib.abac.pip;

// Kept for future reference — pull-based resource fetching approach (not currently used)

//import vn.truongngo.apartcom.one.lib.abac.rap.ParameterMapping;
//import vn.truongngo.apartcom.one.lib.abac.rap.ResourceAccessMetadata;
//import vn.truongngo.apartcom.one.lib.abac.rap.ResourceAccessPoint;
//import vn.truongngo.apartcom.one.lib.abac.rap.ResourceAccessConfig;
//import vn.truongngo.apartcom.one.lib.abac.context.Action;
//import vn.truongngo.apartcom.one.lib.abac.context.HttpRequest;
//import vn.truongngo.apartcom.one.lib.abac.context.Resource;
//import vn.truongngo.apartcom.one.lib.abac.utils.StringUtils;
//import vn.truongngo.apartcom.one.lib.abac.utils.TypeUtils;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.ApplicationContext;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.Comparator;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//
///**
// * Provides resources by extracting and processing data from HTTP requests.
// * @author Truong Ngo
// */
//@Slf4j
//@RequiredArgsConstructor
//public class ResourceProvider {
//
//    @Getter
//    private final ResourceAccessConfig resourceAccessConfig;
//    private final ResourceAccessPoint resourceAccessPoint;
//    private final ApplicationContext applicationContext;
//
//    public Resource getResource(Action action) {
//        Resource resource = new Resource();
//        String path = action.getRequest().getRequestedURI();
//        String resourceName = getResourceName(path);
//        resource.setName(resourceName);
//
//        if (!isResourceDataNeeded(path, action)) return resource;
//
//        String resourceKey = getResourceAccessMetadataKey(action);
//        ResourceAccessMetadata metadata = resourceAccessPoint.getResourceAccessMetadata(resourceKey);
//
//        Class<?>[] parameterTypes = metadata.getParameterMappings().stream()
//                .sorted(Comparator.comparing(ParameterMapping::getIndex))
//                .map(ParameterMapping::getParameterType)
//                .toArray(Class<?>[]::new);
//
//        Object[] extractedParameterValues = metadata.getParameterMappings().stream()
//                .sorted(Comparator.comparing(ParameterMapping::getIndex))
//                .map(mp -> extractHttpValue(action, mp.getParameterName(), mp.getSource()))
//                .toArray(Object[]::new);
//
//        Object[] parameterValues = new Object[parameterTypes.length];
//        for (int i = 0; i < parameterTypes.length; i++) {
//            Object parameterValue = extractedParameterValues[i];
//            Class<?> parameterType = parameterTypes[i];
//            if (parameterValue.getClass() == parameterType) {
//                parameterValues[i] = parameterValue;
//            } else {
//                parameterValues[i] = TypeUtils.castValueAs(parameterValue.toString(), parameterType);
//            }
//        }
//
//        try {
//            Method method = metadata.getAccessor().getMethod(metadata.getAccessorMethod(), parameterTypes);
//            Object accessor = applicationContext.getBean(metadata.getAccessor());
//            Object data = method.invoke(accessor, parameterValues);
//            resource.setData(data);
//            return resource;
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException("Error invoking method for resource access", e);
//        }
//    }
//
//    public String getResourceName(String path) {
//        Pattern pattern = Pattern.compile(resourceAccessConfig.getResourceNameExtractor());
//        Matcher matcher = pattern.matcher(path);
//        return matcher.find() ? matcher.group(1) : "";
//    }
//
//    public String getResourceAccessMetadataKey(Action action) {
//        String path = action.getRequest().getRequestedURI();
//        String pathTemplate = getPathTemplate(path, action);
//        String method = action.getRequest().getMethod();
//        return String.format("%s_%s", method, pathTemplate);
//    }
//
//    public boolean isResourceDataNeeded(String path, Action action) {
//        return resourceAccessConfig.getIgnoredPaths()
//                .stream().anyMatch(s -> matchPath(path, s, action));
//    }
//
//    @SuppressWarnings("all")
//    public String getPathTemplate(String path, Action action) {
//        return resourceAccessConfig.getIgnoredPaths()
//                .stream().filter(s -> matchPath(path, s, action)).findFirst().orElse(null);
//    }
//
//    private boolean matchPath(String path, String template, Action action) {
//        boolean match = StringUtils.matchUrlPath(template, path);
//        boolean hasPathVariable = StringUtils.hasPathVariable(template);
//        if (!match) return false;
//        if (hasPathVariable) return !action.getRequest().getPathVariables().isEmpty();
//        else return true;
//    }
//
//    public Object extractHttpValue(Action action, String key, ParameterMapping.HttpSource source) {
//        HttpRequest request = action.getRequest();
//        return switch (source) {
//            case COOKIE -> request.getCookie(key);
//            case SESSION -> request.getSession(key);
//            case QUERY_PARAM -> request.getQueryParam(key);
//            case PATH_VARIABLE -> request.getPathVariable(key);
//            case REQUEST_HEADER -> request.getHeader(key);
//            case REQUEST_BODY -> request.getRequestBody(key);
//        };
//    }
//}
