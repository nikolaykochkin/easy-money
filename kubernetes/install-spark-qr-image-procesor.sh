kubectl exec -ti --namespace easy-money spark-worker-0 -- spark-submit \
  --master spark://spark-master-0.spark-headless.easy-money.svc.cluster.local:7077 \
  --packages org.apache.hadoop:hadoop-aws:3.3.4,com.amazonaws:aws-java-sdk-bundle:1.12.262 \
  --class ru.yandex.practicum.de.kk91.QRImageProcessor \
  --conf spark.hadoop.fs.s3a.endpoint=https://storage.yandexcloud.net \
  --conf spark.hadoop.fs.s3a.endpoint.region=ru-central1 \
  --conf spark.hadoop.fs.s3a.path.style.access=true \
  --driver-memory 2g --executor-memory 2g \
  --num-executors 1 --executor-cores 2 \
  --conf spark.dynamicAllocation.enabled=false \
  --deploy-mode cluster \
  --verbose \
  s3a://easy-money/spark/spark-qr-image-processor.jar
