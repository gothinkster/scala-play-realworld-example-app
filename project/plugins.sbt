// Comment to get more information during initialization
//logLevel := Level.Warn


resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.1")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.7")
