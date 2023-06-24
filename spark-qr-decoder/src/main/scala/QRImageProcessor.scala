package ru.yandex.practicum.de.kk91

import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_wechat_qrcode.WeChatQRCode


object QRImageProcessor {

  private val detector = new WeChatQRCode()

  private case class Command(id: Long, uuid: String, attachments: List[CommandAttachment])

  private case class CommandAttachment(id: Long, storageId: String)

  def main(args: Array[String]): Unit = {

    val kafkaServer = sys.env.get("KAFKA_SERVERS") match {
      case Some(value) => value
      case None => throw new IllegalStateException("Environment variable KAFKA_SERVERS must be set.")
    }

    val inputTopic = sys.env.getOrElse("INPUT_TOPIC", "spark-photo")
    val outputTopic = sys.env.getOrElse("OUTPUT_TOPIC", "spark-photo-decoded")

    val s3Bucket = sys.env.getOrElse("AWS_S3_BUCKET", "easy-money")
    val s3path = s"s3a://$s3Bucket/"

    val spark = SparkSession.builder
      .appName("QR Image Processor")
      .config("spark.sql.streaming.kafka.useDeprecatedOffsetFetching", value = true)
      .getOrCreate()

    spark.udf.register("decode", udf(detectAndDecode _).asNondeterministic())

    import spark.implicits._

    val commandSchema = ScalaReflection.schemaFor[Command].dataType.asInstanceOf[StructType]

    val commandStream = imageCommandStream(spark, kafkaServer, inputTopic)

    val query = commandStream
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
      .select($"key", from_json($"value", commandSchema).as("command"))
      .writeStream
      .foreachBatch(
        (batchDF: DataFrame, batchId: Long) =>
          if (!batchDF.isEmpty) {
            batchDF.persist()

            val attachments = explodeCommandAttachments(batchDF.as[(String, Command)], s3path)
            val urls = attachments.map(_.getAs[String]("origin")).collect()
            val decoded = readAndDecodeImages(spark, urls)

            writeResults(attachments, decoded, kafkaServer, outputTopic)

            batchDF.unpersist()
          }: Unit
      )
      .start()

    try query.awaitTermination()
    finally spark.stop()
  }

  private def writeResults(attachments: DataFrame, decoded: DataFrame, kafkaServer: String, outputTopic: String): Unit = {
    attachments.join(decoded, "origin", joinType = "leftOuter")
      .selectExpr("key", "map(id, decoded) as attachment")
      .groupBy("key").agg(collect_list("attachment").as("attachments"))
      .selectExpr("key", "to_json(named_struct('uuid', key, 'attachments', attachments)) AS value")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaServer)
      .option("topic", outputTopic)
      .save()
  }

  private def readAndDecodeImages(spark: SparkSession, urls: Array[String]): DataFrame = {
    spark.read.format("image")
      .load(urls: _*)
      .select("image.*")
      .selectExpr("*", "decode(height, width, mode, data) as decoded")
      .drop("data", "height", "width", "mode", "nChannels")
      .cache()
  }

  private def explodeCommandAttachments(batchDS: Dataset[(String, Command)], s3path: String): DataFrame = {
    batchDS
      .selectExpr("key", "explode(command.attachments) AS attachment")
      .selectExpr("key", "attachment.id", "attachment.storageId")
      .withColumn("origin", concat(lit(s3path), col("storageId")))
      .cache()
  }

  private def imageCommandStream(spark: SparkSession, kafkaServer: String, inputTopic: String): DataFrame = {
    spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", kafkaServer)
      .option("subscribe", inputTopic)
      .option("startingOffsets", "latest")
      .load()
  }

  private def detectAndDecode(height: Int, width: Int, mode: Int, data: Array[Byte]): List[String] = {
    try {

      val mat = new Mat(height, width, mode, new BytePointer(data: _*))
      val result = detector.detectAndDecode(mat)
      if (result.empty()) {
        List.empty
      } else {
        result.get().map(_.getString).toList
      }
    } catch {
      case e: Throwable => List(e.getMessage)
    }
  }
}