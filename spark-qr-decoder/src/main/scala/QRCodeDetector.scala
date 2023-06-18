package ru.yandex.practicum.de.kk91

import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.opencv_java
import org.opencv.core.Mat
import org.opencv.wechat_qrcode.WeChatQRCode
import scala.jdk.CollectionConverters._
object QRCodeDetector {
  Loader.load(classOf[opencv_java])
  private val detector = new WeChatQRCode()

  def detectAndDecode(height: Int, width: Int, mode: Int, data: Array[Byte]): Seq[String] = {
    try {
      val mat = new Mat(height, width, mode)
      mat.put(0, 0, data)
      val result = detector.detectAndDecode(mat)
      result.asScala
    } catch {
      case e: Throwable => Seq(e.getMessage)
    }
  }

}
