
import com.johnsnowlabs.nlp.annotators.audio.Wav2Vec2ForCTC
import com.johnsnowlabs.nlp.{AudioAssembler, SparkNLP}
import com.sun.media.sound.AudioFloatInputStream
import org.apache.spark.ml.Pipeline
import org.bytedeco.ffmpeg.ffmpeg
import org.bytedeco.javacpp.Loader

import java.io.File


object SpeechToText {
  private val ffmpeg: String = Loader.load(classOf[ffmpeg])

  def main(args: Array[String]): Unit = {
    val spark = SparkNLP.start(apple_silicon = true)
    import spark.implicits._

    val is = AudioFloatInputStream.getInputStream(new File("file_56.wav"))

    val rawFloats: Array[Float] = new Array[Float](is.available())
    is.read(rawFloats)

    val audioDf = Seq(rawFloats).toDF("audio_content")

    val audioAssembler = new AudioAssembler()
      .setInputCol("audio_content")
      .setOutputCol("audio_assembler")

    //    val speechToText = HubertForCTC
    //      .pretrained("asr_swin_exp_w2v2t_ru_hubert_s818", "ru")
    //      .setInputCols("audio_assembler")
    //      .setOutputCol("text")
    val speechToText = Wav2Vec2ForCTC
      .pretrained("asr_wav2vec2_large_xlsr_53_russian_by_jonatasgrosman", "ru")
      .setInputCols("audio_assembler")
      .setOutputCol("text")

    val pipeline = new Pipeline().setStages(Array(audioAssembler, speechToText))

    val pipelineModel = pipeline.fit(audioDf)

    val pipelineDF = pipelineModel.transform(audioDf)

    pipelineDF.show()

    //    val pb = new ProcessBuilder(ffmpeg, "-y", "-i", "file_56.oga", "-f", "s16le", "-acodec", "pcm_s16le", "file_56.raw")
    //    pb.inheritIO().start().waitFor()
    //
    //    val af = AudioSystem.getAudioFileFormat(new File("file_56.raw"))
    //    println(af)
    //    println(af.getFormat.getSampleRate)
    //
    //    floatFileReader.getAudioInputStream(new File("file_56.wav"))
    //
    //
    //    val df = spark.read.format("binaryFile").load("file_56.wav")
    //    df.show()
    //    val audioAssembler = new AudioAssembler()
    //      .setInputCol("audio_content")
    //      .setOutputCol("audio_assembler")
    //
    //    val speechToText = Wav2Vec2ForCTC
    //      .pretrained("asr_wav2vec2_large_golos", "ru")
    //      .setInputCols("audio_assembler")
    //      .setOutputCol("text")
    //
    //    val pipeline = new Pipeline().setStages(Array(audioAssembler, speechToText))
    //
    //    val pipelineModel = pipeline.fit(audioDf)
    //
    //    val pipelineDF = pipelineModel.transform(audioDf)
    //
    //    pipelineDF.show()
    spark.stop()
  }
}
