# CLAUDE.md — one-project

Đây là hệ thống IAM (Identity & Access Management) xây dựng theo Microservices + DDD + CQRS.
Đọc file này trước, sau đó follow link đến component cần làm việc.

---

## Cấu trúc project

```
one-project/
├── services/
│   ├── oauth2/             # Authentication service (Spring Authorization Server)
│   ├── web-gateway/        # Backend For Frontend — OAuth2 Client, request proxy
│   └── admin/              # User, role management
├── libs/
│   ├── common/             # EventDispatcher, EventHandler, DomainException, ErrorCode
│   └── shared/             # [PLANNED] Shared business concepts (shared kernel) — chưa có gì, không tự ý thêm vào
└── web/                    # Angular frontend
```

→ Kiến trúc tổng thể: [`docs/architecture/overview.md`](docs/architecture/overview.md)

---

## Conventions — đọc trước khi sinh code

| Convention                                 | File                                                                       |
|--------------------------------------------|----------------------------------------------------------------------------|
| Service internal mapping                   | **`SERVICE_MAP.md`** (tại thư mục gốc của từng service)                    |
| Package structure, DDD, CQRS, Event        | [`docs/conventions/ddd-structure.md`](docs/conventions/ddd-structure.md)   |
| API design, URL, response format           | [`docs/conventions/api-design.md`](docs/conventions/api-design.md)         |
| Error handling, ErrorCode, DomainException | [`docs/conventions/error-handling.md`](docs/conventions/error-handling.md) |
| Testing, layer strategy, fixture           | [`docs/conventions/testing.md`](docs/conventions/testing.md)               |

---

## Thông tin từng component

| Component      | Chi tiết                                                           |
|----------------|--------------------------------------------------------------------|
| Web gateway    | [`services/web-gateway/CLAUDE.md`](services/web-gateway/CLAUDE.md) |
| oauth2 service | [`services/oauth2/CLAUDE.md`](services/oauth2/CLAUDE.md)           |
| admin service  | [`services/admin/CLAUDE.md`](services/admin/CLAUDE.md)             |
| libs/common    | [`libs/common/CLAUDE.md`](libs/common/CLAUDE.md)                   |
| libs/shared    | [`libs/shared/CLAUDE.md`](libs/shared/CLAUDE.md)                   |
| web            | [`web/CLAUDE.md`](web/CLAUDE.md)                                   |

---

## Stack chung

|              |                             |
|--------------|-----------------------------|
| Language     | Java 21                     |
| Framework    | Spring Boot 4.x             |
| Build tool   | Maven                       |
| Database     | MySQL                       |
| Base package | `vn.truongngo.apartcom.one` |

---

## Doc Maintenance
Khi implement task có phát sinh thay đổi về domain, behavior, hoặc architectural decision hoặc là cập nhật trạng thái các use case —
cập nhật doc liên quan ngay trong cùng task, không để lại sau.

---

## Quy tắc cứng — không được vi phạm

1. **Đọc convention trước khi sinh code** — đặc biệt `ddd-structure.md`
2. **Tính năng `[PLANNED]`** — không tự sinh code, hỏi lại trước
3. **Không truy cập DB chéo** giữa các services
4. **Không import code trực tiếp** giữa các services — dùng `libs/`. `libs/shared` chỉ dành cho business shared concept (shared kernel) — utility và infrastructure đã có `libs/common`, không đưa vào `libs/shared`
5. **Không dùng `@SpringBootTest`** cho unit test domain và application layer
6. **Thêm field vào DB** — phải có migration file, không sửa schema trực tiếp
7. **Web không lưu token** — mọi auth logic nằm ở Web gateway
8. **Cập nhật tài liệu khi thay đổi lớn** — chỉ update `.md` khi thay đổi ảnh hưởng đến cách các thành phần khác tương tác: thêm/xoá use case, thay đổi API contract, thêm/xoá aggregate, thay đổi convention, thêm/xoá lib trong `libs/common`. Không cần update khi chỉ sửa property nội bộ, refactor implementation, hay thêm field nhỏ không ảnh hưởng ra ngoài.