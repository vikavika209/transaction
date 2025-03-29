
# Spring Transaction Project

Этот проект представляет собой Spring Boot приложение, которое управляет транзакциями, лимитами на счетах и курсами валют. Приложение поддерживает работу с двумя основными валютами — **RUB** (рубли) и **KZT** (тенге), а также конвертирует суммы в USD для сравнений с лимитами. Приложение включает несколько ключевых компонентов для обработки данных транзакций и взаимодействия с внешними сервисами для получения актуальных курсов валют.

## Описание

Проект включает несколько ключевых сервисов и контроллеров для работы с транзакциями, лимитами и курсами валют:

- **TransactionService** — сервис для обработки транзакций. Он включает логику сохранения транзакций, проверки превышения лимитов и конвертации валют.
- **LimitService** — сервис для управления лимитами на счетах. Он создает, извлекает и обновляет лимиты по категориям.
- **ExchangeRateService** — сервис для работы с обменными курсами валют. Он поддерживает конвертацию валют и автоматическое обновление курсов.
- **ExchangeRateApiClient** — клиент для взаимодействия с внешним API для получения курсов валют.
- **ExchangeRateScheduler** — планировщик для регулярного обновления курсов валют.

### Контроллеры:

- **ClientController**:
    - **`/api/client/transactions/exceeded/{accountNumber}`**: Получает список транзакций, которые превышают лимит по счету.
    - **`/api/client/limits`**: Создает новый лимит для счета.
    - **`/api/client/limits/{accountNumber}`**: Получает все лимиты, установленные для данного номера счета.

- **InternalController**:
    - **`/transactions`**: Сохраняет транзакцию на основе переданных данных и обрабатывает логику для различных валют.

### Основные классы и их функции:

- **TransactionService**:
    - Сохраняет транзакции и проверяет, превышает ли сумма транзакции установленный лимит.
    - Поддерживает асинхронную обработку транзакций для разных валют (RUB, KZT).
    - Конвертирует суммы транзакций в USD для сравнения с лимитом.
    - Ведет логирование всех ключевых действий.

- **LimitService**:
    - Создает и управляет лимитами для аккаунтов по категориям (например, продуктовые и сервисные лимиты).
    - Выполняет конвертацию лимитов в USD, если валюта отличается от USD.
    - Обеспечивает возможность работы с лимитами по категориям для каждого аккаунта.

- **ExchangeRateService**:
    - Обновляет курсы валют через внешний API.
    - Предоставляет методы для конвертации сумм в различные валюты с учетом актуальных курсов.
    - Использует сохраненные данные о курсах для конвертации и расчета лимитов.

- **ExchangeRateApiClient**:
    - Получает актуальные курсы валют через внешний API.
    - Выполняет запросы к API для получения курса валют и конвертации.
    - Логирует все запросы и ошибки при взаимодействии с внешним сервисом.

- **ExchangeRateScheduler**:
    - Периодически обновляет курсы валют с помощью сервиса `ExchangeRateService`.
    - Запускается каждый день в 18:00 для обновления курсов.

## Структура проекта

- **com.example.transaction.controller** — контроллеры для обработки запросов, связанные с транзакциями и лимитами.
- **com.example.transaction.service** — сервисы для логики обработки транзакций и лимитов.
- **com.example.transaction.model** — модели для представления данных транзакций и лимитов.
- **com.example.transaction.client** — клиент для взаимодействия с внешним API обменных курсов.
- **com.example.transaction.scheduler** — планировщики для обновления данных о курсах валют.

## Технологии

- **Spring Boot** (Java 17)
- **PostgreSQL**
- **Docker**
- **Docker Compose**
- **Flyway**
- **Open Exchange Rates API**
- **Spring Data JPA**
- **Spring Validation**
- **ModelMapper**
- **SpringDoc OpenAPI**
- **JUnit 5**
- **Mockito**
- **TestContainers**
- **Jacoco**

## Установка и запуск

### 1. Настройка локальной среды

Перед тем как запустить проект, убедитесь, что у вас установлены следующие инструменты:

- **Java 17** (или выше)
- **Maven** (если используете Maven)
- **Docker** и **Docker Compose**
- **Git**

### 2. Клонируйте репозиторий

Клонируйте репозиторий на свою локальную машину:

```bash
git clone https://github.com/vikavika209/transaction.git
cd your-repository
```

### 3. Конфигурация базы данных

В файле `src/main/resources/application.yml` настроены параметры для подключения к базе данных PostgreSQL:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/exchange_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  flyway:
    enabled: false
    baseline-on-migrate: true
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: update
      show-sql: true
      properties:
        hibernate:
          format_sql: true
          dialect: org.hibernate.dialect.PostgreSQLDialect
  exchange:
    rate:
      api:
        url: "https://openexchangerates.org/api/latest.json?app_id=${EXCHANGE_API_KEY}"
  logging:
    level:
      org.springframework: DEBUG
```

Замените `EXCHANGE_API_KEY` на свой реальный API-ключ от [Open Exchange Rates](https://openexchangerates.org/).

### 4. Создание и запуск через Docker

Для удобства приложение и база данных упакованы в Docker-контейнеры. Чтобы запустить их, выполните следующие шаги:

1. Сначала создайте Docker-образ:

```bash
docker build -t spring-app .
```

2. Запустите контейнеры:

```bash
docker-compose up -d
```

3. Приложение будет доступно по адресу: [http://localhost:8080](http://localhost:8080).

### 5. Проверка контейнеров

Чтобы убедиться, что контейнеры работают, выполните:

```bash
docker-compose ps
```

### 6. Остановка контейнеров

Для остановки и удаления контейнеров выполните:

```bash
docker-compose down
```

## Развертывание с использованием GitHub Actions

Проект настроен на автоматическое развертывание с использованием [GitHub Actions](https://github.com/features/actions). После того как код будет запушен в ветку `master`, GitHub Actions автоматически:

1. Соберет Docker-образы.
2. Запустит контейнеры с вашим приложением и базой данных.
3. Проверит, что контейнеры работают.

### Структура файла `deploy.yml`:

```yaml
name: Build and Deploy Transaction

on:
  push:
    branches:
      - master

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    env:
      EXCHANGE_API_KEY: ${{ secrets.EXCHANGE_API_KEY }}
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2
      - name: Build Docker images
        run: |
          docker-compose build
      - name: Run Docker containers
        run: |
          docker-compose up -d
        env:
          EXCHANGE_API_KEY: ${{ secrets.EXCHANGE_API_KEY }}
```

Пример использования секретов в GitHub Actions:

1. Перейдите в настройки репозитория на GitHub.
2. Откройте раздел "Secrets".
3. Добавьте секрет `EXCHANGE_API_KEY` с вашим API-ключом.

## Логирование

Логи работы приложения можно просмотреть через стандартный вывод
