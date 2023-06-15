ThisBuild / organization := "ru.yandex.practicum.de.kk91"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"

lazy val sparkVersion = "3.4.0"
lazy val sparkNlpVersion = "4.4.3"
lazy val opencvPlatformVersion = "4.7.0-1.5.9"
lazy val ffmpegPlatformVersion = "6.0-1.5.9"
lazy val awsSdkVersion = "1.12.262"
lazy val hadoopAwsVersion = "3.3.4"

lazy val commonDependencies = Seq(
  libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion,
  libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion,
  libraryDependencies += "org.apache.spark" %% "spark-streaming" % sparkVersion,
  libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % hadoopAwsVersion,
  libraryDependencies += "org.apache.spark" %% "spark-mllib" % sparkVersion,
  libraryDependencies += "com.amazonaws" % "aws-java-sdk-bundle" % awsSdkVersion
)

lazy val root = (project in file("."))
  .aggregate(nlp, qrdetector)

lazy val nlp = (project in file("nlp"))
  .settings(
    commonDependencies,
    libraryDependencies += "org.bytedeco" % "ffmpeg-platform" % ffmpegPlatformVersion,
//    libraryDependencies += "com.johnsnowlabs.nlp" %% "spark-nlp" % sparkNlpVersion,
    libraryDependencies += "com.johnsnowlabs.nlp" %% "spark-nlp-silicon" % sparkNlpVersion
  )

lazy val qrdetector = (project in file("qrdetector"))
  .settings(
    commonDependencies,
    libraryDependencies += "org.bytedeco" % "opencv-platform" % opencvPlatformVersion
  )

