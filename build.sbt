name := "looks3e"

version := "0.1"

scalaVersion := "2.12.7"

val http4sVersion = "0.18.20"

val awsSdkVersion = "1.11.438"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion,
  "com.amazonaws" % "aws-java-sdk-simpledb" % awsSdkVersion
)

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion
)