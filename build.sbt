
name := "aeron-sample"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq (
  "io.aeron" % "aeron-all" % "1.11.1",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

