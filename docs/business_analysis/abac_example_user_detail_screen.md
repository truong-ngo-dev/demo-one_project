# Ví dụ Cụ thể: ABAC Authorization trên màn hình User Detail

Màn hình `UserDetailComponent` (`/admin/users/:id`) được dùng làm ví dụ end-to-end.
Màn hình có 2 tab, nhiều action button — mỗi button/tab có authorization requirement khác nhau.

---

## 1. Bản đồ màn hình → UIElement

```
UserDetailComponent (/admin/users/:id)
│
├── [HEADER]
│   └── btn "Lock / Unlock User"           → element: btn:user:lock-toggle
│
├── [TAB: Profile]                          → element: tab:user-detail:profile
│   ├── (read-only fields: email, name...)  → không cần riêng, cover bởi tab permission
│   ├── section "Assigned Roles"
│   │   ├── btn "Assign Roles"             → element: btn:user:assign-role
│   │   └── btn "Remove Role" (per chip)   → element: btn:user:remove-role
│   └── section "Social Connections"       → không cần control, cover bởi tab
│
└── [TAB: Security & Devices]              → element: tab:user-detail:security
    ├── table "Devices & Sessions"
    │   └── btn "Force Terminate"          → element: btn:user-session:revoke
    └── table "Login History"              → cover bởi tab permission
```

**6 UIElements** cần kiểm soát. Toàn bộ screen chỉ cần **1 API call** để lấy permissions.

---

## 2. UIElement Registry (dữ liệu mẫu — lưu ở BE)

```json
[
  {
    "elementId": "tab:user-detail:profile",
    "elementType": "TAB",
    "resourceRef": "user",
    "actionRef": "READ",
    "renderMetadata": {
      "label": "Profile",
      "order": 0,
      "group": "user-detail-tabs"
    }
  },
  {
    "elementId": "btn:user:lock-toggle",
    "elementType": "BUTTON",
    "resourceRef": "user",
    "actionRef": "LOCK",
    "renderMetadata": {
      "label": "Lock / Unlock User",
      "group": "user-detail-header"
    }
  },
  {
    "elementId": "btn:user:assign-role",
    "elementType": "BUTTON",
    "resourceRef": "user_role",
    "actionRef": "ASSIGN",
    "renderMetadata": {
      "label": "Assign Roles",
      "group": "user-detail-roles"
    }
  },
  {
    "elementId": "btn:user:remove-role",
    "elementType": "BUTTON",
    "resourceRef": "user_role",
    "actionRef": "REMOVE",
    "renderMetadata": {
      "label": "Remove Role",
      "group": "user-detail-roles"
    }
  },
  {
    "elementId": "tab:user-detail:security",
    "elementType": "TAB",
    "resourceRef": "user_session",
    "actionRef": "LIST",
    "renderMetadata": {
      "label": "Security & Devices",
      "order": 1,
      "group": "user-detail-tabs"
    }
  },
  {
    "elementId": "btn:user-session:revoke",
    "elementType": "BUTTON",
    "resourceRef": "user_session",
    "actionRef": "REVOKE",
    "renderMetadata": {
      "label": "Force Terminate",
      "group": "user-detail-security"
    }
  }
]
```

---

## 3. Policy (dữ liệu mẫu — policy.json cho admin service)

Định dạng tương thích với engine hiện tại. Hai loại evaluation dùng chung Policy:
- **UIElement evaluation**: `action.getAttribute('name') == 'LOCK'` (synthetic action, không có HttpServletRequest)
- **API enforcement** (`@PreEnforce`): `action.request.servletPattern == '/api/...'` (có HttpServletRequest)

```json
{
  "id": "admin-service-root",
  "description": "Root policy for admin service",
  "target": {
    "type": "LITERAL",
    "expression": "environment.getServiceEnv('application.name') == 'admin'"
  },
  "combineAlgorithmName": "DENY_UNLESS_PERMIT",
  "isRoot": true,
  "policies": [

    {
      "id": "policy:user",
      "description": "Access control for user resource",
      "target": {
        "type": "LITERAL",
        "expression": "object.name == 'user'"
      },
      "combineAlgorithmName": "DENY_UNLESS_PERMIT",
      "rules": [
        {
          "id": "rule:user:read:all-admins",
          "description": "Mọi loại admin đều đọc được user",
          "target": {
            "type": "LITERAL",
            "expression": "subject.roles.contains('SYSTEM_ADMIN') || subject.roles.contains('SUPPORT_AGENT') || subject.roles.contains('SERVICE_ADMIN')"
          },
          "condition": {
            "type": "LITERAL",
            "expression": "action.getAttribute('name') == 'READ' || action.request.servletPattern == '/api/admin/users/{id}' && action.request.method == 'GET'"
          },
          "effect": "PERMIT"
        },
        {
          "id": "rule:user:lock:system-and-support",
          "description": "Chỉ SYSTEM_ADMIN và SUPPORT_AGENT mới lock/unlock user",
          "target": {
            "type": "LITERAL",
            "expression": "subject.roles.contains('SYSTEM_ADMIN') || subject.roles.contains('SUPPORT_AGENT')"
          },
          "condition": {
            "type": "LITERAL",
            "expression": "action.getAttribute('name') == 'LOCK' || (action.request.servletPattern == '/api/admin/users/{id}/lock' || action.request.servletPattern == '/api/admin/users/{id}/unlock') && action.request.method == 'POST'"
          },
          "effect": "PERMIT"
        }
      ]
    },

    {
      "id": "policy:user_role",
      "description": "Access control for user role assignment",
      "target": {
        "type": "LITERAL",
        "expression": "object.name == 'user_role'"
      },
      "combineAlgorithmName": "DENY_UNLESS_PERMIT",
      "rules": [
        {
          "id": "rule:user_role:assign-remove:system-admin-only",
          "description": "Chỉ SYSTEM_ADMIN mới assign/remove role",
          "target": {
            "type": "LITERAL",
            "expression": "subject.roles.contains('SYSTEM_ADMIN')"
          },
          "condition": {
            "type": "LITERAL",
            "expression": "action.getAttribute('name') == 'ASSIGN' || action.getAttribute('name') == 'REMOVE' || (action.request.servletPattern == '/api/admin/users/{id}/roles' && (action.request.method == 'POST' || action.request.method == 'DELETE'))"
          },
          "effect": "PERMIT"
        }
      ]
    },

    {
      "id": "policy:user_session",
      "description": "Access control for user session management (admin view)",
      "target": {
        "type": "LITERAL",
        "expression": "object.name == 'user_session'"
      },
      "combineAlgorithmName": "DENY_UNLESS_PERMIT",
      "rules": [
        {
          "id": "rule:user_session:list:admin-roles",
          "description": "Mọi admin đều xem được session list",
          "target": {
            "type": "LITERAL",
            "expression": "subject.roles.contains('SYSTEM_ADMIN') || subject.roles.contains('SUPPORT_AGENT') || subject.roles.contains('SERVICE_ADMIN')"
          },
          "condition": {
            "type": "LITERAL",
            "expression": "action.getAttribute('name') == 'LIST' || (action.request.servletPattern == '/api/admin/users/{id}/sessions' && action.request.method == 'GET')"
          },
          "effect": "PERMIT"
        },
        {
          "id": "rule:user_session:revoke:system-and-support",
          "description": "Chỉ SYSTEM_ADMIN và SUPPORT_AGENT mới revoke session",
          "target": {
            "type": "LITERAL",
            "expression": "subject.roles.contains('SYSTEM_ADMIN') || subject.roles.contains('SUPPORT_AGENT')"
          },
          "condition": {
            "type": "LITERAL",
            "expression": "action.getAttribute('name') == 'REVOKE' || (action.request.servletPattern == '/api/admin/sessions/{id}/revoke' && action.request.method == 'POST')"
          },
          "effect": "PERMIT"
        }
      ]
    }

  ]
}
```

---

## 4. Chuỗi liên kết: UIElement → Policy → Quyết định

Lấy ví dụ `btn:user-session:revoke`:

```
UIElement Registry
  elementId:   "btn:user-session:revoke"
  resourceRef: "user_session"          ← resource name để lookup policy
  actionRef:   "REVOKE"                ← sẽ set vào action.attributes["name"]
       │
       ▼
AuthorizationContextEngine.evaluate(subject, elements)
  1. Group elements by resourceRef
     → {user: [...], user_role: [...], user_session: [...]}

  2. Với group "user_session":
     a. PIP: load policy có target "object.name == 'user_session'"  → policy:user_session
     b. Build synthetic Action:
        action.attributes["name"] = "LIST"   (cho tab:user-detail:security)
        action.attributes["name"] = "REVOKE" (cho btn:user-session:revoke)
     c. Gọi PdpEngine cho (resource="user_session", action.name="LIST")
     d. Gọi PdpEngine cho (resource="user_session", action.name="REVOKE")

  3. Collect kết quả vào grants map

       ▼
policy:user_session được evaluate với rule:user_session:revoke:system-and-support
  target check: subject.roles.contains('SYSTEM_ADMIN')  →  true/false
  condition check: action.getAttribute('name') == 'REVOKE'  →  true
  effect: PERMIT / DENY

       ▼
AuthzContext response: { "user_session:REVOKE": true/false }
```

**Số lần gọi PdpEngine** = số unique (resourceRef, actionRef) pairs = **5 lần** (không phải 6 lần —
`btn:user:assign-role` và `btn:user:remove-role` chia cùng resource `user_role` nhưng action khác nhau).

---

## 5. AuthzContext Response — 3 loại user

### GRANTS constants (FE khai báo một lần, dùng toàn bộ admin app)

```typescript
// src/app/core/auth/grants.ts
export const GRANTS = {
  USER: {
    READ:  'user:READ',
    LOCK:  'user:LOCK',
  },
  USER_ROLE: {
    ASSIGN: 'user_role:ASSIGN',
    REMOVE: 'user_role:REMOVE',
  },
  USER_SESSION: {
    LIST:   'user_session:LIST',
    REVOKE: 'user_session:REVOKE',
  },
} as const;
```

### 5A: SYSTEM_ADMIN

```json
{
  "grants": {
    "user:READ":           true,
    "user:LOCK":           true,
    "user_role:ASSIGN":    true,
    "user_role:REMOVE":    true,
    "user_session:LIST":   true,
    "user_session:REVOKE": true
  }
}
```

### 5B: SUPPORT_AGENT

```json
{
  "grants": {
    "user:READ":           true,
    "user:LOCK":           true,
    "user_role:ASSIGN":    false,
    "user_role:REMOVE":    false,
    "user_session:LIST":   true,
    "user_session:REVOKE": true
  }
}
```

### 5C: SERVICE_ADMIN (oauth2)

```json
{
  "grants": {
    "user:READ":           true,
    "user:LOCK":           false,
    "user_role:ASSIGN":    false,
    "user_role:REMOVE":    false,
    "user_session:LIST":   true,
    "user_session:REVOKE": false
  }
}
```

---

## 6. FE: Angular Service `AuthzContextService`

```typescript
// src/app/core/auth/authz-context.service.ts

import { Injectable, Signal, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthzContextService {
  private http = inject(HttpClient);

  // Grants map trong memory — KHÔNG lưu localStorage
  private _grants = signal<Record<string, boolean>>({});

  /** Gọi một lần sau khi login, trước khi render route đầu tiên */
  loadNavigation(): void {
    this.http
      .post<{ grants: Record<string, boolean> }>('/api/authorization-context', { scope: 'navigation' })
      .subscribe(res => this._grants.set(res.grants));
  }

  /** Gọi sau khi FE fetch xong instance data (instance-level check) */
  loadForInstance(resource: string, data: object): void {
    this.http
      .post<{ grants: Record<string, boolean> }>('/api/authorization-context', { resource, data })
      .subscribe(res => this._grants.update(current => ({ ...current, ...res.grants })));
  }

  /** Computed signal — dùng trực tiếp trong template với @if */
  grant(key: string): Signal<boolean> {
    return computed(() => this._grants()[key] ?? false);
  }

  /** Xóa context khi logout */
  clear(): void {
    this._grants.set({});
  }
}
```

---

## 7. FE: Khai báo trong `UserDetailComponent`

Thêm authorization vào component hiện có. **Chỉ thêm**, không đổi logic nghiệp vụ.

```typescript
// user-detail.ts — chỉ phần thêm mới

import { AuthzContextService } from '../../../core/auth/authz-context.service';
import { GRANTS } from '../../../core/auth/grants';

export class UserDetailComponent implements OnInit {
  // ... existing injections ...
  private authz = inject(AuthzContextService);

  // ── Authorization signals — dùng trực tiếp trong template ──
  canLockUser      = this.authz.grant(GRANTS.USER.LOCK);
  canAssignRole    = this.authz.grant(GRANTS.USER_ROLE.ASSIGN);
  canRemoveRole    = this.authz.grant(GRANTS.USER_ROLE.REMOVE);
  canViewSecurity  = this.authz.grant(GRANTS.USER_SESSION.LIST);
  canRevokeSession = this.authz.grant(GRANTS.USER_SESSION.REVOKE);

  // ── onTabChange: chỉ load security data nếu user có quyền ──
  onTabChange(tabId: string): void {
    if (tabId === 'security' && this.canViewSecurity() && !this.securityLoaded()) {
      this.loadSecurityData();
    }
  }
}
```

---

## 8. FE Template — Khai báo enforcement trong HTML

Pattern chung: `@if (canXxx())`.

```html
<!-- ── HEADER: Lock/Unlock button ── -->
<div class="header-actions">
  @if (canLockUser()) {
    <button mat-flat-button (click)="toggleLock()">
      <mat-icon>{{ user()?.status === 'ACTIVE' ? 'lock' : 'lock_open' }}</mat-icon>
      <span>{{ user()?.status === 'ACTIVE' ? 'Lock User' : 'Unlock User' }}</span>
    </button>
  }
</div>

<!-- ── PROFILE TAB: Assign Roles button ── -->
<div class="section-header-row flex justify-between items-center mb-6">
  <h2>Assigned Roles</h2>
  @if (canAssignRole()) {
    <button mat-stroked-button (click)="openAssignRoles()">
      <mat-icon>add</mat-icon><span>Assign Roles</span>
    </button>
  }
</div>

<!-- ── PROFILE TAB: Remove Role button (per chip) ── -->
@for (role of user()?.roles; track role.id) {
  <div class="role-item">
    <span class="role-name-badge">{{ role.name }}</span>
    @if (canRemoveRole()) {
      <button mat-icon-button (click)="removeRole(role)" matTooltip="Remove role">
        <mat-icon style="font-size: 14px">close</mat-icon>
      </button>
    }
  </div>
}

<!-- ── SECURITY TAB: Force Terminate button ── -->
<ng-container matColumnDef="actions">
  <td mat-cell *matCellDef="let row" class="text-right">
    @if (row.sessionId && canRevokeSession()) {
      <button mat-icon-button color="warn"
              [disabled]="isTerminating(row.sessionId)"
              (click)="openRevokeDialog(row)"
              matTooltip="Force Terminate Session">
        @if (isTerminating(row.sessionId)) {
          <mat-spinner diameter="18"></mat-spinner>
        } @else {
          <mat-icon>power_settings_new</mat-icon>
        }
      </button>
    }
  </td>
</ng-container>

<!-- ── TABS: chỉ render Security tab nếu có quyền ── -->
<mat-tab-group (selectedTabChange)="onTabChange($event.tab.textLabel)">
  <mat-tab label="Profile">
    ...
  </mat-tab>

  @if (canViewSecurity()) {
    <mat-tab label="Security & Devices">
      ...
    </mat-tab>
  }
</mat-tab-group>
```

---

## 9. Kết quả Hiển thị theo từng User Type

| UI Element             | SYSTEM_ADMIN | SUPPORT_AGENT | SERVICE_ADMIN |
|------------------------|:---:|:---:|:---:|
| Tab "Profile"          | ✓   | ✓   | ✓   |
| Btn "Lock/Unlock User" | ✓   | ✓   | ✗ (ẩn hoàn toàn) |
| Btn "Assign Roles"     | ✓   | ✗   | ✗   |
| Btn "Remove Role"      | ✓   | ✗   | ✗   |
| Role chip có nút [x]   | ✓   | ✗ (chip vẫn hiện, chỉ ẩn nút [x]) | ✗ |
| Tab "Security"         | ✓   | ✓   | ✓   |
| Btn "Force Terminate"  | ✓   | ✓   | ✗ (ẩn) |

---

## 10. Lưu ý Quan trọng

### API-level enforcement vẫn phải có
Grant `false` chỉ ẩn button — **không ngăn** user gọi API trực tiếp.
`@PreEnforce` annotation trên BE controller là tầng bảo vệ thật, không thể bỏ.

```java
// AdminUserController.java — vẫn phải có, dù FE đã ẩn button
@PostMapping("/users/{id}/lock")
@PreEnforce
public ResponseEntity<Void> lockUser(@PathVariable String id) { ... }
```

### AuthzContext load trước khi render, không poll

```typescript
// AppComponent hoặc auth guard, sau khi login thành công:
authzContextService.loadNavigation();
// → Sau khi resolve xong mới bắt đầu render route
```

Context chỉ refresh khi user re-login hoặc khi server push notification (WebSocket/SSE) báo policy thay đổi.

### GRANTS constants là contract giữa FE và BE resource catalogue

Nếu đổi resource name hoặc action name ở BE mà không đổi GRANTS constants ở FE → grant key không match → mặc định `false` (deny).
GRANTS constants phải đồng bộ với Resource & Action catalogue trong admin console.
