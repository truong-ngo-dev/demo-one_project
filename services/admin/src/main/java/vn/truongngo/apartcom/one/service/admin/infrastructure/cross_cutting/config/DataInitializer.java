package vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserStatus;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression.AbacExpressionJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression.AbacExpressionJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy.PolicyJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy.PolicyJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy_set.PolicySetJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy_set.PolicySetJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.resource.ActionDefinitionJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.resource.ResourceDefinitionJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.resource.ResourceDefinitionJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.rule.RuleJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.rule.RuleJpaRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementType;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.uielement.UIElementJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.uielement.UIElementJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.role.RoleJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.role.RoleJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user.UserJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user.UserJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user.UserRoleContextJpaEntity;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Seeds initial data on startup:
 * - ADMIN role + admin user
 * - ABAC root PolicySet (scope=ADMIN, isRoot=true) with permit rule for ADMIN role
 * - Resource/Action definitions for all admin endpoints
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String ADMIN_ROLE_ID   = "00000000-0000-0000-0000-000000000001";
    private static final String ADMIN_ROLE_NAME = "ADMIN";
    private static final String ADMIN_USER_ID   = "00000000-0000-0000-0000-000000000002";
    private static final String ADMIN_EMAIL     = "admin@example.com";
    private static final String ADMIN_USERNAME  = "admin";
    private static final String ADMIN_PASSWORD  = "admin123";

    private static final String SERVICE_NAME = "admin-service";

    private final RoleJpaRepository roleJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final PolicySetJpaRepository policySetJpaRepository;
    private final PolicyJpaRepository policyJpaRepository;
    private final RuleJpaRepository ruleJpaRepository;
    private final AbacExpressionJpaRepository expressionJpaRepository;
    private final ResourceDefinitionJpaRepository resourceDefinitionJpaRepository;
    private final UIElementJpaRepository uiElementJpaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        RoleJpaEntity adminRole = seedAdminRole();
        seedAdminUser(adminRole);
        seedAbacPolicy();
        seedResources();
        seedRouteUIElements();
    }

    // ---- User / Role ----

    private RoleJpaEntity seedAdminRole() {
        return roleJpaRepository.findByName(ADMIN_ROLE_NAME).orElseGet(() -> {
            RoleJpaEntity role = new RoleJpaEntity();
            role.setId(ADMIN_ROLE_ID);
            role.setName(ADMIN_ROLE_NAME);
            role.setDescription("System administrator");
            role.setCreatedAt(Instant.now().toEpochMilli());
            log.info("[DataInitializer] Creating ADMIN role");
            return roleJpaRepository.save(role);
        });
    }

    private void seedAdminUser(RoleJpaEntity adminRole) {
        if (userJpaRepository.existsByEmail(ADMIN_EMAIL)) {
            return;
        }
        UserJpaEntity user = new UserJpaEntity();
        user.setId(ADMIN_USER_ID);
        user.setUsername(ADMIN_USERNAME);
        user.setEmail(ADMIN_EMAIL);
        user.setFullName("Administrator");
        user.setHashedPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());

        UserRoleContextJpaEntity adminContext = new UserRoleContextJpaEntity();
        adminContext.setUserId(ADMIN_USER_ID);
        adminContext.setScope(Scope.ADMIN);
        adminContext.setOrgId(""); // '' sentinel for null orgId in ADMIN scope
        adminContext.setRoles(Set.of(adminRole));
        user.getRoleContexts().add(adminContext);

        log.info("[DataInitializer] Creating admin user: {}", ADMIN_EMAIL);
        userJpaRepository.save(user);
    }

    // ---- ABAC Policy ----

    private void seedAbacPolicy() {
        List<PolicySetJpaEntity> existing = policySetJpaRepository.findAllByIsRootTrue();
        if (existing.stream().anyMatch(ps -> "ADMIN".equals(ps.getScope()))) {
            return;
        }

        long now = Instant.now().toEpochMilli();

        // PolicySet: admin-root
        PolicySetJpaEntity policySet = new PolicySetJpaEntity();
        policySet.setName("admin-root");
        policySet.setScope("ADMIN");
        policySet.setCombineAlgorithm("DENY_UNLESS_PERMIT");
        policySet.setRoot(true);
        policySet.setCreatedAt(now);
        policySet.setUpdatedAt(now);
        policySet = policySetJpaRepository.save(policySet);
        log.info("[DataInitializer] Created PolicySet admin-root (id={})", policySet.getId());

        // Policy: admin-access
        PolicyJpaEntity policy = new PolicyJpaEntity();
        policy.setPolicySetId(policySet.getId());
        policy.setName("admin-access");
        policy.setCombineAlgorithm("PERMIT_OVERRIDES");
        policy.setCreatedAt(now);
        policy.setUpdatedAt(now);
        policy = policyJpaRepository.save(policy);
        log.info("[DataInitializer] Created Policy admin-access (id={})", policy.getId());

        // Expression: subject.roles.contains('ADMIN')
        AbacExpressionJpaEntity conditionExpr = new AbacExpressionJpaEntity();
        conditionExpr.setType(AbacExpressionJpaEntity.ExpressionType.LITERAL);
        conditionExpr.setSpelExpression("subject.roles.contains('ADMIN')");
        conditionExpr = expressionJpaRepository.save(conditionExpr);

        // Rule: admin-permit
        RuleJpaEntity rule = new RuleJpaEntity();
        rule.setPolicyId(policy.getId());
        rule.setName("admin-permit");
        rule.setDescription("PERMIT access for users with ADMIN role");
        rule.setConditionExpressionId(conditionExpr.getId());
        rule.setEffect("PERMIT");
        rule.setOrderIndex(1);
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);
        rule = ruleJpaRepository.save(rule);
        log.info("[DataInitializer] Created Rule admin-permit (id={})", rule.getId());
    }

    // ---- Resources / Actions ----

    private void seedResources() {
        seedResource("user", "User management", List.of(
                "LIST", "READ", "CREATE", "LOCK", "UNLOCK", "ASSIGN_ROLE", "REMOVE_ROLE"));
        seedResource("role", "Role management", List.of(
                "LIST", "READ", "CREATE", "UPDATE", "DELETE"));
        seedResource("abac_policy_set", "ABAC PolicySet management", List.of(
                "LIST", "READ", "CREATE", "UPDATE", "DELETE"));
        seedResource("abac_policy", "ABAC Policy management", List.of(
                "LIST", "READ", "CREATE", "UPDATE", "DELETE"));
        seedResource("abac_rule", "ABAC Rule management", List.of(
                "LIST", "READ", "CREATE", "UPDATE", "DELETE"));
        seedResource("abac_resource", "ABAC Resource/Action definition", List.of(
                "LIST", "READ", "CREATE", "UPDATE", "DELETE"));
        seedResource("abac_ui_element", "ABAC UIElement registry", List.of(
                "LIST", "READ", "CREATE", "UPDATE", "DELETE"));
        seedResource("abac_simulate", "ABAC policy simulator", List.of("EXECUTE"));
        seedResource("abac_audit_log", "ABAC audit log", List.of("LIST"));
        seedResource("session", "Session management", List.of("LIST"));
    }

    private void seedResource(String name, String description, List<String> actions) {
        if (resourceDefinitionJpaRepository.existsByName(name)) {
            return;
        }
        long now = Instant.now().toEpochMilli();
        ResourceDefinitionJpaEntity resource = new ResourceDefinitionJpaEntity();
        resource.setName(name);
        resource.setDescription(description);
        resource.setServiceName(SERVICE_NAME);
        resource.setCreatedAt(now);
        resource.setUpdatedAt(now);

        for (String actionName : actions) {
            ActionDefinitionJpaEntity action = new ActionDefinitionJpaEntity();
            action.setResource(resource);
            action.setName(actionName);
            action.setStandard(true);
            resource.getActions().add(action);
        }

        resourceDefinitionJpaRepository.save(resource);
        log.info("[DataInitializer] Created resource '{}' with actions {}", name, actions);
    }

    // ---- Route UIElements ----

    private void seedRouteUIElements() {
        record RouteEntry(String elementId, String label, String resourceName, String actionName, int order) {}

        List<RouteEntry> entries = List.of(
                new RouteEntry("route:users",               "Users",           "user",            "LIST",    1),
                new RouteEntry("route:roles",               "Roles",           "role",            "LIST",    2),
                new RouteEntry("route:abac:resources",      "Resources",       "abac_resource",   "LIST",    3),
                new RouteEntry("route:abac:policy-sets",    "Policy Sets",     "abac_policy_set", "LIST",    4),
                new RouteEntry("route:abac:ui-elements",    "UI Elements",     "abac_ui_element", "LIST",    5),
                new RouteEntry("route:abac:simulator",      "Simulator",       "abac_simulate",   "EXECUTE", 6),
                new RouteEntry("route:abac:audit-log",      "Audit Log",       "abac_audit_log",  "LIST",    7),
                new RouteEntry("route:active-sessions",     "Sessions",        "session",         "LIST",    8),
                new RouteEntry("route:login-activities",    "Login Activities","session",         "LIST",    9),
                new RouteEntry("btn:user:lock",             "Lock/Unlock User","user",            "LOCK",    10)
        );

        for (RouteEntry entry : entries) {
            if (uiElementJpaRepository.existsByElementId(entry.elementId())) {
                continue;
            }
            ResourceDefinitionJpaEntity resource = resourceDefinitionJpaRepository
                    .findByName(entry.resourceName())
                    .orElse(null);
            if (resource == null) {
                log.warn("[DataInitializer] Resource '{}' not found — skipping UIElement '{}'",
                        entry.resourceName(), entry.elementId());
                continue;
            }
            ActionDefinitionJpaEntity action = resource.getActions().stream()
                    .filter(a -> a.getName().equals(entry.actionName()))
                    .findFirst()
                    .orElse(null);
            if (action == null) {
                log.warn("[DataInitializer] Action '{}' not found on resource '{}' — skipping UIElement '{}'",
                        entry.actionName(), entry.resourceName(), entry.elementId());
                continue;
            }

            UIElementJpaEntity uiElement = new UIElementJpaEntity();
            uiElement.setElementId(entry.elementId());
            uiElement.setLabel(entry.label());
            uiElement.setType(UIElementType.MENU_ITEM);
            uiElement.setScope(Scope.ADMIN);
            uiElement.setOrderIndex(entry.order());
            uiElement.setResourceId(resource.getId());
            uiElement.setActionId(action.getId());
            uiElementJpaRepository.save(uiElement);
            log.info("[DataInitializer] Created UIElement '{}' → {}/{}", entry.elementId(), entry.resourceName(), entry.actionName());
        }
    }
}
