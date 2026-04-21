Plan                                                                                                                                                                                                      
BE — AdminPolicyProvider                                                                              - Inject HttpServletRequest, đọc header X-Portal-Scope                                                - Map sang Scope enum, tìm policy set tương ứng thay vì hardcode Scope.ADMIN                          - Fallback: nếu không có header hoặc không tìm thấy policy set cho scope đó → trả empty               DENY_OVERRIDES set

FE — Angular interceptor
- File mới: web/src/app/core/interceptors/portal-scope.interceptor.ts
- Đọc Router.url để detect pattern: /admin/ → ADMIN, /operator/ → OPERATOR
- Gắn X-Portal-Scope: <SCOPE> nếu detect được, bỏ qua nếu không
- Register vào app.config.ts

Docs
- services/admin/SERVICE_MAP.md — ghi nhận contract header X-Portal-Scope
