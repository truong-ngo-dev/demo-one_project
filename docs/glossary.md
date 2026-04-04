# Glossary — Global

Định nghĩa các thuật ngữ dùng chung toàn hệ thống. Cập nhật khi có khái niệm mới xuất hiện across services.

> Term riêng của từng service xem tại glossary của service đó.

---

## Token & Authorization

### Access Token
Token ngắn hạn dạng JWT, dùng để truy cập Resource Server. Verify offline qua JWKS — không cần gọi về Authorization Server.

### Refresh Token
Token dạng opaque, dùng để tái cấp Access Token khi hết hạn. Revoke bằng cách xóa Authorization Record tương ứng.

### ID Token
Token dạng JWT theo chuẩn OIDC, mang thông tin identity của user. Client decode trực tiếp để lấy claims.

### Session ID (`sid`)
Claim được thêm vào Access Token và ID Token, trỏ về Authorization Record tương ứng. Dùng để liên kết token với session và phục vụ revocation.
> Giá trị ánh xạ từ primary key của Authorization Record.

---

## Cross-service Conventions

### ID References giữa các service
Các service communicate qua API — ID được truyền dưới dạng **String (UUID)**. Mỗi service tự wrap vào typed Value Object (`UserId`, `RoleId`, v.v.) trong bounded context của mình.

> Không share Value Object code giữa các service — đây là Anti-corruption Layer pattern. Coupling được enforce qua API contract, không phải shared code.

### oauth2 service
Identity Provider (IdP) và Authorization Server của hệ thống. Source of truth cho Sessions, Devices, và Login Activities. Phát hành và quản lý vòng đời token.

### Web Gateway
Backend For Frontend (BFF) — OAuth2 Client duy nhất của hệ thống. Đứng giữa Angular frontend và các backend services. Xử lý toàn bộ auth flow, proxy request, quản lý session trong Redis.

### Admin Service
Service quản lý user profile và phân quyền. Là source of truth cho thông tin identity của user. Các service khác chỉ lưu `userId` làm reference, không copy dữ liệu từ đây.

### Angular
Frontend SPA — không bao giờ nhận hoặc lưu token trực tiếp. Tương tác với hệ thống hoàn toàn qua `SESSION` cookie thông qua Web Gateway.