# Thymeleaf Conversion Guide

This guide documents the conversion from Jinja2 (Flask) templates to Thymeleaf (Spring Boot) templates.

## Key Conversion Patterns

### 1. Template Extension
**Jinja2:**
```jinja
{% extends "base.html" %}
{% block title %}Page Title{% endblock %}
```

**Thymeleaf:**
```html
<html xmlns:th="http://www.thymeleaf.org" 
      th:replace="~{base :: layout(~{::title}, ~{::pageTitle}, ~{::content}, ~{::scripts})}">
<head>
    <title th:fragment="title">Page Title</title>
</head>
<body>
    <div th:fragment="pageTitle">Page Title</div>
    <div th:fragment="content">...</div>
    <div th:fragment="scripts">...</div>
</body>
</html>
```

### 2. URL Generation
**Jinja2:**
```jinja
{{ url_for('index') }}
{{ url_for('edit_client', client_id=client.id) }}
```

**Thymeleaf:**
```html
th:href="@{/}"
th:href="@{/edit-client(clientId=${client.id})}"
```

### 3. Variables
**Jinja2:**
```jinja
{{ variable }}
{{ variable.field }}
```

**Thymeleaf:**
```html
th:text="${variable}"
th:text="${variable.field}"
[[${variable}]]  <!-- Inline text -->
```

### 4. Conditionals
**Jinja2:**
```jinja
{% if condition %}
{% elif other %}
{% else %}
{% endif %}
```

**Thymeleaf:**
```html
th:if="${condition}"
th:unless="${condition}"
th:switch="${variable}"
th:case="'value'"
```

### 5. Loops
**Jinja2:**
```jinja
{% for item in items %}
    {{ item }}
{% endfor %}
```

**Thymeleaf:**
```html
th:each="item : ${items}"
th:each="item, iterStat : ${items}"  <!-- With iteration status -->
```

### 6. Filters/Formatting
**Jinja2:**
```jinja
{{ value|format }}
{{ value|length }}
```

**Thymeleaf:**
```html
th:text="${#numbers.formatDecimal(value, 0, 0)}"
th:text="${#lists.size(list)}"
th:text="${#strings.length(string)}"
```

### 7. Request Parameters
**Jinja2:**
```jinja
{{ request.args.get('search', '') }}
```

**Thymeleaf:**
```html
th:value="${param.search != null ? param.search[0] : ''}"
```

### 8. Flash Messages
**Jinja2:**
```jinja
{% with messages = get_flashed_messages(with_categories=true) %}
    {% for category, message in messages %}
        {{ message }}
    {% endfor %}
{% endwith %}
```

**Thymeleaf:**
```html
<div th:if="${#flashMessages != null and !#flashMessages.isEmpty()}">
    <div th:each="message : ${#flashMessages.all()}">
        <span th:text="${message.value}"></span>
    </div>
</div>
```

### 9. String Operations
**Jinja2:**
```jinja
{{ string[:50] }}
{{ string|length > 50 ? '...' : '' }}
```

**Thymeleaf:**
```html
th:text="${#strings.substring(string, 0, 50)}"
th:text="${#strings.length(string) > 50 ? #strings.substring(string, 0, 50) + '...' : string}"
```

### 10. Ternary Operators
**Jinja2:**
```jinja
{{ 'value1' if condition else 'value2' }}
```

**Thymeleaf:**
```html
th:text="${condition ? 'value1' : 'value2'}"
th:class="${condition ? 'class1' : 'class2'}"
```

## Common Spring Boot Model Attributes

When converting, ensure your Spring controllers provide these model attributes:

- `title` - Page title
- `pageTitle` - Page heading
- `clients` - List of clients
- `stats` - Statistics object
- `currentBuilding` - Current building object
- `apartments` - List of apartments
- Flash messages via `RedirectAttributes.addFlashAttribute()`

## URL Mapping

Common URL patterns converted:
- `/` → `@{/}`
- `/clients` → `@{/clients}`
- `/add-client` → `@{/add-client}`
- `/edit-client/{id}` → `@{/edit-client(clientId=${id})}`
- `/delete-client/{id}` → `@{/delete-client(clientId=${id})}`

## Notes

1. All templates must include `xmlns:th="http://www.thymeleaf.org"` in the root element
2. Use `th:replace` for layout inclusion
3. Use `th:fragment` to define replaceable sections
4. Use `th:text` for text content (escapes HTML)
5. Use `[[${variable}]]` for inline unescaped text
6. Use `th:utext` for unescaped HTML content (use with caution)

