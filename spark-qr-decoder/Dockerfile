FROM sbtscala/scala-sbt:eclipse-temurin-jammy-11.0.17_8_1.9.0_2.12.18 as build

WORKDIR /workspace/app

# To avoid OOM Exception
ENV SBT_OPTS="-Xms1024M -Xmx4G -Xss2M -XX:MaxMetaspaceSize=2G -XX:+UseG1GC"

COPY . .

RUN sbt clean assembly

FROM apache/spark:3.4.0

ARG SPARK_HOME
ENV WORK_DIR=${SPARK_HOME}/work-dir

ARG MASTER
ARG KAFKA_BOOTSTRAP
ARG PHOTO_INPUT_TOPIC
ARG PHOTO_OUTPUT_TOPIC

ARG AWS_S3_BUCKET
ARG AWS_S3_ENDPOINT
ARG AWS_REGION
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY

WORKDIR $WORK_DIR
VOLUME $WORK_DIR

COPY --from=build /workspace/app/target/scala-2.12/spark-qr-image-processor.jar .

#DEBUG
#COPY target/scala-2.12/spark-qr-image-processor.jar .

ENTRYPOINT ../bin/spark-submit \
      --class ru.yandex.practicum.de.kk91.QRImageProcessor \
      --master $MASTER \
      --deploy-mode cluster \
      --conf spark.hadoop.fs.s3a.access.key=$AWS_ACCESS_KEY_ID \
      --conf spark.hadoop.fs.s3a.secret.key=$AWS_SECRET_ACCESS_KEY \
      --conf spark.hadoop.fs.s3a.endpoint=$AWS_S3_ENDPOINT \
      --conf spark.hadoop.fs.s3a.endpoint.region=$AWS_REGION \
      --conf spark.hadoop.fs.s3a.path.style.access=true \
      --verbose \
      file://$WORK_DIR/spark-qr-image-processor.jar \
      $MASTER \
      $KAFKA_BOOTSTRAP \
      $PHOTO_INPUT_TOPIC \
      $PHOTO_OUTPUT_TOPIC \
      $AWS_S3_BUCKET
