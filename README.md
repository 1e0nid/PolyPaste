# PolyPaste

PolyPaste — сервис для создания и хранения текстовых заметок ("паст") по типу Pastebin, с поддержкой авторизации через VK, модерацией контента, полнотекстовым поиском и коротким URL для шаринга.

## Стек технологий

**Backend**
- Java 17+, Spring Boot 3.4
- Spring Security + JWT — аутентификация
- Spring Data JPA + PostgreSQL — хранение метаданных паст
- Redis — кэширование
- RabbitMQ — асинхронная обработка (модерация контента)
- MinIO (S3-совместимое хранилище) — хранение содержимого паст
- OpenSearch — полнотекстовый поиск по пастам
- ONNX Runtime (`rubert-tiny-toxic`) — модерация текста на токсичность
- VK OAuth2 — вход через ВКонтакте

**Frontend**
- Vue 3 + Vite
- Pinia — стейт-менеджмент
- Vue Router
- highlight.js — подсветка синтаксиса

**Инфраструктура**
- Docker Compose (Postgres, Redis, RabbitMQ, MinIO, OpenSearch)
- Prometheus + Grafana + cAdvisor — мониторинг

## Структура репозитория

```
.
├── backend/PolyPaste/    # Spring Boot backend
├── frontend/              # Vue frontend
├── docker-compose.yml     # Инфраструктура (Postgres, Redis, RabbitMQ, MinIO) + мониторинг
└── prometheus.yml         # Конфигурация Prometheus
```

## Запуск проекта

### 1. Поднять инфраструктуру

```bash
docker-compose up -d
```

Поднимет Postgres, Redis, RabbitMQ, MinIO, а также Prometheus/Grafana/cAdvisor для мониторинга.

### 2. Настроить конфигурацию backend

Скопируйте пример конфига и заполните своими значениями (см. `application.yml.example`, если он есть в проекте, либо создайте `backend/PolyPaste/src/main/resources/application.yml` по образцу ниже):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5439/Poly_Paste
    username: <ваш_логин>
    password: <ваш_пароль>
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: <ваш_логин>
    password: <ваш_пароль>

app:
  minio:
    endpoint: http://127.0.0.1:9000
    access-key: <ваш_access_key>
    secret-key: <ваш_secret_key>
    bucket: pastes

jwt:
  secret: <сгенерируйте случайную длинную строку>

vk:
  app-id: <ваш_vk_app_id>
  client-secret: <ваш_vk_client_secret>
  redirect-uri: http://localhost/auth/vk/callback
```

> ⚠️ Файл `application.yml` содержит секреты и **не должен попадать в git** — см. раздел "Безопасность" ниже.

### 3. Запустить backend

```bash
cd backend/PolyPaste
./mvnw spring-boot:run
```

### 4. Запустить frontend

```bash
cd frontend
npm install
npm run dev
```

## Мониторинг

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- cAdvisor: http://localhost:8080

## Безопасность

- Не коммитьте `application.yml`, `.env`, `*.p12`/`*.jks` — все они добавлены в `.gitignore`.
- Все пароли/ключи в примерах выше — плейсхолдеры, замените на свои перед запуском в проде.
