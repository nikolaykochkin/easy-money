kubectl exec -ti --namespace easy-money spark-worker-0 -- spark-submit \
  --master spark://spark-master-svc:7077 \
  --jars s3a://easy-money/spark/jars/* \
  --class ru.yandex.practicum.de.kk91.QRImageProcessor \
  --conf spark.hadoop.fs.s3a.endpoint=http://minio:9000 \
  --conf spark.hadoop.fs.s3a.endpoint.region=local \
  --conf spark.hadoop.fs.s3a.path.style.access=true \
  --conf spark.hadoop.fs.s3a.aws.credentials.provider=com.amazonaws.auth.EnvironmentVariableCredentialsProvider \
  --deploy-mode cluster \
  --verbose \
  s3a://easy-money/spark/spark-qr-image-processor.jar

#--packages org.apache.hadoop:hadoop-aws:3.3.4,com.amazonaws:aws-java-sdk-bundle:1.12.262,org.apache.spark:spark-sql-kafka-0-10_2.12:3.4.0,org.apache.spark:spark-tags_2.12:3.4.0,org.apache.spark:spark-token-provider-kafka-0-10_2.12:3.4.0 \
