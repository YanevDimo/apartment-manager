# Поправка: Автоматично създаване на Agent записи

**Дата**: 2025-12-25  
**Проблем**: Няма агенти в базата данни, дори когато потребителите имат роля AGENT.

---

## Проблем

Когато потребител се регистрира или когато администратор променя ролята на потребител на AGENT, не се създава автоматично запис в таблицата `agents`. Това води до грешки, когато потребител с роля AGENT се опита да достъпи агентски панел.

---

## Решение

Добавена е логика за автоматично създаване на Agent запис в следните случаи:

### 1. При промяна на роля на AGENT (AdminController)
- Когато администратор променя ролята на потребител на AGENT, автоматично се създава Agent запис с лицензен номер `AGENT-{userId}`.

### 2. При достъп до агентски панел (AgentController)
- Когато потребител с роля AGENT се опита да достъпи агентски панел и няма Agent запис, автоматично се създава такъв.
- Приложено в следните endpoints:
  - `/agent/dashboard`
  - `/agent/properties`
  - `/agent/inquiries`
  - `/agent/profile`

### 3. При регистрация (UserServiceImpl)
- Ако потребител се регистрира с роля AGENT (въпреки че по подразбиране е USER), автоматично се създава Agent запис.

---

## Промени в кода

### UserServiceImpl.java
- Добавен `AgentService` dependency
- В `registerUser()`: Ако ролята е AGENT, автоматично се създава Agent запис
- В `changeUserRole()`: Ако ролята се променя на AGENT и няма Agent запис, автоматично се създава такъв

### AdminController.java
- Добавен `AgentService` dependency
- В `changeUserRole()`: Преди промяна на ролята, проверява дали трябва да се създаде Agent запис

### AgentController.java
- Всички методи, които изискват Agent запис, използват `orElseGet()` за автоматично създаване, ако записът не съществува

---

## Как работи

1. **Автоматично създаване при достъп до панел:**
   ```java
   Agent agent = agentService.getAgentByUserId(user.getId()).orElseGet(() -> {
       String defaultLicense = "AGENT-" + user.getId();
       return agentService.createAgentFromUser(user, defaultLicense);
   });
   ```

2. **Автоматично създаване при промяна на роля:**
   ```java
   if (newRole == User.Role.AGENT && !agentService.getAgentByUserId(id).isPresent()) {
       String defaultLicense = "AGENT-" + id;
       agentService.createAgentFromUser(user, defaultLicense);
   }
   ```

---

## Резултат

✅ Потребителите с роля AGENT вече могат да достъпят агентския панел без проблеми  
✅ Agent записите се създават автоматично, когато е необходимо  
✅ Лицензният номер може да се редактира по-късно в профила на агента  

---

## Бележки

- Лицензният номер по подразбиране е `AGENT-{userId}`, който може да се редактира в профила на агента
- Ако вече съществува Agent запис, не се създава нов
- Всички промени са обратно съвместими


