🎨 DESIGN SYSTEM (IMPORTANT)

Note: This dashboard intentionally overrides the global primary color (Slate #475569 defined in web/CLAUDE.md).
Green is used as the primary accent here for strong visual identity.

Theme:
- Light main layout + dark sidebar

Colors:

Background:
- App background: #f8fafc
- Card background: #ffffff

Sidebar:
- Background: #020617 (very dark)
- Text: #cbd5f5
- Active item background: rgba(34, 197, 94, 0.15)

Primary (Accent):
- Green (success/active): #22c55e
- Green hover: #16a34a

Text:
- Primary: #0f172a
- Secondary: #64748b

Border:
- #e2e8f0 (subtle)

Status colors (for badges, indicators):
- Success: #22c55e
- Warning: #f59e0b
- Error: #ef4444

Design Style:
- Clean, minimal, modern SaaS dashboard
- Strong contrast between sidebar and content
- Use green as main highlight color (NOT blue/slate)

--------------------------------------------------
📐 LAYOUT STRUCTURE

App Shell:
- Sidebar (fixed width: 240px)
- Main area (flex column):
  + Header (top bar)
  + Content

Use Flexbox or CSS Grid for layout.

--------------------------------------------------
📂 SIDEBAR

Style:
- Dark theme
- Width: 240px
- Full height

Structure:
1. Logo / App name: "Apartcom Admin" (top)
2. Navigation sections:
  - Overview
    + Dashboard (mat-icon: dashboard)
  - Management
    + Users (mat-icon: people)
    + Roles (mat-icon: shield)
  - Monitoring
    + Sessions (mat-icon: devices)
    + Login Activities (mat-icon: history)
    + Bottom: Logout button (mat-icon: logout)

Menu Items:
- Use Angular Material `mat-nav-list`
- Each item:
  + Icon + label (horizontal)
  + Padding: 12px 16px
  + Gap between icon and text

States:
- Active:
  + Green highlight
  + Slight background
  + Rounded corners
- Hover:
  + Slight background change

--------------------------------------------------
📌 HEADER (TOP BAR)

Height: 64px

Layout:
- Left:
  + Page title (e.g. "Dashboard")

- Center:
  + Search input (rounded, subtle background)

- Right:
  + Action icons (notification, settings) — placeholder, no logic
  + User avatar — placeholder initials
  + Logout button (bound to logout() method from component)

Style:
- Background: white
- Border bottom: subtle

--------------------------------------------------
📊 MAIN CONTENT

Padding: 24px

Sections:

1. Page Header:
- Title: "System Overview"
- Subtitle: muted text

2. Stats Cards (4 columns on desktop):

Each card:
- Title (small)
- Large number (bold, prominent)
- Trend text (green/red)
- Small mat-icon (top-right)
- All data is static placeholder — no API calls

Suggested cards: Total Users, Active Sessions, Roles, Login Activities

3. Charts Section — PLACEHOLDER ONLY (no chart library):

Left:
- Large card with a CSS-only bar chart simulation (colored divs)
- Label: "Login Activity (7 days)"

Right:
- Status breakdown card with colored progress bars
- Legend:
  + Active (green)
  + Locked (orange)
  + Inactive (red)

4. Bottom Section:
- Recent activity (static table placeholder)
- Quick stats card

--------------------------------------------------
📱 RESPONSIVE

Tablet (< 1024px):
- Stats cards → 2 columns

Mobile (< 768px):
- Sidebar → collapses into drawer
- Header simplified
- Content stacked vertically

--------------------------------------------------
📏 SPACING & ALIGNMENT

- Use 8px spacing system (8, 16, 24, 32)
- Use consistent padding and margins
- Align all elements properly (no misaligned text/icons)
- Use flexbox/grid — avoid random positioning

--------------------------------------------------
🧱 COMPONENT GUIDELINES

- Use Angular Material components:
  + mat-sidenav
  + mat-toolbar
  + mat-card
  + mat-icon
  + mat-list

- Do NOT misuse divs where Material components are better

--------------------------------------------------
🎯 VISUAL GOAL

Make it look like a production-grade SaaS dashboard:
- Clean
- Professional
- Well-aligned
- Balanced spacing
- Clear visual hierarchy

NOT:
- messy layout
- inconsistent spacing
- default Material look

--------------------------------------------------
📁 FILES TO EDIT

Only modify these files — DO NOT touch any .ts files:
- src/app/dashboard/dashboard.html
- src/app/dashboard/dashboard.css

Context files (@-mention vào prompt để Gemini có đủ context):
- src/app/dashboard/dashboard.ts       ← bindings có sẵn, không sửa
- src/app/core/services/auth.service.ts ← để hiểu logout flow
- web/CLAUDE.md                        ← project conventions

--------------------------------------------------
🧾 OUTPUT FORMAT

Return:

1. Angular HTML template
2. CSS styling
3. (Optional) small notes explaining layout decisions

--------------------------------------------------
✨ EXTRA REQUIREMENTS

- Ensure all icons and text are properly aligned
- Ensure sidebar layout is NOT broken
- Ensure consistent spacing everywhere
- Avoid visual clutter
- Prefer subtle UI to flashy UI

--------------------------------------------------
