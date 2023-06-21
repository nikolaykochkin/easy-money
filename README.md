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

### Build backend
```shell
docker build --tag $(minikube ip):5000/backend-application:latest .
docker push $(minikube ip):5000/backend-application:latest
```

### spark submit

```shell
spark-submit \
      --class ru.yandex.practicum.de.kk91.QRImageProcessor \
      --master k8s://https://192.168.49.2:8443 \
      --deploy-mode cluster \
      --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
      --conf spark.kubernetes.file.upload.path=local:///opt/spark/jars/spark-qr-image-processor.jar \
      --conf spark.kubernetes.namespace=easy-money \
      --conf spark.kubernetes.container.image=spark:latest \
      --conf spark.hadoop.fs.s3a.access.key= \
      --conf spark.hadoop.fs.s3a.secret.key= \
      --conf spark.hadoop.fs.s3a.endpoint="http://minio-service.easy-money:9000" \
      --conf spark.hadoop.fs.s3a.endpoint.region=local \
      --conf spark.hadoop.fs.s3a.path.style.access=true \
      --verbose \
      local:///opt/spark/jars/spark-qr-image-processor.jar \
      k8s://https://10.96.0.1:443 \
      "kafka-service.easy-money:9092" \
      spark-photo \
      spark-photo-decoded \
      easy-money
```

```shell
docker-image-tool.sh -m -t latest build
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

## Общая архитектура

![architecture.png](architecture.png)