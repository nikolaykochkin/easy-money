ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"
ThisBuild / organization := "ru.yandex.practicum.de.kk91"

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", "native-image", ps @ _*) if ps.last endsWith "jni-config.json" => MergeStrategy.discard
  case PathList("META-INF", "native-image", ps @ _*) if ps.last endsWith "reflect-config.json" => MergeStrategy.discard
  case "META-INF/substrate/config/reflectionconfig.json" => MergeStrategy.discard
  case "META-INF/versions/9/module-info.class" => MergeStrategy.discard
  case PathList("module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

val sparkVersion = "3.4.0"
val opencvVersion = "4.7.0"
val openblasVersion = "0.3.23"
val ffmpegVersion = "6.0"
val javacppVersion = "1.5.9"
val awsSdkVersion = "1.12.262"
val hadoopAwsVersion = "3.3.4"

val platform = org.bytedeco.javacpp.Loader.Detector.getPlatform

lazy val root = (project in file("."))
  .settings(
    name := "spark-qr-image-processor",

    javaCppVersion := javacppVersion,
    javaCppPlatform := Seq(platform),
    javaCppPresetLibs ++= Seq(
      "opencv" -> opencvVersion,
      "openblas" -> openblasVersion,
      "ffmpeg" -> ffmpegVersion
    ),

    fork := true,

    assembly / mainClass := Some("ru.yandex.practicum.de.kk91.QRImageProcessor"),
    assembly / assemblyJarName := "spark-qr-image-processor.jar",

    Compile / run := Defaults.runTask(Compile / fullClasspath, Compile / run / mainClass, Compile / run / runner).evaluated,

    javaOptions ++= Seq("-Xms512M", "-Xmx2048M"),
    scalacOptions ++= Seq("-deprecation", "-unchecked"),

    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
      "org.apache.hadoop" % "hadoop-aws" % hadoopAwsVersion % "provided",
      "com.amazonaws" % "aws-java-sdk-bundle" % awsSdkVersion % "provided",
      "org.bytedeco" % "javacpp" % javacppVersion classifier platform,
      "org.bytedeco" % "javacv" % javacppVersion,
      "org.bytedeco" % "opencv" % s"$opencvVersion-$javacppVersion",
      "org.bytedeco" % "opencv" % s"$opencvVersion-$javacppVersion" classifier platform,
      "org.bytedeco" % "openblas" % s"$openblasVersion-$javacppVersion",
      "org.bytedeco" % "openblas" % s"$openblasVersion-$javacppVersion" classifier platform,
      "org.bytedeco" % "ffmpeg" % s"$ffmpegVersion-$javacppVersion",
      "org.bytedeco" % "ffmpeg" % s"$ffmpegVersion-$javacppVersion" classifier platform
    )
  )
