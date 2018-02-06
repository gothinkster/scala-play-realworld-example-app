name := """play-scala-slick-compile-time-di-example-project"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  filters,
  evolutions,
  ws,
  ehcache,
  cacheApi,
  "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided",
  "com.typesafe.play" %% "play-json" % "2.6.8",
  "com.typesafe.play" %% "play-slick" % "3.0.3",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3",
  "com.github.slugify" % "slugify" % "2.2",
  "com.h2database" % "h2" % "1.4.187",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
  "org.mindrot" % "jbcrypt" % "0.4",
  "org.julienrf" %% "play-json-derived-codecs" % "4.0.0",
  "org.pac4j" %% "play-pac4j" % "5.0.0",
  "org.pac4j" % "pac4j-jwt" % "2.2.1",
  "org.pac4j" % "pac4j-http" % "2.2.1",
  "commons-validator" % "commons-validator" % "1.6"
)

fork in run := true