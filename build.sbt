name := "looks3e"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.438",
  "com.sksamuel.elastic4s" %% "elastic4s-http" % "6.4.0",
  "com.sksamuel.elastic4s" %% "elastic4s-circe" % "6.4.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.apache.logging.log4j" % "log4j-core" % "2.11.1"
)

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.sksamuel.elastic4s" %% "elastic4s-embedded" % "6.4.0" % "test",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.25"
)
