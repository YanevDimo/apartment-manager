# Решение на SSL/TLS Грешка

## Проблем

Грешката `Invalid character found in method name [0x160x030x010x07...]` означава, че сървърът получава SSL/TLS трафик на HTTP порт.

## Причини

1. **Браузърът се опитва да се свърже чрез HTTPS** вместо HTTP
2. **Browser cache** има записано HTTPS redirect
3. **Proxy или Load Balancer** пренасочва HTTPS трафик
4. **HSTS (HTTP Strict Transport Security)** принуждава HTTPS

## Решения

### Решение 1: Използвайте HTTP (не HTTPS)

**Важно**: Използвайте `http://` не `https://`

```
✅ ПРАВИЛНО: http://localhost:8080/
❌ ГРЕШНО:   https://localhost:8080/
```

### Решение 2: Изчистете Browser Cache

#### Chrome/Edge:
1. Натиснете `Ctrl + Shift + Delete`
2. Изберете "Cached images and files"
3. Изберете "All time"
4. Натиснете "Clear data"

#### Firefox:
1. Натиснете `Ctrl + Shift + Delete`
2. Изберете "Cache"
3. Натиснете "Clear Now"

### Решение 3: Изчистете HSTS Settings

#### Chrome/Edge:
1. Отидете на `chrome://net-internals/#hsts` (или `edge://net-internals/#hsts`)
2. В "Delete domain security policies" въведете: `localhost`
3. Натиснете "Delete"

#### Firefox:
1. Отидете на `about:config`
2. Търсете `security.tls.insecure_fallback_hosts`
3. Добавете `localhost`

### Решение 4: Използвайте Incognito/Private Mode

Отворете браузъра в Incognito/Private режим и опитайте отново:
- Chrome: `Ctrl + Shift + N`
- Firefox: `Ctrl + Shift + P`
- Edge: `Ctrl + Shift + N`

### Решение 5: Проверете URL в Browser

Уверете се, че използвате правилния URL:
- ✅ `http://localhost:8080/`
- ✅ `http://127.0.0.1:8080/`
- ❌ `https://localhost:8080/` (НЕ използвайте HTTPS!)

### Решение 6: Рестартирайте Application

След промените в конфигурацията, рестартирайте приложението:

```powershell
# Спрете приложението (Ctrl+C)
# След това стартирайте отново
mvn spring-boot:run
```

## Конфигурация

Следните настройки са добавени в `application.properties`:

```properties
server.port=8080
server.http2.enabled=false
server.ssl.enabled=false
```

И в `ServerConfig.java` за по-добра обработка на грешки.

## Проверка

След рестартиране, проверете че приложението работи:

1. Отворете браузър
2. Отидете на: `http://localhost:8080/` (НЕ HTTPS!)
3. Трябва да видите Dashboard страницата

## Ако проблемът продължава

1. Проверете дали има други приложения на порт 8080
2. Променете порта в `application.properties`:
   ```properties
   server.port=8081
   ```
3. След това използвайте: `http://localhost:8081/`

---

**Важно**: Винаги използвайте `http://` за localhost development!


