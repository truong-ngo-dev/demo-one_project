# UC-003: Xác thực Social Login

## Mô tả
Resolve user khi đăng nhập bằng social account — tìm user theo email, tạo mới nếu chưa tồn tại. Được gọi nội bộ bởi oauth2 service sau khi xác thực thành công với social provider.

## Actors
- **oauth2 service**: Gọi sau khi xác thực thành công với social provider.

## Trigger
`POST /api/v1/internal/users/social`

> Endpoint internal — chỉ oauth2 service được gọi, không expose ra public.

## Input
```json
{
  "provider": "GOOGLE",
  "providerUserId": "google-uid-123",
  "providerEmail": "john@gmail.com"
}
```

## Luồng chính

1. Tìm `User` theo `providerEmail`.
2. **Đã tồn tại** → upsert `SocialConnection` (thêm nếu chưa có provider này, bỏ qua nếu đã có).
3. Trả về user hiện tại, `requiresProfileCompletion: false`.

## Luồng thay thế

### A. User chưa tồn tại
- Tại bước 1 → tiếp tục:
- Auto-generate username: `email_prefix + timestamp`.
- Tạo `User` với status `ACTIVE`, password null, `usernameChanged: false`.
- Tạo `SocialConnection` gắn vào User.
- Persist.
- Dispatch `UserCreatedEvent`, `SocialConnectedEvent`.
- Trả về user mới, `requiresProfileCompletion: true`.

## Output
`200 OK` (user đã tồn tại) hoặc `201 Created` (user mới)
```json
{
  "userId": "uuid",
  "username": "john.doe_1735123456789",
  "requiresProfileCompletion": true
}
```

## Điều kiện sau
- User tồn tại trong DB.
- `SocialConnection` được gắn vào User với đúng `provider` + `providerUserId`.
- `requiresProfileCompletion: true` nếu user mới — UI hiển thị nudge nhắc cập nhật username.

## Ghi chú
- Lookup theo `providerEmail` — email là immutable identifier, không thay đổi phía provider.
- `providerUserId` lưu trong `SocialConnection` để định danh chính xác account phía provider, không dùng làm lookup key.
- Upsert `SocialConnection` cho phép cùng 1 user liên kết nhiều provider (Google, GitHub...) với cùng email.
- `usernameChanged: false` khi tạo — user được phép đổi username một lần duy nhất. Sau khi đổi, `usernameChanged: true` và username trở thành immutable.
- Social user không có password — không được yêu cầu password khi xác thực.

## Tham khảo
- [Social Registration Flow](../flows/001_social_registration_flow.md)
- [Domain: User](../domains/user.md)
- [Glossary](../glossary.md)