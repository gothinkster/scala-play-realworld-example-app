name := """real-world-example-project"""

version := "1.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.12.8"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
  filters,
  evolutions,
  ws,
  ehcache,
  cacheApi,
  "com.typesafe.play" %% "play-json" % "2.7.1",
  "org.julienrf" %% "play-json-derived-codecs" % "5.0.0", // 5.0.0
  "com.typesafe.play" %% "play-slick" % "4.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0",
  "commons-validator" % "commons-validator" % "1.6",
  "com.github.slugify" % "slugify" % "2.3",
  "com.h2database" % "h2" % "1.4.197",
  "org.mindrot" % "jbcrypt" % "0.4",
  "org.pac4j" %% "play-pac4j" % "7.0.0",
  "org.pac4j" % "pac4j-jwt" % "3.5.0",
  "org.pac4j" % "pac4j-http" % "3.5.0",
  "org.apache.commons" % "commons-lang3" % "3.8.1",

  "com.softwaremill.macwire" %% "macros" % "2.3.1" % "provided",

  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % "test",
)

fork in run := true