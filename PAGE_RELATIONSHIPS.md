# Връзки между страниците в приложението

**Дата**: 2025-12-25  
**Цел**: Документиране на навигационната структура и връзките между страниците

---

## 🗺️ Обща структура на приложението

Приложението е разделено на **4 основни секции** базирани на роли и достъп:

1. **Публична секция** (Public) - Без автентификация
2. **Потребителска секция** (User) - След login
3. **Агентска секция** (Agent) - За агенти
4. **Административна секция** (Admin) - За администратори

---

## 📍 Точка на влизане

```
/ (root)
  ↓
  redirect:/index
  ↓
  /index → dashboard.html
```

**SecurityConfig**: След успешен login → `/index` (dashboard)

---

## 🌐 Публична секция (Public)

### Навигация:
```
/public/home (Начало)
  ├─ /public/properties (Имоти)
  │   └─ /public/properties/{id} (Детайли за имот)
  ├─ /public/about (За нас)
  └─ /public/contact (Контакти)
```

### Връзки:
- **Начало** → Имоти, За нас, Контакти
- **Имоти** → Детайли за имот → Inquiry форма
- **Контакти** → Contact form → Redirect към /public/contact
- **Login/Register** → Доступни от навигацията

### Контролер: `PublicController`
- `/public/home` → `public/home.html`
- `/public/properties` → `public/property-list.html`
- `/public/properties/{id}` → `public/property-details.html`
- `/public/about` → `public/about.html`
- `/public/contact` → `public/contact.html`

---

## 👤 Потребителска секция (User)

### Навигация:
```
/auth/login → login.html
  ↓ (след успешен login)
  /index → dashboard.html
  ↓
  /user/dashboard (Потребителски dashboard)
    ├─ /user/profile (Профил)
    └─ /user/inquiries (Моите запитвания)
        └─ /user/inquiries/{id} (Детайли за запитване)
```

### Връзки:
- **Login** → `/index` (dashboard)
- **Dashboard** → Profile, Inquiries
- **Profile** → Update → Redirect към `/user/profile`
- **Inquiries** → Inquiry details

### Контролер: `UserController`
- `/user/dashboard` → `user/dashboard.html`
- `/user/profile` → `user/profile.html`
- `/user/inquiries` → `user/inquiry-history.html`
- `/user/inquiries/{id}` → `user/inquiry-details.html`

---

## 🏢 Агентска секция (Agent)

### Навигация:
```
/agent/dashboard (Агентски dashboard)
  ├─ /agent/properties (Моите имоти)
  │   ├─ /agent/properties/create (Създай имот)
  │   ├─ /agent/properties/edit/{id} (Редактирай имот)
  │   └─ /agent/properties/delete/{id} (Изтрий имот)
  ├─ /agent/inquiries (Запитвания)
  │   └─ /agent/inquiries/{id} (Детайли за запитване)
  └─ /agent/profile (Профил)
```

### Връзки:
- **Dashboard** → Properties, Inquiries, Profile
- **Properties** → Create, Edit, Delete → Redirect към `/agent/properties`
- **Inquiries** → Respond, Change status → Redirect към `/agent/inquiries/{id}`

### Контролер: `AgentController`
- `/agent/dashboard` → `agent/dashboard.html`
- `/agent/properties` → `agent/properties.html`
- `/agent/properties/create` → `agent/property-form.html`
- `/agent/properties/edit/{id}` → `agent/property-form.html`
- `/agent/inquiries` → `agent/inquiries.html`
- `/agent/inquiries/{id}` → `agent/inquiry-details.html`
- `/agent/profile` → `agent/profile.html`

---

## 👑 Административна секция (Admin)

### Навигация:
```
/admin/dashboard (Админ dashboard)
  ├─ /admin/users (Потребители)
  │   └─ /admin/users/{id} (Детайли за потребител)
  │       ├─ Activate/Deactivate → Redirect към /admin/users/{id}
  │       └─ Change role → Redirect към /admin/users/{id}
  ├─ /admin/inquiries (Всички запитвания)
  │   └─ /admin/inquiries/{id} (Детайли за запитване)
  └─ /admin/system (Системен мониторинг)
```

### Връзки:
- **Dashboard** → Users, Inquiries, System
- **Users** → User details → Activate/Deactivate/Role change
- **Inquiries** → Inquiry details

### Контролер: `AdminController`
- `/admin/dashboard` → `admin/dashboard.html`
- `/admin/users` → `admin/users.html`
- `/admin/users/{id}` → `admin/user-details.html`
- `/admin/inquiries` → `admin/inquiries.html`
- `/admin/inquiries/{id}` → `admin/inquiry-details.html`
- `/admin/system` → `admin/system-health.html`

---

## 🏠 Legacy секция (Управление на апартаменти)

### Навигация:
```
/index (Dashboard)
  ├─ /apartments (Списък с апартаменти)
  ├─ /add-apartment (Добави апартамент)
  ├─ /clients (Клиенти)
  │   └─ /add-client (Добави клиент) → Redirect към /clients
  ├─ /add-building (Добави сграда)
  ├─ /buildings (Сгради)
  ├─ /payments (Плащания) ⚠️ ЛИПСВА ТЕМПЛЕЙТ
  ├─ /excel (Excel операции)
  │   ├─ /excel/export (Експорт)
  │   └─ /excel/import (Импорт) → import_excel.html
  └─ /contract (Договори)
      ├─ /contract/download/{id}
      └─ /contract/preview/{id}
```

### Връзки от Dashboard:
- **Dashboard** → Apartments, Add Apartment, Clients, Add Building, Excel Export
- **Apartments** → Edit, Delete, Add Payment, View Payments
- **Clients** → Add Client, Edit, Delete, Search
- **Add Client** → Save → Redirect към `/clients`
- **Buildings** → Add Building

### Контролери:
- **MainController**: `/`, `/index`, `/add-apartment`, `/add-building`, `/add-client`
- **ApartmentController**: `/apartments`, `/apartments/api/**`
- **ClientController**: `/clients`, `/clients/api/**`
- **BuildingController**: `/buildings`, `/buildings/add`
- **PaymentController**: `/payments`, `/payments/api/**` ⚠️
- **ExcelController**: `/excel`, `/excel/export`, `/excel/import`
- **ContractController**: `/contract/download/{id}`, `/contract/preview/{id}`

---

## 🔄 Redirect потоки

### Автентификация:
```
/auth/login → (success) → /index
/auth/register → (success) → /auth/login
/auth/logout → /
```

### Форми:
```
/add-client (POST) → /clients
/user/profile (POST) → /user/profile
/agent/properties/create (POST) → /agent/properties
/agent/properties/edit/{id} (POST) → /agent/properties
/agent/inquiries/{id}/respond (POST) → /agent/inquiries/{id}
/admin/users/{id}/activate (POST) → /admin/users/{id}
/admin/users/{id}/role (POST) → /admin/users/{id}
```

### Inquiry поток:
```
/public/properties/{id} → Submit inquiry → /public/properties/{id}
/user/inquiries → View details → /user/inquiries/{id}
/agent/inquiries → View details → /agent/inquiries/{id}
```

---

## 🔐 Роли и достъп

### Публичен достъп (без login):
- `/public/**`
- `/auth/login`, `/auth/register`
- `/api/properties/**`

### Аутентифицирани потребители (USER, AGENT, ADMIN):
- `/index` (dashboard)
- `/user/**`
- `/apartments/**`, `/clients/**`, `/payments/**`
- `/add-apartment`, `/add-building`, `/add-client`
- `/buildings/**`

### Агенти и Администратори (AGENT, ADMIN):
- `/agent/**`
- `/properties/create`, `/properties/edit/**`

### Само Администратори (ADMIN):
- `/admin/**`
- `/system/**`

---

## 📊 Диаграма на връзките

```
                    ┌─────────────┐
                    │   / (root)  │
                    └──────┬──────┘
                           │ redirect
                           ↓
                    ┌─────────────┐
                    │   /index     │
                    │  (dashboard)│
                    └──────┬───────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ↓                  ↓                  ↓
   ┌─────────┐      ┌──────────┐      ┌──────────┐
   │ Public  │      │  User    │      │  Agent   │
   │ Section │      │ Section  │      │ Section  │
   └────┬────┘      └────┬─────┘      └────┬─────┘
        │                │                  │
        ↓                ↓                  ↓
   /public/home    /user/dashboard    /agent/dashboard
   /public/props   /user/profile     /agent/properties
   /public/about   /user/inquiries   /agent/inquiries
   /public/contact                  /agent/profile
        │
        ↓
   /auth/login ──────────┐
        │                 │
        ↓                 │
   /auth/register         │
        │                 │
        └─────────────────┘
              (login)
                  │
                  ↓
         ┌────────────────┐
         │  Role-based     │
         │  Dashboard      │
         └────────────────┘
                  │
        ┌─────────┼─────────┐
        │         │         │
        ↓         ↓         ↓
    USER      AGENT      ADMIN
    /user/    /agent/    /admin/
```

---

## 🔗 Ключови навигационни пътища

### 1. Потребителски поток (User):
```
Login → Dashboard → Profile → Inquiries → Inquiry Details
```

### 2. Агентски поток (Agent):
```
Login → Agent Dashboard → Properties → Create/Edit → Inquiries → Respond
```

### 3. Административен поток (Admin):
```
Login → Admin Dashboard → Users → User Details → Manage → Inquiries → System
```

### 4. Публичен поток (Public):
```
Home → Properties → Property Details → Submit Inquiry → Login/Register
```

### 5. Legacy поток (Apartments Management):
```
Dashboard → Apartments → Edit/Delete → Clients → Add Client → Payments
```

---

## ⚠️ Проблеми и липсващи връзки

### Липсващи темплейти:
1. **payments.html** - използван от `PaymentController.paymentsPage()`
   - Endpoint: `GET /payments` → `return "payments"`
   - Връзка: Dashboard → Payments (не работи)

2. **system_reset.html** - използван от `SystemResetController.resetPage()`
   - Endpoint: `GET /system/reset` → `return "system_reset"`
   - Връзка: Admin → System Reset (не работи)

### Неизползвани връзки в base.html:
- `/payment-plans` - няма контролер
- `/upload-apartments` - няма контролер
- `/export-data` - няма контролер
- `/building-stage` - няма контролер
- `/global-stage-change` - функционалността е в apartments.html
- `/backup` - няма контролер
- `/restore-backup` - няма контролер

---

## 📝 Резюме

### Основни секции:
1. **Public** - 5 страници (home, properties, property-details, about, contact)
2. **User** - 4 страници (dashboard, profile, inquiries, inquiry-details)
3. **Agent** - 6 страници (dashboard, properties, property-form, inquiries, inquiry-details, profile)
4. **Admin** - 6 страници (dashboard, users, user-details, inquiries, inquiry-details, system-health)
5. **Legacy** - 10+ страници (dashboard, apartments, clients, buildings, payments, excel, contract)

### Навигационни модели:
- **Top Navigation** - в публичните страници
- **Sidebar Navigation** - в base.html (неизползван)
- **Dashboard Cards** - в dashboard.html
- **Breadcrumbs** - липсват
- **Role-based Menus** - в публичните страници

### Redirect стратегия:
- Форми → Redirect към списък или детайли
- Login → Redirect към `/index`
- Logout → Redirect към `/`
- Успешни операции → Flash message + Redirect

---

## 🎯 Препоръки

1. **Създаване на липсващите темплейти**:
   - `payments.html`
   - `system_reset.html`

2. **Обединяване на навигацията**:
   - Премахване на base.html (неизползван)
   - Създаване на общ navigation component

3. **Добавяне на breadcrumbs**:
   - За по-добра навигация в сложни секции

4. **Оптимизация на redirects**:
   - Използване на flash messages за feedback
   - Консистентни redirect пътища



