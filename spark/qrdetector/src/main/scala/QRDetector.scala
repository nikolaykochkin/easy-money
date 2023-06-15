import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.opencv_java
import org.opencv.core.Mat
import org.opencv.wechat_qrcode.WeChatQRCode

import scala.jdk.CollectionConverters._

object QRDetector {

  Loader.load(classOf[opencv_java])
  private val detector = new WeChatQRCode()

  private def decode(height: Int, width: Int, mode: Int, data: Array[Byte]): Seq[String] = {
    try {
      val mat = new Mat(height, width, mode)
      mat.put(0, 0, data)
      val result = detector.detectAndDecode(mat)
      result.asScala
    } catch {
      case e: Throwable => Seq(e.getMessage)
    }
  }

  private case class Command(id: Long, uuid: String, attachments: Seq[CommandAttachment])

  private case class CommandAttachment(id: Long, storageId: String)

  // TODO: extract properties 
  def main(args: Array[String]): Unit = {
    val path = "s3a://easy-money/"

    val spark = SparkSession.builder
      .appName("QRDetector")
      .master("local")
      .getOrCreate()

    spark.udf.register("decode", udf(decode _).asNondeterministic())

    spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", "8smG21LcbEPMdjeJUXHQ")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", "01HQerzObCNVEPhJCxLYMkDnvdRkcaBtWV5TCDJm")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "http://127.0.0.1:9000")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint.region", "ru-central1")

    import spark.implicits._

    val commandSchema = ScalaReflection.schemaFor[Command].dataType.asInstanceOf[StructType]

    spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "spark-photo")
      .option("startingOffsets", "earliest")
      .load()
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
      .select($"key", from_json($"value", commandSchema).as("command"))
      .writeStream
      .foreachBatch(
        (batchDF: DataFrame, batchId: Long) =>
          if (!batchDF.isEmpty) {
            batchDF.persist()

            val df = batchDF
              .as[(String, Command)]
              .selectExpr("key", "explode(command.attachments) AS attachment")
              .select($"key", $"attachment.id", $"attachment.storageId", concat(lit(path), $"storageId").as("origin"))
              .cache()

            val urls = df.map(_.getAs[String]("origin")).collect()

            val decoded = spark.read.format("image")
              .load(urls: _*)
              .select("image.*")
              .select($"*", expr("decode(height, width, mode, data)").as("decoded"))
              .drop($"data")
              .drop($"height", $"width", $"mode", $"nChannels")
              .cache()

            df.join(decoded, "origin", joinType = "leftOuter")
              .select($"key", map($"id", $"decoded").as("attachment"))
              .groupBy($"key").agg(collect_list($"attachment").as("attachments"))
              .selectExpr("key", "to_json(named_struct('uuid', key, 'attachments', attachments)) AS value")
              .write
              .format("kafka")
              .option("kafka.bootstrap.servers", "localhost:9092")
              .option("topic", "spark-photo-decoded")
              .save()

            batchDF.unpersist()
          }: Unit
      )
      .start()
      .awaitTermination()

    spark.stop()
  }
}
