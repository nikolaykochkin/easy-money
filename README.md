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

../bin/spark-submit \
--class ru.yandex.practicum.de.kk91.QRImageProcessor \
--master spark://spark-master:7077 \
--deploy-mode cluster \
--conf spark.hadoop.fs.s3a.access.key=DxGGce0n2EnFytRyAojY \
--conf spark.hadoop.fs.s3a.secret.key=KvHbcbgf9PdzEcRUSrbh59WbwRyro80SXrUAYmmd \
--conf spark.hadoop.fs.s3a.endpoint=http://minio:9000 \
--conf spark.hadoop.fs.s3a.endpoint.region=local \
--conf spark.hadoop.fs.s3a.path.style.access=true \
spark-qr-image-processor.jar \
spark://spark-master:7077 \
kafka:9092 \
spark-photo \
spark-photo-decoded \
easy-money

http://minio:9000/spar-jobs-release/spark-qr-image-processor.jar?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=admin%2F20230619%2Flocal%2Fs3%2Faws4_request&X-Amz-Date=20230619T110230Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&versionId=null&X-Amz-Signature=5d57c25244bfd847d960ed48eb026bfb3cc9619e98b7e8f2f14504f626eac7c1

AWS_ACCESS_KEY_ID=DxGGce0n2EnFytRyAojY;AWS_REGION=local;AWS_S3_ENDPOINT=http://127.0.0.1:9000;AWS_SECRET_ACCESS_KEY=KvHbcbgf9PdzEcRUSrbh59WbwRyro80SXrUAYmmd;BOT_TOKEN=6109561936:AAE5qvqN8EGel_Fv8nlR6BEHiPDCY8mGuNU

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

## Общая архитектура

![architecture.png](architecture.png)