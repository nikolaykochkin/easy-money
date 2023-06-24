# Простой учет финансов

Сервис для учета личных финансов с интерфейсом через чат-бота Telegram.

## TL;DR

### Start:

```shell
docker compose -f Docker-compose.yaml -p easy-money up -d --build
```

### Stop:

```shell
docker compose -f Docker-compose.yaml down
```

## Принцип работы

### Входные данные

Пользователь может вносить траты или поступления несколькими способами:

- Текстовым сообщением в чате с ботом,
- Голосовым сообщением боту,
- Фотографией чека.

### Процесс обработки
1. Бот получает сообщения от пользователя и отправляет их в стрим сообщений.
2. Дальше система обработки сообщений определяет тип сообщения: голос, изображение, текст и отправляет его в целевой
   конвейер процессинга.
3. Система процессинга состоит из модулей:
    - Модуль обработки голосовых сообщений преобразовывает речь в текст.
    - Модуль обработки естественного языка преобразует текстовые команды в очищенные данные для записи в базу.
    - Модуль обработки изображений распознает QR коды чеков и передает их в парсер для последующей обработки.
4. Обработанные данные записываются в базу.
5. Пользователю отправляется подтверждение с деталями операции.

### Выходные данные

Выходными данными служат дашборды с информацией по расходам, а также отчеты может формировать бот и отправлять их
пользователю в чат.

## Технологический стек

### Программное обеспечение

- База данных: PostgreSQL;
- Брокер: Kafka;
- Файловое хранилище: S3 (Minio - dev, Object Storage - prod);
- Деплой: K8S + Helm
- Инфраструктура: Minikube или Docker Compose - dev, Yandex Cloud - prod

### Бэкенд - stateless монолит на Java 17 и Spring Boot 3.1.0

- API: Spring Web + Spring Actuator
- ORM: Spring Data JPA
- Bot: telegrambots
- AWS: spring-cloud-aws-starter-s3
- Kafka: spring-kafka (простой темплейт для продюсера и аннотация подписчика)
- Project Lombok - уменьшить бойлерплейт код
- Билд: gradle, Dockerfile, Helm Chart

### Spark:

- Работа с Кафкой: Spark SQL, Spark Structured Streaming
- Загрузка файлов: Hadoop AWS
- Детектор QR:
  - SparkML (загрузка изображений в opencv формате)
  - Доступ к нативному коду OpenCV: JavaCPP + JavaCV
  - Детектор: OpenCV  WeChat QRCode
- NLP: SparkNLP

## Общая схема системы

```mermaid
flowchart 
    U((User))
    Tg(((Telegram)))
    U <--> Tg
    Tg <--> Bot
    U <--> D
    subgraph Cloud
        
        D(((dashboard)))
        DB[(Postgres)]
        D <--> DB
        subgraph Backend Kubernetes
            K>Kafka]
            subgraph Application
                Bot
                MG[Message Gateway]
                MP[Message Processors]
                CP[Command Processor]
                TP[Transaction Service]
                P[Parser]
                L[Loader]
                R[Repository]
                Bot --> MP
                MP --> CP
                MP --> R
                CP <--> L
                CP --> R
                CP <---> MG
                CP --> TP
                TP --> R
                CP <--> P
            end
            subgraph Spark
                direction LR
                SN[[Spark NLP]]
                QR[[QR Image Processor]]
            end
            MG <--> K
            K <--> Spark
        end
        L --> S3
        S3 --> Spark
        S3[\ File Storage/]
        R <--> DB
        
    end
    P <--> Web((( HTTP\nmapr.tax.gov.me )))
```

## Схема данных

```mermaid
erDiagram
    account {
        bigserial id PK
        bigint owner_id FK
        varchar account_group
        varchar name
        smallint account_type
        varchar currency
        timestamp created_date
        timestamp last_modified_date
    }
    category {
        bigserial id PK
        varchar category_group
        varchar name
        timestamp created_date
        timestamp last_modified_date
    }
    command {
        bigserial id PK
        uuid uuid UK
        bigint telegram_message_id FK
        smallint source
        smallint state
        smallint type
        text content
        text error
        text sql
        bigint user_id
        timestamp created_date
        timestamp last_modified_date
    }
    command_attachment {
        bigserial id PK
        uuid uuid
        bigint command_id FK
        varchar url
        varchar telegram_id
        varchar storage_id
        varchar filename
        bigint filesize
        varchar mime_type
    }
    counterparty {
        bigserial id PK
        varchar counterparty_group
        varchar name
        varchar external_id
        timestamp created_date
        timestamp last_modified_date
    }
    invoice {
        bigserial id PK
        uuid uuid UK
        bigint seller_id FK
        bigint user_id FK
        bigint account_id FK
        timestamp date_time
        varchar external_id
        varchar url
        text content
        varchar currency
        numeric sum
        timestamp created_date
        timestamp last_modified_date
    }
    invoice_item {
        bigserial id PK
        bigint invoice_id FK
        bigint item_id FK
        real quantity
        numeric unit_price
        numeric price
    }
    item {
        bigserial id PK
        bigint category_id FK
        varchar external_id UK
        varchar item_group
        varchar name
        timestamp created_date
        timestamp last_modified_date
    }
    telegram_message {
        bigserial id PK
        uuid uuid UK
        bigint user_id FK
        timestamp message_date
        integer update_id
        integer message_id
        bigint chat_id
        bigint tg_user_id
        varchar voice_file_path
        varchar photo_file_path
        text text
        jsonb update
        jsonb file_metadata
        timestamp created_date
        timestamp last_modified_date
    }
    transaction {
        bigserial id PK
        bigint account_id FK
        bigint category_id FK
        bigint counterparty_id FK
        bigint invoice_id FK
        bigint user_id FK
        uuid uuid
        timestamp timestamp
        boolean draft
        smallint transaction_type
        varchar currency
        numeric sum
        varchar comment
        timestamp created_date
        timestamp last_modified_date
    }
    users {
        bigserial id PK
        varchar login UK
        varchar name
        varchar password
        bigint telegram_id
        timestamp created_date
        timestamp last_modified_date
    }

    telegram_message }o--|| users: user_id
    telegram_message ||--o{ command: telegram_message_id
    command ||--o{ command_attachment: command_id
    command }o--|| users: user_id
    users ||--o{ account: owner_id
    users ||--o{ invoice: user_id
    account ||--o{ transaction: account_id
    account ||--o{ invoice: account_id
    invoice }o--|| counterparty: seller_id
    invoice ||--|{ invoice_item: invoice_id
    invoice_item }|--|| item: item_id
    users ||--o{ transaction: user_id
    transaction }o--|| category: category_id
    category ||--o{ item: category_id
    transaction }o--|| counterparty: counterparty_id
    transaction }o--|| invoice: invoice_id
```

## Обработка фотографии чека с QR кодом

### Фаза 1 Сохранение Телеграм сообщения (STG)

```mermaid
sequenceDiagram
    actor User
    participant Telegram
    participant Bot
    participant Gateway
    participant 'Message\nProcessor'
    participant Kafka
    participant DB
```

### Фаза 2. Сохранение фото в S3

```mermaid
sequenceDiagram
    participant CP
    participant Loader
    participant S3
    participant Kafka
    participant Spark
    participant DB
```