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

```plantuml
@startuml
!pragma layout smetana
left to right direction

actor "User" as user
cloud "Telegram" as telegram
component "Bot" as bot
queue "Received\nMessages" as receive
queue "Response\nMessages" as response
stack "S3" as s3

queue "Voice" as voice
queue "Text" as text
queue "Image" as image
queue "Load" as load
component "Spark Cluster" as cluster {
    component "Image\nProcessor" as ip
    component "Natural\nLanguage\nProcessor" as nlp
    component "Speech\nRecognition" as sr
}
queue "Handled data" as handled
component "Application" as app {
    component "Message\nHandler" as mh
    component "Loader" as loader
    component "Repository" as repo
    component "Command\nHandler" as ch
    mh --> repo: "Save\nraw\nmessage"
    ch --> repo: "Save\nhandled\ndata"
}
database "DB" as db
agent "Dashboard" as dashboard

user --> telegram: "Voices\nPhotos\nText"
telegram --> bot
bot --> telegram
telegram --> loader: "Download Files"
bot --> receive
receive --> mh
loader --> s3: "Upload Files"
loader --> voice
loader --> image
mh --> text
mh --> load
load --> loader
image --> ip
voice --> sr
text --> nlp
s3 --> sr: "Download\nVoices"
s3 --> ip: "Download\nImages"
sr --> text

nlp --> handled
ip --> handled

handled -l-> ch
repo --> db
db --> dashboard
ch -u-> response: "Send reply"
response --> bot

@enduml 
```

