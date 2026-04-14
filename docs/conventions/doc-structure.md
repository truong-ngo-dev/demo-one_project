# Convention: Tổ chức tài liệu

## Nguyên tắc phân tầng

```
Root docs/          → cross-service: kiến trúc, convention, quyết định, context sản phẩm
Service docs/       → implementation-specific: use case, domain model, flow, quyết định kỹ thuật
```

**Rule nhanh**: Tài liệu ảnh hưởng ≥ 2 service → `docs/`. Chỉ 1 service → `services/{service}/docs/`.

---

## Root `docs/` — cấu trúc và mục đích

```
docs/
├── architecture/       ← Kiến trúc tổng thể cross-service (current truth)
├── conventions/        ← Coding standards, patterns áp dụng cho toàn project
├── decisions/          ← Cross-service ADR (Architecture Decision Records)
├── api/                ← Centralized API catalog (YAML specs các service)
├── context/            ← Product thinking
│   ├── final_design/   ← Quyết định đã chốt, đang là current truth
│   └── analysis/       ← Hành trình phân tích (có thể outdated)
└── glossary.md         ← Chỉ chứa term dùng cross-service
```

### `context/final_design/`
Chứa quyết định thiết kế đã được chốt. Khi implement xong và behavior đã được document trong service docs → file ở đây có thể đánh dấu `IMPLEMENTED` nhưng vẫn giữ lại làm reference cho *tại sao* thiết kế vậy.

### `context/analysis/`
Chứa quá trình khám phá, phân tích, so sánh options. Không nhất thiết phản ánh thiết kế hiện tại. Khi một file bị superseded bởi quyết định mới, thêm header:

```markdown
> **Status: SUPERSEDED** — Xem [link tới doc hiện tại]
```

### `decisions/` (ADR)
Dùng cho quyết định cross-service có ảnh hưởng lớn. Format tối thiểu:
- Bối cảnh (context)
- Quyết định (decision)
- Hệ quả (consequences)

Không cần ADR cho mọi thứ — chỉ khi quyết định không self-evident từ code.

---

## Service `docs/` — cấu trúc chuẩn

```
services/{service}/docs/
├── use-cases/          ← UC-NNN files, 1 file per use case
├── domains/            ← Domain model spec (aggregate, entity, VO, invariant)
├── flows/              ← Cross-component flows (sequence, state)
├── decisions/          ← Service-level ADR
├── testing/            ← Test data, fixtures, test scenarios
└── glossary.md         ← Term nội bộ service (không dùng cross-service)
```

### Glossary phân cấp
- Term dùng cross-service (Subject, PolicySet, RoleContext…) → `docs/glossary.md`
- Term nội bộ domain (ví dụ: "registration method" trong admin service) → service `glossary.md`

---

## Những gì KHÔNG nên lưu vào docs

| Loại | Lưu ở đâu |
|------|-----------|
| Quyết định hiển nhiên từ code | Không cần doc |
| Lịch sử thay đổi | Git log + commit message |
| Trạng thái in-progress task | Tasks/issues |
| Config, credentials | Không commit |
| Test data cụ thể cho 1 service | `services/{service}/docs/testing/` |

---

## Ứng xử với tài liệu outdated

Không xóa — thêm status header:

```markdown
> **Status: SUPERSEDED** — Quyết định hiện tại tại [link]
> **Status: IMPLEMENTED** — Đã implement tại [service/commit]
> **Status: DRAFT** — Chưa chốt, đang thảo luận
```

File trong `context/analysis/` mặc định là "hành trình tư duy" — không cần label trừ khi gây nhầm lẫn.
