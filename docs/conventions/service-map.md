# Service Map Convention

> **Dành cho AI agents**: File `SERVICE_MAP.md` tại root của mỗi service là điểm dừng chân đầu tiên (First entry point). 
> Hãy đọc file này để hiểu ý đồ kiến trúc, danh sách nghiệp vụ (Use Cases) và các thành phần Infrastructure quan trọng trước khi sử dụng các công cụ tìm kiếm file.

---

## Mục đích
Mục tiêu của `SERVICE_MAP.md` là cung cấp một bản đồ điều hướng mức cao, tập trung vào **chức năng** thay vì liệt kê file thuần túy.

## Cấu trúc mẫu (`SERVICE_MAP.md`)

Mỗi service phải có một file `SERVICE_MAP.md` tuân theo cấu trúc sau:

### 📂 1. Domain Layer (`domain/`)
*Chỉ nêu tên package Aggregate và liệt kê các "Chủ xị" (Aggregate Root) cùng "Hợp đồng" (Ports).*

- **{aggregate_name}**: [Mô tả ngắn vai trò aggregate này]
    - `Root`: {AggregateRootClass} (Chứa logic bảo vệ invariant chính)
    - `Port`: {Aggregate}Repository (Interface định nghĩa các thao tác lưu trữ)
    - `ErrorCode`: {Aggregate}ErrorCode (Danh mục lỗi nghiệp vụ của aggregate)
    - `Domain Service`: {Aggregate}Service (Nếu có logic phối hợp nhiều aggregate)

### 🚀 2. Application Layer (`application/`)
*Bản đồ Use Cases theo Vertical Slice. Mỗi folder slice tự chứa Command/Query, Handler và Mapper.*

- **{aggregate_name}**:
    - `{use_case_folder}`: [Mô tả intent]. (Ví dụ: `register/`: Đăng ký tài khoản qua Email/Password)
    - `{use_case_folder}`: [Mô tả intent]. (Ví dụ: `find_by_id/`: Truy vấn thông tin chi tiết)

### 🛠️ 3. Infrastructure Layer (`infrastructure/`)
*Mức độ chi tiết khác nhau tùy theo thành phần:*

#### 📦 Persistence (Khái quát)
- **Cơ chế**: [Ví dụ: MySQL + Spring Data JPA]
- **Entity mapping**:
    - `{AggregateRoot}` ↔ `{RootJpaEntity}` → bảng `{root_table}`
        - `{ChildEntity}` ↔ `{ChildJpaEntity}` → bảng `{child_table}` *(child entity, nếu có)*
        - `{ValueObject}` → embedded trong `{root_table}` | bảng `{vo_table}` *(nếu tách bảng)*

#### 🔐 Security (BẮT BUỘC chi tiết từng Class)
*Đây là phần nhạy cảm, cần mô tả rõ trách nhiệm của từng class thực tế trong service:*
- `{ClassA}`: [Trách nhiệm]. (Ví dụ: `JwtAuthenticationFilter`: Verify token từ header)
- `{ClassB}`: [Trách nhiệm]. (Ví dụ: `SecurityConfiguration`: Phân quyền endpoint và cấu hình CORS)
- `{ClassC}`: [Trách nhiệm]. (Ví dụ: `TokenProvider`: Logic sinh và giải mã JWT)

#### 🔌 Adapters (Chi tiết theo chức năng)
- `adapter/repository/`: Implementation của các Repository Ports.
- `adapter/service/`: Implementation của các External Capability Ports. Ghi rõ implement Port nào, dùng client nào từ `api/`.
    - `{ConceptAdapter}`: implement `{ConceptPort}` — dùng `{Client}` từ `api/{protocol}/{internal|external}/{service}/`
- `adapter/query/`: Implementation của Query Ports (Read side) trả về trực tiếp DTO.

#### 🌐 Outbound Clients — `api/` (Nếu có)
*Liệt kê khi service gọi sang service khác hoặc third-party. Xem chi tiết convention tại [`ddd-structure.md`](./ddd-structure.md).*
- `api/http/internal/{service}/`
    - `{Service}Client`: [các method + endpoint + use case phục vụ]
    - *DTOs dùng từ `libs/shared` nếu là shared contract — không tạo `dto/` riêng trong trường hợp này*
- `api/http/external/{provider}/`
    - `{Provider}Client`: [các method + endpoint + use case phục vụ]
    - `dto/`: HTTP DTOs riêng của provider *(chỉ tạo khi không có trong `libs/shared`)*
- `api/grpc/internal/{service}/`: *(nếu dùng gRPC)*

#### 📨 Messaging & Pipeline (Nếu có)
- Mô tả các Kafka Producers/Consumers hoặc các Job `@Scheduled` quan trọng.

### 🖥️ 4. Presentation Layer (`presentation/`)
- **{aggregate_name}**:
    - `{Aggregate}Controller`: Danh sách các REST Endpoints chính.
    - `model/`: Các Request/Response DTOs dành riêng cho API.

### 📄 5. Resources & Configs
- `application.properties` / `application.yml`: Chứa các cấu hình quan trọng (API prefix, JWT secrets, DB connection).
- `db/migration/`: Quản lý schema database.

---

## Quy tắc biên soạn cho con người
1. **Đừng liệt kê file lặp lại**: Nếu folder `application/user/register/` đã có `RegisterUserHandler.java` theo đúng convention, không cần ghi tên file đó vào Map.
2. **Tập trung vào "Tại sao"**: Giải thích ý đồ của một Domain Service hoặc một Security Filter phức tạp.
3. **Cập nhật**: Xem quy tắc cập nhật tài liệu tại [CLAUDE.md root](../../CLAUDE.md#doc-maintenance).
