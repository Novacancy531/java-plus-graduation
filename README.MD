![pic](pic.png)

# Explore With Me — Microservices Architecture

## Описание проекта

**Explore With Me** — серверное микросервисное приложение для публикации событий, подачи заявок на участие, комментирования и формирования персональных рекомендаций мероприятий.

Система предоставляет единый внешний REST API через gateway, при этом внутри состоит из набора независимых сервисов. Архитектура ориентирована на масштабируемость, отказоустойчивость и потоковую обработку пользовательских действий в реальном времени.

Рекомендательная подсистема построена на Apache Kafka, gRPC и анализе пользовательских взаимодействий с мероприятиями.

---

## Архитектура

Проект построен по микросервисной архитектуре с использованием **Spring Boot** и **Spring Cloud**.

В системе используются:

- централизованная конфигурация
- сервис обнаружения
- gateway
- потоковая обработка через Kafka
- gRPC для межсервисного взаимодействия
- рекомендательная платформа из отдельных сервисов

---

## Состав сервисов

### Инфраструктурные сервисы

- **config-server** — сервер централизованной конфигурации сервисов (Spring Cloud Config)
- **discovery-server** — сервер обнаружения сервисов и сервис-реестр (Eureka)
- **gateway-server** — API gateway, единая точка входа для внешних клиентов
- **Apache Kafka** — брокер сообщений и потоковая шина событий

---

### Бизнес-сервисы

- **event-service** — управление событиями, категориями и подборками
- **user-service** — управление пользователями
- **request-service** — заявки на участие в событиях
- **comment-service** — комментарии к событиям

---

## Внешний API

Внешний контракт системы описан в OpenAPI-спецификациях:

Основной API:  
[ewm-main-service-spec.json](ewm-main-service-spec.json)

---

### Платформа рекомендаций (stats-platform)

Подсистема рекомендаций реализована как набор отдельных микросервисов.

#### collector-service

Назначение — приём действий пользователей.

Функции:
- принимает действия пользователей по gRPC
- регистрируется в discovery-server
- получает конфигурацию из config-server
- запускает gRPC сервер на случайном порту
- преобразует сообщения в Avro
- публикует события в Kafka

Kafka топик:

stats.user-actions.v1

Типы действий:
- VIEW — просмотр события
- REGISTER — регистрация на событие
- LIKE — лайк события

---

#### aggregator-service

Назначение — расчёт сходства мероприятий.

Функции:
- читает поток действий пользователей из Kafka
- рассчитывает косинусное сходство мероприятий на основе весов действий
- выполняет инкрементальный пересчёт коэффициентов
- поддерживает частные суммы S_min(A,B), S_A, S_B
- упорядочивает пары событий по id для устранения дублей
- публикует коэффициенты сходства в Kafka

Kafka топик:

stats.events-similarity.v1

---

#### analyzer-service

Назначение — хранение метрик и генерация рекомендаций.

Функции:
- читает Kafka потоки действий и коэффициентов сходства
- обновляет базу данных:
    - историю взаимодействий пользователей
    - коэффициенты сходства событий
    - максимальные веса действий
- предоставляет gRPC API рекомендаций
- регистрируется в discovery-server
- получает конфигурацию из config-server
- запускает gRPC сервер на случайном порту

Поддерживаемые сценарии:
- рекомендации пользователю
- похожие мероприятия
- сумма взаимодействий по событиям

---

## Kafka топики

Создаются автоматически при запуске Docker Compose:

- stats.user-actions.v1
- stats.events-similarity.v1

Формат сообщений — **Avro**

Namespace схем:

ru.practicum.ewm.stats.avro

---

## gRPC API (внутреннее межсервисное взаимодействие)

### Collector — UserActionController

Метод:

CollectUserAction(UserActionProto) → Empty

Поля сообщения:
- user_id
- event_id
- action_type
- timestamp

---

### Analyzer — RecommendationsController

Методы:

GetRecommendationsForUser → stream RecommendedEventProto  
GetSimilarEvents → stream RecommendedEventProto  
GetInteractionsCount → stream RecommendedEventProto

---

## Взаимодействие сервисов

### REST (Feign)

- event-service → user-service
- event-service → request-service
- comment-service → user-service
- comment-service → event-service
- request-service → user-service
- request-service → event-service

---

### gRPC

- бизнес-сервисы → collector-service
- бизнес-сервисы → analyzer-service

---

### Kafka

- collector → Kafka
- aggregator → Kafka
- analyzer → база данных

---

## Надёжность

- Feign Client
- Resilience4j Circuit Breaker
- Spring Retry
- fallback-логика
- идемпотентная обработка Kafka сообщений
- инкрементальные пересчёты сходства

---

## Технологический стек

- Java 21
- Maven multi-module
- Spring Boot 3.x
- Spring Cloud
- PostgreSQL
- Apache Kafka
- gRPC
- Avro
- JPA / Hibernate
- QueryDSL
- MapStruct
- Lombok
- Resilience4j
- Spring Retry
- Zalando Logbook

---

## Тестирование

Для проверки основных бизнес-сервисов используется Postman-коллекция, а для рекомендательной платформы — автоматический tester.jar.

---

### Тесты основных сервисов (Main API)

**Postman Collection:**  
[ewm-main-service.json](tests/ewm-main-service.json)

Проверяются:

- пользователи (/admin/users)
- категории (/admin/categories)
- события (public / private / admin API)
- заявки на участие
- бизнес-ограничения (409 Conflict)
- валидация запросов (400 Bad Request)
- корректность статусов и формата ответов

---

### Тестирование рекомендательной платформы

Для проверки collector / aggregator / analyzer используется tester.jar (tests/tester.jar)
Он генерирует пользовательские действия, отправляет их в систему и проверяет корректность обработки.

---

### Пример запуска

Тестируем только **collector** и сохраняем отчёт в файл:

java -jar tester.jar --tester.execution.mode=COLLECTION --tester.execution.output.file-path=./report.txt

---

### Настройки генерации данных

Случайное количество уникальных пользователей:

tester.generation.user-count: 5 - 15

Случайное количество мероприятий:

tester.generation.event-count: 10 - 20

Верхний лимит общего числа действий:

tester.generation.actions-limit: 50 - 100

---

### Параметры временных меток

Смещение первой метки назад от текущего времени (Duration формат):

tester.generation.timestamp-settings.back: 3d

Минимальный интервал между метками:

tester.generation.timestamp-settings.increment-start: 1m

Максимальный интервал между метками:

tester.generation.timestamp-settings.increment-end: 10m

---

### Режимы выполнения

- COLLECTION – только collector
- AGGREGATION – collector + aggregator
- ANALYZE – collector + aggregator + analyzer

tester.execution.mode: ANALYZE

Мгновенное логирование в консоль:

tester.execution.immediate-logging.enabled: false

---

### Настройки вывода

Печатать info-сообщения:

tester.execution.output.info-enabled: true

Печатать TRACE-сообщения:

tester.execution.output.trace-enabled: true

Выводить отчёт в консоль:

tester.execution.output.print: true

Сохранять отчёт в файл:

tester.execution.output.file: true

Путь к файлу отчёта:

tester.execution.output.file-path: "./execution-report.txt"

---

## Конфигурация и запуск

- конфигурации сервисов — config-server
- обнаружение сервисов — discovery-server
- внешний доступ — gateway-server
- Kafka и топики — Docker Compose
- отдельная PostgreSQL база для каждого сервиса

---

## Итог

Система представляет собой микросервисную платформу событий с потоковой обработкой пользовательских действий и рекомендательным движком. Используются Kafka, gRPC и алгоритмы сходства мероприятий, что обеспечивает высокую производительность, масштабируемость и расширяемость платформы.
