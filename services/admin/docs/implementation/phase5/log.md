# Phase 5 Log — Auth Context Query

## Status: COMPLETED
`mvn clean compile -DskipTests` — BUILD SUCCESS

---

## Files created

| File | Notes |
|------|-------|
| `application/auth/get_contexts/GetUserContexts.java` | Query handler + `ContextView` record |
| `presentation/auth/AuthContextController.java` | `GET /api/v1/auth/contexts?userId=` |

## Endpoint

| Method | Path | Params | Response |
|--------|------|--------|----------|
| `GET` | `/api/v1/auth/contexts` | `?userId=` (required) | `200 { data: List<ContextView> }` |

## ContextView fields
```java
record ContextView(
    Long contextId,
    Scope scope,
    String orgId,       // null for ADMIN
    OrgType orgType,    // null for ADMIN
    String displayName, // "Admin Portal" | building name | org name | orgId fallback
    List<String> roleIds
)
```

## displayName resolution
- `ADMIN` → `"Admin Portal"`
- `OPERATOR` / `RESIDENT` → `buildingReferenceRepository.findById(orgId).getName()` or fallback `orgId`
- `TENANT` → `orgReferenceRepository.findById(orgId).getName()` or fallback `orgId`

## Deviations
None.
