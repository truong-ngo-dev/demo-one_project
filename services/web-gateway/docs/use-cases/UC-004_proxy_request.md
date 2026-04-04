# UC-004: Proxy Request

## Mô tả
Forward request từ Angular đến backend services, tự động gắn Bearer token via Token Relay.

## Actors
- **Angular**: Gửi request kèm `SESSION` cookie.
- **Backend services**: Nhận request với Bearer token.

## Trigger
Mọi request từ Angular đến `GET|POST|PUT|DELETE /api/**`

## Điều kiện tiên quyết
- Angular đang có `SESSION` cookie hợp lệ.

## Luồng chính

1. Spring Cloud Gateway nhận request + `SESSION` cookie.
2. Verify session hợp lệ trong Redis.
3. Token Relay Filter lấy access token từ `OAuth2AuthorizedClient`.
4. Nếu access token hết hạn → Spring tự động refresh, lưu token mới vào Redis.
5. Gắn `Authorization: Bearer {access_token}` vào request.
6. Strip `/api/` prefix khỏi path.
7. Forward đến backend service theo route config.

## Route config

```properties
# Admin Service
spring.cloud.gateway.routes[0].id=admin-service
spring.cloud.gateway.routes[0].uri=http://admin-service:8082
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/v1/users/**,/api/v1/roles/**
spring.cloud.gateway.routes[0].filters[0]=TokenRelay=

# oauth2 service
spring.cloud.gateway.routes[1].id=oauth2-service
spring.cloud.gateway.routes[1].uri=http://oauth2-service:8081
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/v1/sessions/**,/api/v1/login-activities/**
spring.cloud.gateway.routes[1].filters[0]=TokenRelay=
```

## Luồng thay thế

### A. Session không tồn tại hoặc đã expired
- Tại bước 2: session không tìm thấy trong Redis.
- Trả `401` — Angular redirect về login.

### B. Token refresh thất bại
- Tại bước 4: Refresh Token không hợp lệ hoặc đã bị revoke.
- Clear session, trả `401` — Angular redirect về login.

### C. Backend service không available
- Tại bước 7: backend không phản hồi.
- Trả `503 Service Unavailable`.

## Ghi chú
- Không tự implement Token Relay — dùng `tokenRelay()` filter của Spring Cloud Gateway.
- Route config dùng `application.properties` — chỉ dùng Java DSL khi cần custom logic mà properties không handle được.
- Angular không biết việc token refresh xảy ra — hoàn toàn transparent.

## Tham khảo
- [Domain](../domains/domain.md)
- [Glossary](../glossary.md)