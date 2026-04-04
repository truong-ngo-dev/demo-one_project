You are a senior UI/UX-focused frontend engineer.

Restyle the following files following the design system in @web/docs/layout/dashboard.md.

---

## CONSTRAINTS

- DO NOT modify any .ts files
- DO NOT modify Thymeleaf logic, th:* attributes, form action, input names, or JavaScript in login.html
- DO NOT add new Angular Material imports (only use what's already in the component's imports[])
- DO NOT add new libraries
- DO NOT use inline styles (except dynamic values)
- Tailwind is available for layout/spacing — Angular Material for components

---

## FILES TO RESTYLE

### 1. Angular Login Page
Files: @web/src/app/login/login.html + @web/src/app/login/login.css
Context (DO NOT modify): @web/src/app/login/login.ts

Goal: Centered login card, consistent with the design system.
- Full viewport height, centered vertically and horizontally
- Background: #f8fafc
- Card: white, max-width 380px, subtle shadow
- Title: "Apartcom Admin"
- Subtitle: "Đăng nhập để tiếp tục"
- Button: full width, green (#22c55e), hover (#16a34a)
- Available bindings: (click)="login()"

---

### 2. Angular Dashboard
Files: @web/src/app/dashboard/dashboard.html + @web/src/app/dashboard/dashboard.css
Context (DO NOT modify): @web/src/app/dashboard/dashboard.ts

Goal: Full dashboard layout per spec in @web/docs/layout/dashboard.md.
- Available bindings: (click)="logout()"
- All data is static placeholder

---

### 3. OAuth2 Login Page (Thymeleaf)
File: @services/oauth2/src/main/resources/templates/login.html

Goal: Match the design system visually — same card style as Angular login page.
- Add a <style> block inside <head> — no external CSS file
- MUST preserve: form action="/login", all input name attributes,
  th:* attributes, the JavaScript block at the bottom
- Card layout: centered, max-width 380px
- Username + password inputs: full width, styled consistently
- Submit button: full width, green (#22c55e)
- Error message (param.error): red text, subtle background
- Locked message (param.locked): orange text, subtle background

---

## OUTPUT FORMAT

For each file, return the complete updated file content.
Add a short note if you make a non-obvious layout decision.
