package vn.truongngo.apartcom.one.lib.abac.pep;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.abac.context.Action;
import vn.truongngo.apartcom.one.lib.abac.context.Environment;
import vn.truongngo.apartcom.one.lib.abac.context.Resource;
import vn.truongngo.apartcom.one.lib.abac.context.Subject;
import vn.truongngo.apartcom.one.lib.abac.domain.AbstractPolicy;
import vn.truongngo.apartcom.one.lib.abac.exception.AuthorizationException;
import vn.truongngo.apartcom.one.lib.abac.pdp.AuthzDecision;
import vn.truongngo.apartcom.one.lib.abac.pdp.AuthzRequest;
import vn.truongngo.apartcom.one.lib.abac.pip.PipEngine;
import vn.truongngo.apartcom.one.lib.abac.rap.ResourceMapping;

import java.lang.reflect.Method;

/**
 * Aspect that enforces authorization policies for methods annotated with @PreEnforce and @PostEnforce.
 * When @ResourceMapping is present on the method, resource and action names are derived from the annotation.
 * Otherwise, falls back to extracting the resource name from the URL path.
 * @author Truong Ngo
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

    private final HttpServletRequest request;
    private final PepEngine pepEngine;
    private final PipEngine pipEngine;

    @Value("${spring.application.name}")
    private String serviceName;

    @Before(value = "@annotation(vn.truongngo.apartcom.one.lib.abac.pep.PreEnforce)")
    public void preEnforce(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        PreEnforce enforcer = method.getAnnotation(PreEnforce.class);
        String[] ignoredPath = enforcer.ignore();
        AuthzRequest authzRequest = prepareAuthzRequest(method);
        AuthzDecision decision = pepEngine.enforce(authzRequest, ignoredPath);
        if (decision.isDeny())
            throw new AuthorizationException("Forbidden", decision.getDetails(), decision.getTimestamp());
    }

    @AfterReturning(value = "@annotation(vn.truongngo.apartcom.one.lib.abac.pep.PostEnforce)", returning = "returnObject")
    public void postEnforce(JoinPoint joinPoint, Object returnObject) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        PostEnforce enforcer = method.getAnnotation(PostEnforce.class);
        String[] ignoredPath = enforcer.ignore();
        AuthzRequest authzRequest = prepareAuthzRequest(method);
        authzRequest.getObject().setData(returnObject);
        AuthzDecision decision = pepEngine.enforce(authzRequest, ignoredPath);
        if (decision.isDeny())
            throw new AuthorizationException("Forbidden", decision.getDetails(), decision.getTimestamp());
    }

    public AuthzRequest prepareAuthzRequest(Method method) {
        AbstractPolicy policy = pipEngine.getPolicy(serviceName);
        Subject subject = pipEngine.getSubject(request.getUserPrincipal());
        Environment environment = pipEngine.getEnvironment(serviceName);
        Resource resource = new Resource();
        Action action;

        ResourceMapping resourceMapping = method.getAnnotation(ResourceMapping.class);
        if (resourceMapping != null) {
            resource.setName(resourceMapping.resource());
            action = Action.semantic(resourceMapping.action());
        } else {
            action = new Action(request);
            String path = action.getRequest().getRequestedURI();
            String resourceName = pipEngine.getResourceName(path);
            resource.setName(resourceName);
        }

        return new AuthzRequest(subject, resource, action, environment, policy);
    }
}
