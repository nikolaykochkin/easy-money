import org.apache.spark.sql.{Encoder, SparkSession}
import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.opencv_java
import org.opencv.core.Mat
import org.opencv.wechat_qrcode.WeChatQRCode


object QRDetector {
  Loader.load(classOf[opencv_java])
  private val detector = new WeChatQRCode()

  private def decode(mat: Mat): String = {
    detector.detectAndDecode(mat).get(0)
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("QRDetector")
      .master("local")
      .getOrCreate()

    spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", "8smG21LcbEPMdjeJUXHQ")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", "01HQerzObCNVEPhJCxLYMkDnvdRkcaBtWV5TCDJm")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "http://127.0.0.1:9000")
    spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint.region", "ru-central1")

    import spark.implicits._
    implicit val myObjEncoder: Encoder[Mat] = org.apache.spark.sql.Encoders.kryo[Mat]

    spark.read.format("image")
      .option("dropInvalid", true)
      .load("s3a://easy-money/30da3737-d663-4ebd-b84c-aa746bf4dda9/6/photos/file_42.jpg")
      .cache()
      .select("image.*")
      .map(row => {
        val height = row.getAs[Int]("height")
        val width = row.getAs[Int]("width")
        val mode = row.getAs[Int]("mode")
        val bin = row.getAs[Array[Byte]]("data")
        val mat = new Mat(height, width, mode)
        mat.put(0, 0, bin)
        mat
      }).map(decode)
      .foreach(println)

    spark.stop()
  }
}
