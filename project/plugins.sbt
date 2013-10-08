addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" % "0.10.0")

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0-SNAPSHOT")

resolvers += "Akka Repo" at "http://repo.akka.io/releases/"

addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.2.1")
