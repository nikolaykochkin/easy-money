FROM eclipse-temurin:17-jdk as build

WORKDIR /workspace/app

COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN --mount=type=cache,target=/root/.gradle ./gradlew clean build
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*-SNAPSHOT.jar)

FROM eclipse-temurin:17-jre

ARG DEPENDENCY=/workspace/app/build/dependency

ARG SERVER_PORT
ARG AWS_S3_ENDPOINT
ARG AWS_REGION
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
ARG DB_URL
ARG DB_USER
ARG DB_PASS
ARG KAFKA_BOOTSTRAP
ARG BOT_TOKEN
ARG BOT_NAME
ARG DOWNLOADS_PATH

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","ru.yandex.practicum.de.kk91.easymoney.EasyMoneyApplication"]