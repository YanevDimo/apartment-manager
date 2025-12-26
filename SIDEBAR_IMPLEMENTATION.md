# Sidebar Implementation Guide

## ✅ Completed
1. Created `fragments/sidebar.html` with all sidebar components
2. Added sidebar to `apartments.html`
3. Added sidebar to `clients.html`

## 📋 Pages that need sidebar added:

### Main Pages:
- [x] dashboard.html (already has sidebar)
- [x] apartments.html
- [x] clients.html
- [ ] buildings.html
- [ ] add_apartment.html
- [ ] add_client.html
- [ ] add_building.html
- [ ] edit_apartment.html
- [ ] edit_client.html
- [ ] edit_building.html

### User Pages:
- [ ] user/dashboard.html
- [ ] user/profile.html
- [ ] user/inquiry-history.html
- [ ] user/inquiry-details.html

### Agent Pages:
- [ ] agent/dashboard.html
- [ ] agent/properties.html
- [ ] agent/property-form.html
- [ ] agent/inquiries.html
- [ ] agent/inquiry-details.html
- [ ] agent/profile.html

### Admin Pages:
- [ ] admin/dashboard.html
- [ ] admin/users.html
- [ ] admin/user-details.html
- [ ] admin/inquiries.html
- [ ] admin/inquiry-details.html
- [ ] admin/system-health.html

## How to add sidebar to a page:

1. Add Thymeleaf namespace:
```html
<html lang="bg" xmlns:th="http://www.thymeleaf.org">
```

2. Add sidebar styles in `<head>`:
```html
<!-- Sidebar Styles -->
<th:block th:replace="~{fragments/sidebar :: sidebarStyles}"></th:block>
```

3. Add sidebar components after `<body>`:
```html
<!-- Sidebar Toggle Button (Mobile) -->
<th:block th:replace="~{fragments/sidebar :: sidebarToggle}"></th:block>

<!-- Sidebar -->
<th:block th:replace="~{fragments/sidebar :: sidebar}"></th:block>

<!-- Main Content -->
<div class="main-content" id="mainContent">
```

4. Wrap existing content in `main-content` div

5. Add sidebar script before `</body>`:
```html
<!-- Sidebar Script -->
<th:block th:replace="~{fragments/sidebar :: sidebarScript}"></th:block>
</div> <!-- End main-content -->
```

## Notes:
- Login and register pages should NOT have sidebar
- Public pages (public/*) can have sidebar but it's optional
- All authenticated pages should have sidebar


