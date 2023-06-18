ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"
ThisBuild / organization := "ru.yandex.practicum.de.kk91"

ThisBuild / assemblyMergeStrategy := {
  case "META-INF/native-image/linux-x86_64/jnijavacpp/jni-config.json" => MergeStrategy.discard
  case "META-INF/native-image/linux-x86_64/jnijavacpp/reflect-config.json" => MergeStrategy.discard
  case "META-INF/versions/9/module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

val sparkVersion = "3.4.0"
val opencvVersion = "4.7.0"
val javacppVersion = "1.5.9"
val awsSdkVersion = "1.12.262"
val hadoopAwsVersion = "3.3.4"

val platform = "linux-x86_64"

lazy val root = (project in file("."))
  .settings(
    name := "spark-qr-image-processor",

    javaCppVersion := javacppVersion,
    javaCppPlatform := Seq(platform),
    javaCppPresetLibs ++= Seq("opencv" -> opencvVersion),

    fork := true,

    assembly / mainClass := Some("ru.yandex.practicum.de.kk91.QRImageProcessor"),
    assembly / assemblyJarName := "spark-qr-image-processor.jar",

    Compile / run := Defaults.runTask(Compile / fullClasspath, Compile / run / mainClass, Compile / run / runner).evaluated,

    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled"),
    scalacOptions ++= Seq("-deprecation", "-unchecked"),

    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
      "org.apache.hadoop" % "hadoop-aws" % hadoopAwsVersion % "provided",
      "com.amazonaws" % "aws-java-sdk-bundle" % awsSdkVersion,
      "org.bytedeco" % "javacpp" % javacppVersion classifier platform,
      "org.bytedeco" % "javacv" % javacppVersion,
      "org.bytedeco" % "opencv" % s"$opencvVersion-$javacppVersion" classifier platform
    )
  )
