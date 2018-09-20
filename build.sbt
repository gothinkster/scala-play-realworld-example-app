name := """real-world-example-project"""

version := "1.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.12.7"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
  filters,
  evolutions,
  ws,
  ehcache,
  cacheApi,
  "com.typesafe.play" %% "play-json" % "2.6.10",
  "org.julienrf" %% "play-json-derived-codecs" % "4.0.1",
  "com.typesafe.play" %% "play-slick" % "3.0.3",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3",
  "commons-validator" % "commons-validator" % "1.6",
  "com.github.slugify" % "slugify" % "2.2",
  "com.h2database" % "h2" % "1.4.197",
  "org.mindrot" % "jbcrypt" % "0.4",
  "org.pac4j" %% "play-pac4j" % "5.0.0",
  "org.pac4j" % "pac4j-jwt" % "2.3.1",
  "org.pac4j" % "pac4j-http" % "2.3.1",

  "com.softwaremill.macwire" %% "macros" % "2.3.1" % "provided",

  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
)

fork in run := true