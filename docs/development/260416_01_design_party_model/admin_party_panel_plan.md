# Plan: Admin Panel — Party Management

## Phạm vi

Implement section "Parties" trong admin panel — cho phép SUPER_ADMIN quản lý danh tính pháp lý (Party) phục vụ vận hành hệ thống.

Ảnh hưởng: `web/` (Angular) only — không thay đổi backend. Party-service proxy đã có từ Phase 2.

---

## Scope phân quyền theo loại Party

| Party Type     | Search/View | Create     | Ghi chú                                  |
|----------------|-------------|------------|------------------------------------------|
| `PERSON`       | ✅           | ✅          | Edge case: người chưa có tài khoản app   |
| `ORGANIZATION` | ✅           | ✅ BQL only | `orgType` mặc định và chỉ cho phép `BQL` |
| `HOUSEHOLD`    | ✅           | ❌          | Tạo tự động qua agreement flow           |

---

## API party-service (qua gateway `/api/party/v1/...`)

| Method | Endpoint                                      | Dùng cho                              |
|--------|-----------------------------------------------|---------------------------------------|
| `GET`  | `/parties?keyword=&type=&status=&page=&size=` | Search tất cả party                   |
| `GET`  | `/parties/{id}`                               | Xem chi tiết                          |
| `POST` | `/parties`                                    | Tạo party (body dispatch theo `type`) |
| `POST` | `/parties/{id}/identifications`               | Thêm giấy tờ định danh                |

### Create PERSON body
```json
{
  "type": "PERSON",
  "partyName": "Nguyễn Văn A",
  "firstName": "Văn A",
  "lastName": "Nguyễn",
  "dob": "1990-01-15",
  "gender": "MALE",
  "identifications": [
    { "type": "CCCD", "value": "012345678901", "issuedDate": "2020-01-01" }
  ]
}
```

### Create ORGANIZATION body
```json
{
  "type": "ORGANIZATION",
  "partyName": "Ban Quản Lý Tòa A",
  "orgType": "BQL",
  "taxId": "0123456789",
  "registrationNo": "..."
}
```

### Enums
- `PartyType`: `PERSON`, `ORGANIZATION`, `HOUSEHOLD`
- `OrgType`: `BQL`, `TENANT`, `VENDOR`, `OTHER` — admin chỉ tạo được `BQL`
- `Gender`: `MALE`, `FEMALE`, `OTHER`
- `PartyIdentificationType`: `CCCD`, `TAX_ID`, `PASSPORT`, `BUSINESS_REG`
- `PartyStatus`: `ACTIVE`, `INACTIVE`

---

## Trạng thái hiện tại

- Sidebar `dashboard.html` đã có placeholder "Parties" — `sidebar-item-disabled`
- Party-service proxy `/api/party/**` đã config trong web-gateway (Phase 2)

---

## Phase — Implementation

### Service

**File**: `web/src/app/admin/parties/party.service.ts`

```typescript
export interface PartySummary {
  id: string;
  name: string;
  type: 'PERSON' | 'ORGANIZATION' | 'HOUSEHOLD';
  status: 'ACTIVE' | 'INACTIVE';
}

export interface PartyDetail extends PartySummary {
  identifications: { id: string; type: string; value: string; issuedDate: string }[];
  subtypeData: PersonData | OrgData | HouseholdData | null;
  createdAt: string;
  updatedAt: string;
}

export interface PersonData { firstName: string; lastName: string; dob: string; gender: string }
export interface OrgData { orgType: string; taxId: string; registrationNo: string }
export interface HouseholdData { headPersonId: string }
```

| Method                                                                                  | HTTP   | Endpoint                                     |
|-----------------------------------------------------------------------------------------|--------|----------------------------------------------|
| `searchParties(keyword?, type?, status?, page?, size?): Observable<Page<PartySummary>>` | `GET`  | `/api/party/v1/parties`                      |
| `getPartyById(id): Observable<PartyDetail>`                                             | `GET`  | `/api/party/v1/parties/{id}`                 |
| `createPerson(req): Observable<{id: string}>`                                           | `POST` | `/api/party/v1/parties`                      |
| `createOrganization(req): Observable<{id: string}>`                                     | `POST` | `/api/party/v1/parties`                      |
| `addIdentification(partyId, req): Observable<void>`                                     | `POST` | `/api/party/v1/parties/{id}/identifications` |

---

### Components

#### `party-list.component`

**File**: `web/src/app/admin/parties/party-list.component.ts`

- Search bar + filter `type` (`<mat-select>`: ALL / PERSON / ORGANIZATION / HOUSEHOLD) + filter `status`
- `MatTable` phân trang: columns `name`, `type`, `status`, `id`, actions
- Nút "Tạo Person" → `CreatePersonDialogComponent`
- Nút "Tạo BQL Org" → `CreateOrgDialogComponent`
- Click row → navigate `/admin/parties/{id}`

---

#### `party-detail.component`

**File**: `web/src/app/admin/parties/party-detail.component.ts`

- Load `partyService.getPartyById(id)` từ route param
- Hiển thị thông tin cơ bản + subtype data (PersonData / OrgData / HouseholdData)
- Bảng `identifications` + nút "Thêm giấy tờ" → `AddIdentificationDialogComponent`
- Badge hiển thị `type` + `status`

---

#### `CreatePersonDialogComponent`

**File**: `web/src/app/admin/parties/create-person-dialog.component.ts`

Fields:
- `partyName` (text, required)
- `firstName`, `lastName` (text, required)
- `dob` (`<mat-datepicker>`)
- `gender` (`<mat-select>`: MALE / FEMALE / OTHER)
- `identifications`: optional, dạng chip list — thêm từng giấy tờ (type + value + issuedDate)

---

#### `CreateOrgDialogComponent`

**File**: `web/src/app/admin/parties/create-org-dialog.component.ts`

Fields:
- `partyName` (text, required)
- `orgType`: readonly, fixed = `BQL` (hiển thị label, không cho chọn)
- `taxId` (text)
- `registrationNo` (text)

> `orgType` locked tại `BQL` — không mở các type khác từ admin UI.

---

#### `AddIdentificationDialogComponent`

**File**: `web/src/app/admin/parties/add-identification-dialog.component.ts`

Fields:
- `type` (`<mat-select>`: CCCD / TAX_ID / PASSPORT / BUSINESS_REG)
- `value` (text)
- `issuedDate` (`<mat-datepicker>`)

---

### Routes

Thêm vào children của `admin` trong `app.routes.ts`:

```typescript
{
  path: 'parties',
  loadComponent: () => import('./admin/parties/party-list.component').then(m => m.PartyListComponent),
},
{
  path: 'parties/:id',
  loadComponent: () => import('./admin/parties/party-detail.component').then(m => m.PartyDetailComponent),
},
```

---

### Sidebar `dashboard.html`

Thay placeholder disabled bằng nav item thực, thêm ABAC guard:

```html
<!-- Xóa placeholder cũ (sidebar-item-disabled / matTooltip) -->

@if (abacService.isPermitted('route:admin:parties')) {
  <mat-list-item class="sidebar-item" routerLink="parties" routerLinkActive="active">
    <mat-icon matListItemIcon>people</mat-icon>
    <span matListItemTitle>Parties</span>
  </mat-list-item>
}
```

---

### `abac.service.ts`

Thêm vào `ADMIN_ROUTE_ELEMENT_IDS`:

```typescript
'route:admin:parties',
```

---

## ABAC Seed

Thêm vào `services/admin/src/main/resources/db/seed/operator_portal_seed.sql`:

```sql
-- Resource: party management
INSERT IGNORE INTO resource_definition (name, description, service_name, created_at, updated_at) VALUES
('party:management', 'Quản lý danh tính pháp lý — Person, Organization (BQL)',
 'party-service', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'view',            'Xem/tìm kiếm danh sách party',       TRUE  FROM resource_definition WHERE name = 'party:management';
INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'create-person',   'Tạo mới Person',                      FALSE FROM resource_definition WHERE name = 'party:management';
INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'create-bql-org',  'Tạo mới Organization loại BQL',       FALSE FROM resource_definition WHERE name = 'party:management';
INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'add-id-document', 'Thêm giấy tờ định danh cho party',    FALSE FROM resource_definition WHERE name = 'party:management';

-- UI Elements
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT 'route:admin:parties', 'Parties', 'MENU_ITEM', 'ADMIN', 'admin-nav', 3,
       r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'view'
WHERE r.name = 'party:management';

INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT 'btn:admin:party:create-person', 'Tạo Person', 'BUTTON', 'ADMIN', 'admin-party', 0,
       r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'create-person'
WHERE r.name = 'party:management';

INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT 'btn:admin:party:create-bql-org', 'Tạo BQL Org', 'BUTTON', 'ADMIN', 'admin-party', 1,
       r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'create-bql-org'
WHERE r.name = 'party:management';

INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT 'btn:admin:party:add-id-document', 'Thêm giấy tờ', 'BUTTON', 'ADMIN', 'admin-party', 2,
       r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'add-id-document'
WHERE r.name = 'party:management';
```

---

## Thứ tự implement

Phase 2 (gateway) đã xong → implement trực tiếp, không phụ thuộc phase nào khác.

---

## Status

| Item                   | Status          |
|------------------------|-----------------|
| Party Management Panel | `[x] Completed` |
