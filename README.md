# Alert System - Инструкция по сборке и запуску


## Структура проекта

```
alert-system/
├── common/                  <- общая библиотека (DTO, Kafka-события)
├── api-gateway/             <- единая точка входа, JWT-валидация
├── config-manager/          <- аутентификация, управление конфигами   
├── metrics-collector/       <- scraping метрик
├── tsdb-storage/            <- прокси над InfluxDB
├── analytics-alerting/      <- движок правил алертинга
├── online-notifier/         <- отправка уведомлений
└── visual-reports/          <- API дашбордов, генерация PDF
```

## Порты сервисов

| Сервис             | Порт |
|:-------------------|:-----|
| API Gateway        | 8080 |
| Config Manager     | 8081 |
| Metrics Collector  | 8082 |
| TSDB Storage       | 8083 |
| Analytics Alerting | 8084 |
| Visual Reports     | 8085 |
| Online Notifier    | 8086 |
| InfluxDB           | 8087 |
| KafkaUI            | 8088 |
| PostgreSQL         | 5432 |
| Kafka              | 9093 |
| Redis              | 6379 |

---

## Подключение Kafka топиков

| Топик            | Кто пишет           | Кто читает                            |
|:-----------------|:--------------------|:--------------------------------------|
| `metrics-raw`    | metrics-collector   | tsdb-storage, analytics-alerting      |
| `alerts-topic`   | analytics-alerting  | online-notifier, visual-reports       |
| `config-updated` | config-manager      | metrics-collector, analytics-alerting |

---

## Схема баз данных

| БД             | Сервис               |
|:---------------|:---------------------|
| `config_db`    | config-manager       |
| `alerting_db`  | analytics-alerting   |
| `notifier_db`  | online-notifier      |
| `reports_db`   | visual-reports       |

---

## Схема взаимодействия

```
                        +------------------+
  Внешние агенты -----> |  API Gateway     |  :8080
  (Prometheus, SNMP)    |  (Spring Cloud   |
                        |   Gateway)       |
                        +--------+---------+
                                 |
            +--------------------+--------------------+
            |                    |                    |
     +------v------+    +--------v-------+   +--------v-------+
     | Config      |    | Metrics        |   | Visual         |
     | Manager     |    | Collector      |   | Reports        |
     | :8081       |    | :8082          |   | :8085          |
     +------+------+    +--------+-------+   +--------+-------+
            |                    |                    |
            | REST/Feign         | Kafka              | REST
            |            +-------v--------+           |
            +----------->|     Kafka      |<----------+
                         | (metrics-raw,  |
                         |  alerts-topic) |
                         +-------+--------+
                                 |
                    +------------+------------+
                    |                         |
             +------v------+         +--------v-------+
             | Analytics   |         | TSDB Storage   |
             | Alerting    |         | :8083          |
             | :8084       |         | (InfluxDB       |
             +------+------+         |  proxy/writer) |
                    |                +--------+-------+
                    | Kafka (alerts)          |
                    |                  InfluxDB :8086
             +------v------+
             | Online      |
             | Notifier    |
             | :8086       |
             +------+------+
                    |
          +---------+---------+
          |                   |
     Telegram API        SMTP Server
```

---

## Сервисы

### 1. api-gateway (порт 8080)

**Назначение:**
- Единая точка входа для всех внешних запросов
- Валидация JWT-токенов
- Маршрутизация к нужному микросервису по prefix пути
- Rate limiting

---

### 2. config-manager (порт 8081)

**Назначение:**
- Аутентификация и авторизация пользователей (JWT)
- CRUD для всех конфигураций системы
- Уведомление других сервисов об изменениях через Kafka (`config-updated` топик)

---

### 3. metrics-collector (порт 8082)

**Назначение:**
- Активный сбор метрик (scraping) с источников по HTTP (формат Prometheus)
- Прием push-метрик от агентов через REST
- Нормализация и обогащение метрик метаданными
- Запись в Kafka (`metrics-raw` топик) и напрямую в TSDB через REST

**Конфигурация:** читает data_sources из ConfigManager при старте и обновляет по Kafka `config-updated`

**Логика scraping:**
- Планировщик (ScheduledExecutorService) создает задачи на scrape для каждого data_source
- Каждый scrape: HTTP GET к endpoint -> парсинг Prometheus text format -> нормализация -> Kafka

---

### 4. tsdb-storage (порт 8083)

**Назначение:**
- Прокси-сервис между остальными микросервисами и InfluxDB
- Прием метрик из Kafka (`metrics-raw`) и запись в InfluxDB
- REST API для запросов к данным (используется AnalyticsAlerting и VisualReports)

**Kafka (consumer):** `metrics-raw` - читает пакетами, записывает в InfluxDB batch write


**InfluxDB организация:**
- Bucket: `metrics` (retention: 90 дней)
- Measurement = имя метрики (cpu_usage_percent, memory_used_bytes, ...)
- Tags: source_id, source_name + все labels из метрики
- Field: value (double)

---

### 5. analytics-alerting (порт 8084)

**Назначение:**
- Периодическая проверка правил алертинга (polling TSDB)
- Обнаружение аномалий: порог, отсутствие данных, скорость роста
- Генерация алертов и публикация в Kafka (`alerts-topic`)
- Дедупликация: не генерирует повторный алерт, пока предыдущий не разрешен

**Конфигурация:** читает alert_rules из ConfigManager, обновляет по Kafka `config-updated`

**Логика проверки:**
```
Каждые N секунд (из правила duration_sec):
  1. Запрос к tsdb-storage: последние значения метрики за window
  2. Проверка условия: value > threshold | value < threshold | no_data
  3. Простой тренд: если avg(последние 5 точек) растет > 10%/мин -> pre-alert
  4. Если условие выполнено -> создать Alert, отправить в Kafka
  5. Если условие перестало выполняться -> отправить RESOLVED алерт
```

**Kafka:**
- Consumer: `metrics-raw` (для real-time анализа потока)
- Consumer: `config-updated`
- Producer: `alerts-topic`

---

### 6. online-notifier (порт 8086)

**Назначение:**
- Прием алертов из Kafka (`alerts-topic`)
- Дедупликация (не слать одно и то же чаще раза в 10 минут)
- Формирование сообщений под каждый канал
- Отправка через Telegram Bot API или SMTP

**Kafka (consumer):** `alerts-topic`

**Логика:**
```
1. Получить алерт из Kafka
2. Проверить дедупликацию (Redis или in-memory cache с TTL)
3. Получить конфиг канала из ConfigManager (Feign)
4. Сформировать сообщение (шаблон Thymeleaf/plain text)
5. Отправить:
   - TELEGRAM: POST https://api.telegram.org/bot{token}/sendMessage
   - EMAIL: JavaMailSender -> SMTP
6. Записать в лог отправки
```

---

### 7. visual-reports (порт 8085)

**Назначение:**
- REST API для дашбордов (данные для фронтенда)
- Генерация PDF/HTML отчетов по расписанию
- Хранение сгенерированных файлов отчетов


**Генерация отчетов (Quartz):**
```
По cron из report_schedules:
  1. Запрос к tsdb-storage: агрегированные данные за период
  2. Запрос к analytics-alerting: алерты за период
  3. Расчет SLA (availability = uptime / total_time * 100)
  4. Генерация PDF (iText) или HTML (Thymeleaf)
  5. Сохранение файла на диск/S3
  6. Запись в generated_reports через config-manager API
```

---


## Безопасность

- JWT (RS256) - config-manager выдает токены, api-gateway проверяет публичным ключом
- Роли: `ADMIN` > `LEAD_ENGINEER` > `ENGINEER`
- Каждый сервис валидирует заголовок `X-User-Id` и `X-User-Role` (проставляет gateway после JWT decode)

---

## Топики Kafka

| Топик            | Producer              | Consumer(s)                        |
|------------------|-----------------------|------------------------------------|
| `metrics-raw`    | metrics-collector     | tsdb-storage, analytics-alerting   |
| `alerts-topic`   | analytics-alerting    | online-notifier, visual-reports    |
| `config-updated` | config-manager        | metrics-collector, analytics-alerting, online-notifier, visual-reports |

---
