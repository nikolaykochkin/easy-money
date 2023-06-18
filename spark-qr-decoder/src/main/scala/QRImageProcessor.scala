package ru.yandex.practicum.de.kk91

import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object QRImageProcessor {
  private case class Command(id: Long, uuid: String, attachments: Seq[CommandAttachment])

  private case class CommandAttachment(id: Long, storageId: String)

  def main(args: Array[String]): Unit = {
    val master = args(0)

    val kafkaServer = args(1)
    val inputTopic = args(2)
    val outputTopic = args(3)

    val s3Bucket = args(4)
    val s3path = s"s3a://$s3Bucket/"

    val spark = SparkSession.builder
      .appName("QR Image Processor")
      .master(master)
      .getOrCreate()

    spark.udf.register("decode", udf(QRCodeDetector.detectAndDecode _).asNondeterministic())

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

}