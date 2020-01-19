name := """real-world-example-project"""

version := "1.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.13.1"

javacOptions ++= Seq("-source", "11", "-target", "11")

libraryDependencies ++= Seq(
  filters,
  evolutions,
  ws,
  ehcache,
  cacheApi,
  "com.typesafe.play" %% "play-json" % "2.8.1",
  "org.julienrf" %% "play-json-derived-codecs" % "7.0.0",
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "commons-validator" % "commons-validator" % "1.6",
  "com.github.slugify" % "slugify" % "2.4",
  "com.h2database" % "h2" % "1.4.200",
  "org.mindrot" % "jbcrypt" % "0.4",
  "org.apache.commons" % "commons-lang3" % "3.9",

  "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided",

  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % "test",
)

fork in run := true