// Comment to get more information during initialization
logLevel := Level.Warn

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Akka Repo" at "http://repo.akka.io/releases/"

addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" % "0.10.0")

// Idea compatibility
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// Microkernel distribution
addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.2.3")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.2")

// web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.0")

// Helps when resolving transitive dependency problems
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

