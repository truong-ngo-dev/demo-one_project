# ABAC Phase 1 — Test Data & Test Scenarios

> Dữ liệu test cho Phase 1 (Resource Catalogue, PolicySet/Policy/Rule, UIElement Registry).  
> Thiết kế để:  
> 1. Cover đầy đủ API CRUD của Phase 1  
> 2. Làm **fixture** cho Phase 2 (Visual Builder, Simulator Navigation) và Phase 3 (Instance Simulator, Audit Log)  
> 3. Phục vụ **UIElement evaluate** để filter sidebar khi gộp admin UI + portal UI

---

## Quy ước SpEL trong tài liệu này

| Biến                          | Kiểu          | Ý nghĩa                                                            |
|-------------------------------|---------------|--------------------------------------------------------------------|
| `subject.id`                  | String        | userId của người dùng hiện tại                                     |
| `subject.roles`               | Set\<String\> | Tập tên role, vd `['ADMIN', 'STAFF']`                              |
| `subject.getAttribute(k)`     | Object        | Attribute động của subject                                         |
| `action.getAttribute('name')` | String        | Tên action, vd `'LIST'`                                            |
| `object.name`                 | String        | Tên resource, vd `'user'` (dùng ở Policy target)                   |
| `object.data`                 | Map / null    | Instance data. `null` = navigation-level, có data = instance-level |

---

## 1. Roles cần tạo

Gọi `POST /api/v1/roles` (JWT của admin@example.com).

| name       | description                                          |
|------------|------------------------------------------------------|
| `ADMIN`    | *(đã seed — DataInitializer, bỏ qua)*                |
| `STAFF`    | Nhân viên ban quản lý — đọc được user, không tạo/xóa |
| `RESIDENT` | Cư dân — chỉ dùng portal cá nhân                     |

```text
POST /api/v1/roles
{ "name": "STAFF", "description": "Ban quản lý — xem và thao tác hạn chế" }

POST /api/v1/roles
{ "name": "RESIDENT", "description": "Cư dân — truy cập portal cá nhân" }
```

---

## 2. Users cần tạo

Gọi `POST /api/v1/users` (JWT của admin@example.com) sau đó gán role.

| Email                 | Password    | Role(s)      | Mục đích                                  |
|-----------------------|-------------|--------------|-------------------------------------------|
| `admin@example.com`   | *(đã seed)* | ADMIN        | Full access — kiểm tra PERMIT toàn phần   |
| `operator@bql.com`    | `Test1234!` | STAFF        | Quyền hạn chế — kiểm tra DENY một phần    |
| `resident001@apt.com` | `Test1234!` | RESIDENT     | Portal only — kiểm tra DENY toàn bộ admin |
| `norole@test.com`     | `Test1234!` | *(không có)* | Kiểm tra DEFAULT_DENY khi không có rule   |

```text
POST /api/v1/users
{ "email": "operator@bql.com", "password": "Test1234!", "fullName": "Operator Test" }
→ lưu userId → POST /api/v1/users/{userId}/roles  { "roleIds": ["<id của STAFF>"] }

POST /api/v1/users
{ "email": "resident001@apt.com", "password": "Test1234!", "fullName": "Resident Test" }
→ POST /api/v1/users/{userId}/roles  { "roleIds": ["<id của RESIDENT>"] }

POST /api/v1/users
{ "email": "norole@test.com", "password": "Test1234!", "fullName": "No Role User" }
```

---

## 3. Resources & Actions

Gọi `POST /api/v1/abac/resources` rồi `POST /api/v1/abac/resources/{id}/actions`.

### 3.1 `user` (admin-service)
```text
POST /api/v1/abac/resources
{ "name": "user", "description": "Quản lý người dùng hệ thống", "serviceName": "admin-service" }
```
Actions:
| name          | description                          | isStandard |
|---------------|--------------------------------------|------------|
| `LIST`        | Danh sách users                      | true       |
| `READ`        | Xem chi tiết user                    | true       |
| `CREATE`      | Tạo user mới                         | true       |
| `LOCK`        | Khóa tài khoản user                  | false      |
| `UNLOCK`      | Mở khóa tài khoản user               | false      |
| `ASSIGN_ROLE` | Gán role cho user                    | false      |
| `REMOVE_ROLE` | Gỡ role khỏi user                    | false      |

### 3.2 `role` (admin-service)
```json
{ "name": "role", "description": "Quản lý vai trò", "serviceName": "admin-service" }
```
Actions: `LIST`(std), `READ`(std), `CREATE`(std), `UPDATE`(std), `DELETE`(std)

### 3.3 `session` (oauth2-service)
```json
{ "name": "session", "description": "Quản lý phiên đăng nhập", "serviceName": "oauth2-service" }
```
Actions:
| name          | description                    | isStandard |
|---------------|--------------------------------|------------|
| `LIST_ALL`    | Xem tất cả session (admin)     | false      |
| `REVOKE_ANY`  | Thu hồi bất kỳ session (admin) | false      |
| `VIEW_OWN`    | Xem session của chính mình     | false      |
| `REVOKE_OWN`  | Thu hồi session của chính mình | false      |

### 3.4 `login_activity` (oauth2-service)
```json
{ "name": "login_activity", "description": "Lịch sử đăng nhập", "serviceName": "oauth2-service" }
```
Actions:
| name       | description                    | isStandard |
|------------|--------------------------------|------------|
| `LIST_ALL` | Xem toàn bộ log (admin)        | false      |
| `VIEW_OWN` | Xem lịch sử của chính mình     | false      |

### 3.5 `abac_resource` (admin-service)
```json
{ "name": "abac_resource", "description": "Resource & Action Catalogue", "serviceName": "admin-service" }
```
Actions: `LIST`(std), `READ`(std), `CREATE`(std), `UPDATE`(std), `DELETE`(std)

### 3.6 `abac_policy` (admin-service)
```json
{ "name": "abac_policy", "description": "PolicySet / Policy / Rule management", "serviceName": "admin-service" }
```
Actions: `LIST`(std), `READ`(std), `CREATE`(std), `UPDATE`(std), `DELETE`(std)

### 3.7 `abac_ui_element` (admin-service)
```json
{ "name": "abac_ui_element", "description": "UIElement Registry", "serviceName": "admin-service" }
```
Actions: `LIST`(std), `READ`(std), `CREATE`(std), `UPDATE`(std), `DELETE`(std)

### 3.8 `abac_simulator` (admin-service)
```json
{ "name": "abac_simulator", "description": "Policy Simulator", "serviceName": "admin-service" }
```
Actions:
| name      | description          | isStandard |
|-----------|----------------------|------------|
| `EXECUTE` | Chạy mô phỏng policy | false      |

### 3.9 `profile` (admin-service — self-service)
```json
{ "name": "profile", "description": "Hồ sơ cá nhân người dùng", "serviceName": "admin-service" }
```
Actions:
| name              | description               | isStandard |
|-------------------|---------------------------|------------|
| `VIEW`            | Xem hồ sơ cá nhân        | false      |
| `UPDATE`          | Cập nhật hồ sơ cá nhân   | false      |
| `CHANGE_PASSWORD` | Đổi mật khẩu             | false      |

### 3.10 `device` (oauth2-service)
```json
{ "name": "device", "description": "Thiết bị đăng nhập", "serviceName": "oauth2-service" }
```
Actions:
| name     | description                    | isStandard |
|----------|--------------------------------|------------|
| `LIST`   | Xem danh sách thiết bị của mình| true       |
| `REVOKE` | Thu hồi thiết bị               | false      |

### 3.11 `iam_dashboard` (oauth2-service)
```json
{ "name": "iam_dashboard", "description": "Tổng quan IAM KPI", "serviceName": "oauth2-service" }
```
Actions:
| name   | description      | isStandard |
|--------|------------------|------------|
| `VIEW` | Xem dashboard KPI| false      |

---

## 4. PolicySet

```text
POST /api/v1/abac/policy-sets
{
  "name": "bql-root",
  "scope": "OPERATOR",
  "combineAlgorithm": "DENY_OVERRIDES",
  "isRoot": true
}
```
> `isRoot: true` → PolicySet này được `AdminPolicyProvider` tự động load khi evaluate.  
> Lưu `policySetId` để dùng trong bước 5.

---

## 5. Policies

Gọi `POST /api/v1/abac/policies`. Tất cả thuộc `policySetId` = id của `bql-root`.

| name                     | targetExpression                   | combineAlgorithm     |
|--------------------------|------------------------------------|----------------------|
| `policy:user`            | `object.name == 'user'`            | `DENY_UNLESS_PERMIT` |
| `policy:role`            | `object.name == 'role'`            | `DENY_UNLESS_PERMIT` |
| `policy:session`         | `object.name == 'session'`         | `DENY_UNLESS_PERMIT` |
| `policy:login_activity`  | `object.name == 'login_activity'`  | `DENY_UNLESS_PERMIT` |
| `policy:abac_resource`   | `object.name == 'abac_resource'`   | `DENY_UNLESS_PERMIT` |
| `policy:abac_policy`     | `object.name == 'abac_policy'`     | `DENY_UNLESS_PERMIT` |
| `policy:abac_ui_element` | `object.name == 'abac_ui_element'` | `DENY_UNLESS_PERMIT` |
| `policy:abac_simulator`  | `object.name == 'abac_simulator'`  | `DENY_UNLESS_PERMIT` |
| `policy:profile`         | `object.name == 'profile'`         | `DENY_UNLESS_PERMIT` |
| `policy:device`          | `object.name == 'device'`          | `DENY_UNLESS_PERMIT` |
| `policy:iam_dashboard`   | `object.name == 'iam_dashboard'`   | `DENY_UNLESS_PERMIT` |

```text
POST /api/v1/abac/policies
{
  "policySetId": "<bql-root-id>",
  "name": "policy:user",
  "targetExpression": "object.name == 'user'",
  "combineAlgorithm": "DENY_UNLESS_PERMIT"
}
```
*(lặp lại cho các policy còn lại)*

---

## 6. Rules

Gọi `POST /api/v1/abac/policies/{policyId}/rules`. Thứ tự `orderIndex` có ý nghĩa với `FIRST_APPLICABLE`.

### 6.1 `policy:user`

| # | name                        | effect | targetExpression                                                                                                                                                                   | conditionExpression                                             |
|---|-----------------------------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| 1 | `admin-full-user`           | PERMIT | `subject.roles.contains('ADMIN')`                                                                                                                                                  | *(để trống — luôn PERMIT)*                                      |
| 2 | `operator-list-read-user`   | PERMIT | `subject.roles.contains('STAFF') && (action.getAttribute('name') == 'LIST' \|\| action.getAttribute('name') == 'READ')`                                                            | *(để trống)*                                                    |
| 3 | `operator-lock-unlock-user` | PERMIT | `subject.roles.contains('STAFF') && (action.getAttribute('name') == 'LOCK' \|\| action.getAttribute('name') == 'UNLOCK')`                                                          | `object.data == null \|\| !object.data.roles.contains('ADMIN')` |
| 4 | `deny-operator-mutate-user` | DENY   | `subject.roles.contains('STAFF') && (action.getAttribute('name') == 'CREATE' \|\| action.getAttribute('name') == 'ASSIGN_ROLE' \|\| action.getAttribute('name') == 'REMOVE_ROLE')` | *(để trống)*                                                    |

> **Rule 3** là case hay cho Phase 3 — instance-level condition: operator không thể lock một user có role ADMIN.  
> **Rule 4** test DENY trong cùng policy với PERMIT.

### 6.2 `policy:role`

| # | name              | effect | targetExpression                  | conditionExpression |
|---|-------------------|--------|-----------------------------------|---------------------|
| 1 | `admin-full-role` | PERMIT | `subject.roles.contains('ADMIN')` | *(để trống)*        |

### 6.3 `policy:session`

| # | name                   | effect | targetExpression                                                                                                   | conditionExpression |
|---|------------------------|--------|--------------------------------------------------------------------------------------------------------------------|---------------------|
| 1 | `admin-full-session`   | PERMIT | `subject.roles.contains('ADMIN')`                                                                                  | *(để trống)*        |
| 2 | `user-own-session`     | PERMIT | `action.getAttribute('name') == 'VIEW_OWN' \|\| action.getAttribute('name') == 'REVOKE_OWN'`                       | *(để trống)*        |

### 6.4 `policy:login_activity`

| # | name                      | effect | targetExpression                                                               | conditionExpression |
|---|---------------------------|--------|--------------------------------------------------------------------------------|---------------------|
| 1 | `admin-list-all-activity` | PERMIT | `subject.roles.contains('ADMIN') && action.getAttribute('name') == 'LIST_ALL'` | *(để trống)*        |
| 2 | `user-view-own-activity`  | PERMIT | `action.getAttribute('name') == 'VIEW_OWN'`                                    | *(để trống)*        |

### 6.5 `policy:abac_resource`

| # | name                       | effect | targetExpression                  | conditionExpression |
|---|----------------------------|--------|-----------------------------------|---------------------|
| 1 | `admin-full-abac-resource` | PERMIT | `subject.roles.contains('ADMIN')` | *(để trống)*        |

### 6.6 `policy:abac_policy`

| # | name                     | effect | targetExpression                  | conditionExpression |
|---|--------------------------|--------|-----------------------------------|---------------------|
| 1 | `admin-full-abac-policy` | PERMIT | `subject.roles.contains('ADMIN')` | *(để trống)*        |

### 6.7 `policy:abac_ui_element`

| # | name                      | effect | targetExpression                  | conditionExpression |
|---|---------------------------|--------|-----------------------------------|---------------------|
| 1 | `admin-full-ui-element`   | PERMIT | `subject.roles.contains('ADMIN')` | *(để trống)*        |

### 6.8 `policy:abac_simulator`

| # | name                      | effect | targetExpression                  | conditionExpression |
|---|---------------------------|--------|-----------------------------------|---------------------|
| 1 | `admin-execute-simulator` | PERMIT | `subject.roles.contains('ADMIN')` | *(để trống)*        |

### 6.9 `policy:profile`

| # | name                   | effect | targetExpression                                                                                                                           | conditionExpression |
|---|------------------------|--------|--------------------------------------------------------------------------------------------------------------------------------------------|---------------------|
| 1 | `self-service-profile` | PERMIT | `action.getAttribute('name') == 'VIEW' \|\| action.getAttribute('name') == 'UPDATE' \|\| action.getAttribute('name') == 'CHANGE_PASSWORD'` | *(để trống)*        |

> **Không** giới hạn role — mọi user đã đăng nhập đều có quyền tự quản lý profile.  
> Enforce "chỉ xem profile của mình" là tầng API, không phải ABAC.

### 6.10 `policy:device`

| # | name              | effect | targetExpression                                                                     | conditionExpression                                         |
|---|-------------------|--------|--------------------------------------------------------------------------------------|-------------------------------------------------------------|
| 1 | `user-own-device` | PERMIT | `action.getAttribute('name') == 'LIST' \|\| action.getAttribute('name') == 'REVOKE'` | `object.data == null \|\| object.data.userId == subject.id` |

> **Rule này** là case tốt cho Phase 3 instance-level: navigation → PERMIT (object.data null), instance với `userId == subject.id` → PERMIT, instance của người khác → DENY.

### 6.11 `policy:iam_dashboard`

| # | name                   | effect | targetExpression                  | conditionExpression |
|---|------------------------|--------|-----------------------------------|---------------------|
| 1 | `admin-view-dashboard` | PERMIT | `subject.roles.contains('ADMIN')` | *(để trống)*        |

---

## 7. UIElements

Gọi `POST /api/v1/abac/ui-elements`.  
**Lưu ý**: `actionId` phải thuộc `resourceId` đã khai báo ở bước 3 — PEP sẽ reject nếu không hợp lệ.

### 7.1 Admin Sidebar Navigation (`group: admin-nav`, type: MENU_ITEM)

| elementId                    | label            | resource          | action     | order |
|------------------------------|------------------|-------------------|------------|-------|
| `nav:admin:dashboard`        | Dashboard        | `iam_dashboard`   | `VIEW`     | 1     |
| `nav:admin:users`            | Users            | `user`            | `LIST`     | 2     |
| `nav:admin:roles`            | Roles            | `role`            | `LIST`     | 3     |
| `nav:admin:abac-resources`   | Resources        | `abac_resource`   | `LIST`     | 4     |
| `nav:admin:abac-policy-sets` | Policy Sets      | `abac_policy`     | `LIST`     | 5     |
| `nav:admin:abac-ui-elements` | UI Elements      | `abac_ui_element` | `LIST`     | 6     |
| `nav:admin:abac-simulator`   | Simulator        | `abac_simulator`  | `EXECUTE`  | 7     |
| `nav:admin:sessions`         | Sessions         | `session`         | `LIST_ALL` | 8     |
| `nav:admin:login-activities` | Login Activities | `login_activity`  | `LIST_ALL` | 9     |

### 7.2 Portal Sidebar Navigation (`group: portal-nav`, type: MENU_ITEM)

| elementId                  | label             | resource         | action     | order |
|----------------------------|-------------------|------------------|------------|-------|
| `nav:portal:dashboard`     | Home              | `profile`        | `VIEW`     | 1     |
| `nav:portal:profile`       | Hồ sơ             | `profile`        | `VIEW`     | 2     |
| `nav:portal:devices`       | Thiết bị          | `device`         | `LIST`     | 3     |
| `nav:portal:login-history` | Lịch sử đăng nhập | `login_activity` | `VIEW_OWN` | 4     |

### 7.3 User Management Actions (`group: user-actions`, type: BUTTON)

| elementId              | label         | resource | action        | order |
|------------------------|---------------|----------|---------------|-------|
| `btn:user:create`      | Tạo user      | `user`   | `CREATE`      | 1     |
| `btn:user:lock`        | Khóa          | `user`   | `LOCK`        | 2     |
| `btn:user:unlock`      | Mở khóa       | `user`   | `UNLOCK`      | 3     |
| `btn:user:assign-role` | Gán role      | `user`   | `ASSIGN_ROLE` | 4     |
| `btn:user:remove-role` | Gỡ role       | `user`   | `REMOVE_ROLE` | 5     |

### 7.4 Role Management Actions (`group: role-actions`, type: BUTTON)

| elementId          | label      | resource | action   | order |
|--------------------|------------|----------|----------|-------|
| `btn:role:create`  | Tạo role   | `role`   | `CREATE` | 1     |
| `btn:role:update`  | Sửa role   | `role`   | `UPDATE` | 2     |
| `btn:role:delete`  | Xóa role   | `role`   | `DELETE` | 3     |

### 7.5 ABAC Resource Actions (`group: abac-resource-actions`, type: BUTTON)

| elementId                      | label          | resource        | action   | order |
|--------------------------------|----------------|-----------------|----------|-------|
| `btn:abac-resource:create`     | Tạo resource   | `abac_resource` | `CREATE` | 1     |
| `btn:abac-resource:update`     | Sửa resource   | `abac_resource` | `UPDATE` | 2     |
| `btn:abac-resource:delete`     | Xóa resource   | `abac_resource` | `DELETE` | 3     |
| `btn:abac-resource:add-action` | Thêm action    | `abac_resource` | `UPDATE` | 4     |

### 7.6 ABAC Policy Actions (`group: abac-policy-actions`, type: BUTTON)

| elementId                  | label        | resource      | action   | order |
|----------------------------|--------------|---------------|----------|-------|
| `btn:abac-policy:create`   | Tạo policy   | `abac_policy` | `CREATE` | 1     |
| `btn:abac-policy:update`   | Sửa policy   | `abac_policy` | `UPDATE` | 2     |
| `btn:abac-policy:delete`   | Xóa policy   | `abac_policy` | `DELETE` | 3     |

### 7.7 UI Element Actions (`group: abac-ui-element-actions`, type: BUTTON)

| elementId                      | label       | resource          | action   | order |
|--------------------------------|-------------|-------------------|----------|-------|
| `btn:abac-ui-element:create`   | Tạo element | `abac_ui_element` | `CREATE` | 1     |
| `btn:abac-ui-element:update`   | Sửa element | `abac_ui_element` | `UPDATE` | 2     |
| `btn:abac-ui-element:delete`   | Xóa element | `abac_ui_element` | `DELETE` | 3     |

---

## 8. Test Scenarios Phase 1

### 8.1 CRUD Validation Cases

| Scenario                                          | API call                                                                     | Expected                     |
|---------------------------------------------------|------------------------------------------------------------------------------|------------------------------|
| Tạo resource trùng tên                            | `POST /abac/resources` với `name: "user"` lần 2                              | 400, AbacErrorCode trùng tên |
| Thêm action trùng tên                             | `POST /abac/resources/{id}/actions` với `name: "LIST"` lần 2                 | 400                          |
| Tạo UIElement với actionId không thuộc resourceId | `POST /abac/ui-elements` với sai cặp resource/action                         | 400                          |
| Tạo UIElement trùng elementId                     | `POST /abac/ui-elements` với `elementId: "nav:admin:users"` lần 2            | 400                          |
| Tạo Policy với SpEL sai cú pháp                   | `POST /abac/policies` với `targetExpression: "object.name =="` (thiếu value) | 400                          |
| Xóa resource có UIElement ref                     | `DELETE /abac/resources/{userId}` khi còn UIElement trỏ vào                  | 400                          |
| Xóa action có UIElement ref                       | `DELETE /abac/resources/{id}/actions/{actionId}` khi còn UIElement           | 400                          |

### 8.2 Rule Reorder

```
Sau khi tạo 4 rules trong policy:user, thứ tự mặc định: [1, 2, 3, 4]
PUT /api/v1/abac/policies/{policyId}/rules/reorder
{ "ruleIds": ["<id-4>", "<id-1>", "<id-3>", "<id-2>"] }
→ GET /api/v1/abac/policies/{policyId} → rules trả về đúng thứ tự mới
```

### 8.3 UIElement Evaluate — Navigation Level

Đây là kịch bản cốt lõi: **login → lấy token → evaluate**.

```
POST /api/v1/abac/ui-elements/evaluate
Authorization: Bearer <JWT của user>
{
  "elementIds": [
    "nav:admin:dashboard",
    "nav:admin:users",
    "nav:admin:roles",
    "nav:admin:abac-resources",
    "nav:admin:abac-policy-sets",
    "nav:admin:abac-ui-elements",
    "nav:admin:abac-simulator",
    "nav:admin:sessions",
    "nav:admin:login-activities",
    "nav:portal:dashboard",
    "nav:portal:profile",
    "nav:portal:devices",
    "nav:portal:login-history"
  ]
}
```

---

## 9. Expected Evaluate Results Matrix

> `P` = PERMIT, `D` = DENY

| UIElement                    | ADMIN | STAFF      | RESIDENT | No Role |
|------------------------------|-------|------------|----------|---------|
| `nav:admin:dashboard`        | P     | D          | D        | D       |
| `nav:admin:users`            | P     | P *(LIST)* | D        | D       |
| `nav:admin:roles`            | P     | D          | D        | D       |
| `nav:admin:abac-resources`   | P     | D          | D        | D       |
| `nav:admin:abac-policy-sets` | P     | D          | D        | D       |
| `nav:admin:abac-ui-elements` | P     | D          | D        | D       |
| `nav:admin:abac-simulator`   | P     | D          | D        | D       |
| `nav:admin:sessions`         | P     | D          | D        | D       |
| `nav:admin:login-activities` | P     | D          | D        | D       |
| `nav:portal:dashboard`       | P     | P          | P        | P       |
| `nav:portal:profile`         | P     | P          | P        | P       |
| `nav:portal:devices`         | P     | P          | P        | P       |
| `nav:portal:login-history`   | P     | P          | P        | P       |

> **Giải thích STAFF**:  
> - `nav:admin:users` → PERMIT vì rule `operator-list-read-user` match action `LIST`  
> - `nav:admin:roles` → DENY vì không có rule nào PERMIT STAFF với resource `role`  
> - portal items → PERMIT vì `policy:profile`, `policy:device`, `policy:login_activity` rule không restrict theo role  
>
> **Giải thích No Role**:  
> - portal items → PERMIT vì `policy:profile` rule target không check role  
> - admin items → DENY vì tất cả rules đều check `subject.roles.contains('ADMIN')`  

---

## 10. Button-level Evaluate (bổ sung khi vào page cụ thể)

```
POST /api/v1/abac/ui-elements/evaluate
Authorization: Bearer <JWT của STAFF>
{
  "elementIds": [
    "btn:user:create",
    "btn:user:lock",
    "btn:user:unlock",
    "btn:user:assign-role",
    "btn:user:remove-role"
  ]
}
```

Expected với **STAFF**:

| UIElement              | Expected | Lý do                                                                         |
|------------------------|----------|-------------------------------------------------------------------------------|
| `btn:user:create`      | DENY     | rule `deny-operator-mutate-user` DENY CREATE                                  |
| `btn:user:lock`        | PERMIT   | rule `operator-lock-unlock-user` PERMIT (navigation level → object.data null) |
| `btn:user:unlock`      | PERMIT   | như trên                                                                      |
| `btn:user:assign-role` | DENY     | rule `deny-operator-mutate-user` DENY ASSIGN_ROLE                             |
| `btn:user:remove-role` | DENY     | rule `deny-operator-mutate-user` DENY REMOVE_ROLE                             |

---

## 11. Kịch bản để dành cho Phase 2 / Phase 3

### Phase 2 — Simulator Navigation Mode
Sau khi Simulator có UI, dùng `bql-root` PolicySet với virtual subject:
- Virtual: roles = `['STAFF']` → xác nhận kết quả trùng với matrix trên
- Virtual: roles = `[]` → tất cả DENY

### Phase 3 — Simulator Instance Mode

**Kịch bản device ownership (policy:device — rule 1):**
```text
Resource: device
Instance data:
  { "deviceId": "dev-001", "userId": "<userId của resident001>" }

Subject: resident001 (RESIDENT)
→ ACTION LIST: PERMIT (object.data.userId == subject.id)

Subject: norole@test.com
→ ACTION LIST: DENY (object.data.userId != subject.id)
```

**Kịch bản admin lock protection (policy:user — rule 3):**
```text
Resource: user
Instance data:
  { "userId": "<adminUserId>", "roles": ["ADMIN"] }

Subject: operator@bql.com (STAFF)
→ ACTION LOCK: DENY (object.data.roles.contains('ADMIN') → condition false)

Instance data:
  { "userId": "<resident001Id>", "roles": ["RESIDENT"] }

Subject: operator@bql.com (STAFF)
→ ACTION LOCK: PERMIT (object.data.roles không chứa ADMIN → condition true)
```

---

## 12. Thứ tự tạo data (dependency order)

```
1. Roles (STAFF, RESIDENT)
2. Users (operator, resident001, norole)  — gán roles
3. Resources & Actions (11 resources, tổng ~35 actions)
4. PolicySet (bql-root)
5. Policies (11 policies)  — cần policySetId
6. Rules     — cần policyId và phải nhớ thứ tự orderIndex
7. UIElements — cần resourceId và actionId chính xác
```

> Bước 7 phụ thuộc bước 3 nên **không thể đảo thứ tự**.  
> Nên lưu lại các ID vào một file tạm (ví dụ `test-ids.json`) khi tạo, để dùng trong bước sau.
