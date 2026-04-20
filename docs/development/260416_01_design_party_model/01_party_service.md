# Party Service — Design

## 1. Trách nhiệm

Quản lý **identity của các tác nhân** trong hệ thống — ai là ai, quan hệ giữa họ, và thông tin định danh pháp lý. Không biết về FixedAsset, RoleContext, hay Agreement.

---

## 2. Domain Model

### 2.1 Party Hierarchy

```
Party (AR)          — core identity: type, name, status, PartyIdentification
Person (AR riêng)   — share ID với Party — cá nhân
Organization (AR)   — share ID với Party — pháp nhân (BQL Org, Tenant Org, Vendor)
Household (AR)      — share ID với Party — hộ gia đình (informal group, mirrors hộ khẩu VN)
```

**Nguyên tắc:**
- `Party`, `Person`, `Organization`, `Household` là các **Aggregate Root riêng biệt** — mỗi cái có Repository và Command Handler riêng
- Share cùng primary key (composition + shared ID) — không phải single-table inheritance
- `CreatePerson` tạo cả `Party` lẫn `Person` atomic tại **application layer** (cùng transaction)
- Household ≠ Organization: không có pháp nhân, không đăng ký kinh doanh

### 2.2 PartyRelationship và Employment

`PartyRelationship` và `Employment` là 2 Aggregate Root riêng biệt.

```
PartyRelationship (AR) — thin, chỉ track kết nối giữa 2 Party
Employment (AR riêng)  — nghiệp vụ HR, linked tới PartyRelationship qua FK
  └── PositionAssignment — list chức vụ, support 1 người kiêm nhiều vị trí đồng thời
```

**Relationship types:**

| Type | From | To | Employment? |
|------|------|----|-------------|
| `MEMBER_OF` | Person | Household | Không |
| `MEMBER_OF` | Person | Organization (Tenant) | Không |
| `EMPLOYED_BY` | Person | Organization (BQL) | Có |

**Lý do chỉ BQL có Employment:** Hệ thống chỉ quản lý HR của BQL. Tenant Org và Household membership chỉ cần biết "ai thuộc về đâu".

**Terminate employment ≠ xóa PartyRelationship** — relationship record giữ lại như audit trail. Employment.status = TERMINATED.

### 2.3 PartyIdentification

Lưu thông tin định danh pháp lý của Party — tách bảng để 1 Party có thể có nhiều loại ID.

---

## 3. Schema

```sql
-- Supertype
CREATE TABLE party (
    id          VARCHAR(36) PRIMARY KEY,
    type        ENUM('PERSON', 'ORGANIZATION', 'HOUSEHOLD') NOT NULL,
    name        VARCHAR(255) NOT NULL,
    status      ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME NOT NULL
);

-- Subtypes
CREATE TABLE person (
    party_id    VARCHAR(36) PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    dob         DATE,
    gender      ENUM('MALE', 'FEMALE', 'OTHER'),
    FOREIGN KEY (party_id) REFERENCES party(id)
);

CREATE TABLE organization (
    party_id        VARCHAR(36) PRIMARY KEY,
    org_type        ENUM('BQL', 'TENANT', 'VENDOR', 'OTHER') NOT NULL,
    tax_id          VARCHAR(20),
    registration_no VARCHAR(50),
    FOREIGN KEY (party_id) REFERENCES party(id)
);

CREATE TABLE household (
    party_id        VARCHAR(36) PRIMARY KEY,
    head_person_id  VARCHAR(36) NOT NULL,
    FOREIGN KEY (party_id)       REFERENCES party(id),
    FOREIGN KEY (head_person_id) REFERENCES person(party_id)
);

-- Relationship
CREATE TABLE party_relationship (
    id              VARCHAR(36) PRIMARY KEY,
    from_party_id   VARCHAR(36) NOT NULL,
    to_party_id     VARCHAR(36) NOT NULL,
    type            ENUM('MEMBER_OF', 'EMPLOYED_BY') NOT NULL,
    from_role       ENUM('MEMBER', 'HEAD', 'EMPLOYEE', 'EMPLOYER') NOT NULL,
    to_role         ENUM('MEMBER', 'HEAD', 'EMPLOYEE', 'EMPLOYER') NOT NULL,
    status          ENUM('ACTIVE', 'ENDED') NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE NOT NULL,
    end_date        DATE,
    FOREIGN KEY (from_party_id) REFERENCES party(id),
    FOREIGN KEY (to_party_id)   REFERENCES party(id)
);

-- Employment AR (tạo khi và chỉ khi party_relationship.type=EMPLOYED_BY với BQL Org)
CREATE TABLE employment (
    id              VARCHAR(36) PRIMARY KEY,
    relationship_id VARCHAR(36) NOT NULL UNIQUE,
    employee_id     VARCHAR(36) NOT NULL,
    org_id          VARCHAR(36) NOT NULL,
    employment_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT') NOT NULL,
    status          ENUM('ACTIVE', 'TERMINATED') NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE NOT NULL,
    end_date        DATE,
    FOREIGN KEY (relationship_id) REFERENCES party_relationship(id),
    FOREIGN KEY (employee_id)     REFERENCES person(party_id),
    FOREIGN KEY (org_id)          REFERENCES organization(party_id)
);

-- PositionAssignment (support kiêm nhiệm)
CREATE TABLE position_assignment (
    id              VARCHAR(36) PRIMARY KEY,
    employment_id   VARCHAR(36) NOT NULL,
    position        ENUM('MANAGER', 'DEPUTY_MANAGER', 'FINANCE', 'TECHNICAL',
                         'SECURITY', 'RECEPTIONIST', 'STAFF') NOT NULL,
    department      VARCHAR(100),
    start_date      DATE NOT NULL,
    end_date        DATE,
    FOREIGN KEY (employment_id) REFERENCES employment(id)
);

-- Identification
CREATE TABLE party_identification (
    id          VARCHAR(36) PRIMARY KEY,
    party_id    VARCHAR(36) NOT NULL,
    type        ENUM('CCCD', 'TAX_ID', 'PASSPORT', 'BUSINESS_REG') NOT NULL,
    value       VARCHAR(100) NOT NULL,
    issued_date DATE,
    FOREIGN KEY (party_id) REFERENCES party(id),
    UNIQUE KEY uq_identification (type, value)
);
```

---

## 4. Domain Events Published

| Event | Trigger | Payload |
|-------|---------|---------|
| `PersonCreated` | Tạo Person mới | `{ partyId, name }` |
| `OrganizationCreated` | Tạo Org mới | `{ partyId, name, orgType }` |
| `HouseholdCreated` | Tạo Household | `{ partyId, headPersonId }` |
| `MemberAdded` | Person join Household/TenantOrg | `{ relationshipId, personId, groupId, groupType }` |
| `MemberRemoved` | Person rời Household/TenantOrg | `{ relationshipId, personId, groupId }` |
| `EmploymentCreated` | BQL staff được thêm | `{ employmentId, personId, orgId }` |
| `EmploymentTerminated` | BQL staff nghỉ việc | `{ employmentId, personId, orgId }` |
| `PositionAssigned` | Giao/thay đổi chức vụ | `{ employmentId, position, department, startDate }` |

---

## 5. Use Cases

### OPERATOR portal — nhân sự BQL

| Use case | Actor | Flow |
|----------|-------|------|
| Tạo Person (BQL staff) | BQL_MANAGER | Tạo Party(PERSON) + Person record |
| Thêm staff vào BQL | BQL_MANAGER | Tạo PartyRelationship(EMPLOYED_BY) → tạo Employment → emit EmploymentCreated |
| Giao chức vụ cho staff | BQL_MANAGER | Tạo PositionAssignment trên Employment → emit PositionAssigned |
| Terminate employment | BQL_MANAGER | Employment.status=TERMINATED, end_date=now → emit EmploymentTerminated |
| Xem danh sách nhân sự | BQL_MANAGER | Query Employment JOIN PositionAssignment WHERE org_id=BQL Org |

### OPERATOR portal — resident / tenant

| Use case | Actor | Flow |
|----------|-------|------|
| Tạo Person (resident) | BQL_MANAGER | Tạo Party(PERSON) + Person record |
| Tạo Household | BQL_MANAGER | Tạo Party(HOUSEHOLD) + Household, add head person |
| Thêm thành viên hộ | BQL_MANAGER | Tạo PartyRelationship(MEMBER_OF, Person → Household) |
| Tạo Tenant Org | BQL_MANAGER | Tạo Party(ORGANIZATION) + Organization(orgType=TENANT) |
| Thêm đại diện Tenant | BQL_MANAGER | Tạo PartyRelationship(MEMBER_OF, Person → TenantOrg) |

### TENANT portal — member management

| Use case | Actor | Note |
|----------|-------|------|
| Thêm nhân sự vào Org | TENANT_ADMIN | Tạo PartyRelationship(MEMBER_OF, Person → TenantOrg) |
| Xoá nhân sự khỏi Org | TENANT_ADMIN | Set end_date + status=ENDED trên PartyRelationship |
| Assign sub-role cho member | TENANT_ADMIN | **Không phải party-service** — gọi admin-service (TenantSubRoleAssignment) |

---

## 6. Business Rules

1. `Household.head_person_id` phải là `MEMBER_OF` Household đó
2. 1 Person chỉ có 1 active `Employment` với BQL Org tại 1 thời điểm
3. `Employment` chỉ được tạo khi `party_relationship.type = EMPLOYED_BY` và `to_party.org_type = BQL` — validate tại application layer
4. `PartyIdentification.value` unique theo từng `type`
5. `PositionAssignment` chỉ được tạo trên Employment có `status = ACTIVE`
