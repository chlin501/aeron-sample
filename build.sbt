
name := "aeron-sample"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq (
  "io.aeron" % "aeron-all" % "1.9.3",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

