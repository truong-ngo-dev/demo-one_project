# Admin Service — IAM Design

## 1. Trách nhiệm

Quản lý **identity, authentication context, và access control** — ai được phép làm gì, trong phạm vi nào. Không biết về business domain (Party, FixedAsset, Agreement). Chỉ lưu reference ID từ các service khác, không import domain logic.

---

## 2. Domain Model

### 2.1 User

Đại diện cho một tài khoản trong hệ thống. Liên kết 1-1 với Person trong party-service thông qua `party_id`.

```
User
├── party_id (nullable) → ref party-service
│   null     = SUPER_ADMIN hoặc system/service account
│   not null = human user có Party profile
└── RoleContext[] (1-N)
```

**Rule:** Nếu User được assign RoleContext có `scope != ADMIN` thì `party_id` bắt buộc phải có.

**SUPER_ADMIN cross-portal:** SUPER_ADMIN chỉ ở ADMIN portal (party_id = null, không thể có RoleContext scope khác). Muốn vào portal khác → tạo user riêng với party_id + RoleContext tương ứng.

### 2.2 Role

Role do admin định nghĩa qua CRUD. Scoped theo portal — role chỉ có ý nghĩa trong scope nó thuộc về.

```
Role { id, name, scope }

Ví dụ:
  BQL_MANAGER    → scope=OPERATOR
  BQL_FINANCE    → scope=OPERATOR
  BQL_TECHNICAL  → scope=OPERATOR
```

### 2.3 RoleContext

Một "context" mà User có thể switch vào. Mỗi context xác định portal và phạm vi dữ liệu.

```
RoleContext { scope, orgId, orgType, roles[], status }
```

**orgId semantic theo scope:**

| Scope | orgId | orgType |
|-------|-------|---------|
| `ADMIN` | `null` | `null` |
| `OPERATOR` | `FixedAsset.id` (BUILDING) | `FIXED_ASSET` |
| `TENANT` | `Party.id` (Organization) | `PARTY` |
| `RESIDENT` | `FixedAsset.id` (UNIT) | `FIXED_ASSET` |

`orgId` là **scope dimension** — định nghĩa ranh giới dữ liệu, không phải identity của user.

### 2.4 TenantSubRoleAssignment

Layer 2 của TENANT permission model. TENANT_ADMIN assign feature-level roles cho member trong org của mình. Tách biệt hoàn toàn với RoleContext (layer 1 — portal access do BQL own).

```
TenantSubRoleAssignment { userId, orgId, subRole, assignedBy, assignedAt }
```

Sub-roles platform define sẵn: `TENANT_MANAGER`, `TENANT_FINANCE`, `TENANT_HR`

### 2.5 Reference Cache

Admin service cache reference từ các service khác để validate mà không cần sync call:
- `building_reference` — populate từ `BuildingCreated`
- `org_reference` — populate từ `OrganizationCreated` (BQL Org)

---

## 3. Schema

```sql
CREATE TABLE user (
    id          VARCHAR(36) PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    status      ENUM('ACTIVE', 'LOCKED', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    party_id    VARCHAR(36),                        -- ref → party-service (nullable)
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME NOT NULL
);

CREATE TABLE role (
    id      VARCHAR(36) PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    scope   ENUM('ADMIN', 'OPERATOR', 'TENANT', 'RESIDENT') NOT NULL,
    UNIQUE KEY uq_role_name_scope (name, scope)
);

CREATE TABLE role_context (
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL,
    scope       ENUM('ADMIN', 'OPERATOR', 'TENANT', 'RESIDENT') NOT NULL,
    org_id      VARCHAR(36),
    org_type    ENUM('PARTY', 'FIXED_ASSET'),
    status      ENUM('ACTIVE', 'REVOKED') NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE role_context_role (
    role_context_id VARCHAR(36) NOT NULL,
    role_id         VARCHAR(36) NOT NULL,
    PRIMARY KEY (role_context_id, role_id),
    FOREIGN KEY (role_context_id) REFERENCES role_context(id),
    FOREIGN KEY (role_id)         REFERENCES role(id)
);

-- Layer 2: TENANT_ADMIN owns, tách biệt với role_context
CREATE TABLE tenant_sub_role_assignment (
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL,
    org_id      VARCHAR(36) NOT NULL,              -- ref → party-service (Tenant Org)
    sub_role    ENUM('TENANT_MANAGER', 'TENANT_FINANCE', 'TENANT_HR') NOT NULL,
    assigned_by VARCHAR(36) NOT NULL,              -- ref → user (phải là TENANT_ADMIN của org đó)
    assigned_at DATETIME NOT NULL,
    UNIQUE KEY uq_sub_role (user_id, org_id, sub_role),
    FOREIGN KEY (user_id)     REFERENCES user(id),
    FOREIGN KEY (assigned_by) REFERENCES user(id)
);

-- Reference cache
CREATE TABLE building_reference (
    building_id     VARCHAR(36) PRIMARY KEY,        -- ref → property-service
    name            VARCHAR(255) NOT NULL,
    managing_org_id VARCHAR(36),                    -- ref → party-service
    cached_at       DATETIME NOT NULL
);

CREATE TABLE org_reference (
    org_id      VARCHAR(36) PRIMARY KEY,            -- ref → party-service (BQL Org)
    name        VARCHAR(255) NOT NULL,
    org_type    VARCHAR(20) NOT NULL,
    cached_at   DATETIME NOT NULL
);
```

---

## 4. Event Consumers

### 4.1 BuildingCreated (from property-service)

```
Nhận: { buildingId, name, managingOrgId }
Xử lý: Upsert building_reference
Mục đích: Validate orgId khi assign OPERATOR RoleContext mà không cần sync call
```

### 4.2 OrganizationCreated (from party-service)

```
Nhận: { partyId, name, orgType }
Xử lý: IF orgType = BQL → Upsert org_reference
Mục đích: Validate BQL Org tồn tại khi bootstrap
```

### 4.3 OccupancyAgreementActivated (from property-service)

```
Nhận: { agreementId, partyId, partyType, assetId, agreementType }
Xử lý:
  IF agreementType = OWNERSHIP AND partyType = PERSON
    → tìm User có party_id = partyId
    → tạo RoleContext { RESIDENT, orgId=assetId, orgType=FIXED_ASSET }

  IF agreementType = LEASE AND partyType = PERSON
    → tìm User có party_id = partyId
    → tạo RoleContext { RESIDENT, orgId=assetId, orgType=FIXED_ASSET }

  IF agreementType = LEASE AND partyType = HOUSEHOLD
    → query party-service: GET /internal/parties/{partyId}/members → [personId...]
    → với mỗi member: tìm User, tạo RoleContext { RESIDENT, orgId=assetId }

  IF agreementType = LEASE AND partyType = ORGANIZATION
    → query party-service: GET /internal/parties/{partyId}/members → [personId...]
    → với member đầu tiên (TENANT_ADMIN): tìm User, tạo RoleContext { TENANT, orgId=partyId, orgType=PARTY }
```

### 4.4 OccupancyAgreementTerminated (from property-service)

```
Nhận: { agreementId, partyId, partyType, assetId, agreementType }
Xử lý:
  IF agreementType = OWNERSHIP OR (agreementType = LEASE AND partyType IN (PERSON, HOUSEHOLD))
    → revoke tất cả RoleContext WHERE org_id=assetId AND scope=RESIDENT

  IF agreementType = LEASE AND partyType = ORGANIZATION
    → revoke tất cả RoleContext WHERE org_id=partyId AND scope=TENANT
    → revoke tất cả TenantSubRoleAssignment WHERE org_id=partyId
```

### 4.5 EmploymentTerminated (from party-service)

```
Nhận: { employmentId, personId, orgId }
Xử lý:
  → tìm User có party_id = personId
  → revoke RoleContext { scope=OPERATOR } của User đó WHERE org_id match building của orgId
```

---

## 5. Use Cases

### ADMIN portal — bootstrap

| Use case | Actor | Flow |
|----------|-------|------|
| Tạo User + assign OPERATOR | SUPER_ADMIN | Tạo User, validate building exists (cache), tạo RoleContext { OPERATOR, buildingId } |
| CRUD Role | SUPER_ADMIN | Tạo/sửa/xoá Role theo scope |

### OPERATOR portal — nhân sự

| Use case | Actor | Flow |
|----------|-------|------|
| Tạo User cho BQL staff | BQL_MANAGER | Tạo User, link party_id = Person đã tạo ở party-service |
| Assign OPERATOR RoleContext | BQL_MANAGER | Tạo RoleContext { OPERATOR, buildingId }, attach roles[] |
| Revoke OPERATOR RoleContext | BQL_MANAGER | Set RoleContext.status = REVOKED |

### TENANT portal — sub-role management

| Use case | Actor | Flow |
|----------|-------|------|
| Assign sub-role cho member | TENANT_ADMIN | Tạo TenantSubRoleAssignment { userId, orgId, subRole } — validate user là member của org |
| Revoke sub-role | TENANT_ADMIN | Xoá TenantSubRoleAssignment tương ứng |
| Xem sub-roles của org | TENANT_ADMIN | Query tenant_sub_role_assignment WHERE org_id = subject.orgId |

### Auth flow — context selection

| Use case | Actor | Flow |
|----------|-------|------|
| Lấy danh sách contexts | User | GET /auth/contexts → RoleContext[] kèm displayName (resolve từ cache) |
| Switch context | User | Chọn RoleContext → issue token với claims { scope, orgId, roles, subRoles } |

---

## 6. Business Rules

1. 1 User chỉ có 1 active RoleContext cho mỗi `(scope, orgId)` combination
2. `scope != ADMIN` → `party_id` trên User bắt buộc trước khi assign RoleContext
3. `scope = OPERATOR` → validate `orgId` tồn tại trong `building_reference`
4. `scope = RESIDENT` → chỉ tạo qua event `OccupancyAgreementActivated`, không tạo thủ công
5. `scope = TENANT` → chỉ tạo qua event `OccupancyAgreementActivated`, không tạo thủ công
6. Role chỉ được attach vào RoleContext có cùng `scope`
7. `TenantSubRoleAssignment.assigned_by` phải là User có RoleContext `{ scope=TENANT, org_id=orgId }` với role TENANT_ADMIN
8. TENANT_ADMIN chỉ assign sub-role cho User có active RoleContext `{ scope=TENANT, org_id = subject.orgId }`

---

## 7. Dependency

Admin service **consume events** từ:
- `property-service` → `BuildingCreated`, `OccupancyAgreementActivated`, `OccupancyAgreementTerminated`
- `party-service` → `OrganizationCreated`, `EmploymentTerminated`

Admin service **query** (sync, read-only) sang:
- `party-service` → resolve members của Household/Organization khi xử lý `OccupancyAgreementActivated`
