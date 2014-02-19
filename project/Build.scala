import sbt._
import Keys._
import com.typesafe.sbt.SbtStartScript
import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{Dist, outputDirectory, distJvmOptions}

object BuildSettings {
    import Dependencies._
    import Resolvers._

  lazy val buildSettings = Seq(
    organization := "org.risto",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.10.2",
    scalacOptions ++= Seq("-encoding", "UTF-8",
                          "-deprecation", "-unchecked"),
    fork in test := true,
    libraryDependencies ++= Seq(Test.scalatest),
    resolvers := Seq(scalaToolsSnapshots, jboss, akka, sonatypeOss, sprayRepo, sprayNightly)
  )

   val projectSettings = Defaults.defaultSettings ++ buildSettings
}

object Resolvers {
    val sonatypeReleases = "Sonatype Release" at "http://oss.sonatype.org/content/repositories/releases"
    val scalaToolsSnapshots = "Scala Tools" at "http://scala-tools.org/repo-snapshots/"
    val jboss = "JBoss" at "http://repository.jboss.org/nexus/content/groups/public/"
    val akka = "Akka" at "http://akka.io/repository/"
    val typesafeReleases = "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
    val sprayRepo = "Spray repo" at "http://repo.spray.io"
    val sprayNightly = "Spray nightly" at "http://nightlies.spray.io"
    val sonatypeOss = "Sonatype OSS" at "http://oss.sonatype.org/content/public"
}

object Dependencies {

  // Versions
  object V {
    val Scalatest = "2.0.RC3"
    val Akka = "2.2.0"
    val Spray = "1.2-20131004"     //nightly required for Akka 2.2 compatibility
    val SprayJson = "1.2.5"
    val ScalaUri = "0.3.6"
    val Jetty = "7.4.0.v20110414"
    val SLF4J = "1.7.5"
    val RabbitMq = "3.1.4"
  }

  // Compile
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % V.Akka
  val akkaKernel = "com.typesafe.akka" %% "akka-kernel" % V.Akka
  val akkaSlf4j = "com.typesafe.akka" % "akka-slf4j" % V.Akka
  val scalatest = "org.scalatest" %% "scalatest" % V.Scalatest
  val sprayClient = "io.spray" % "spray-client" % V.Spray
  val sprayJson = "io.spray" %% "spray-json" % V.SprayJson
  val scalaUri = "com.github.theon" %% "scala-uri" % V.ScalaUri
  val jettyServer = "org.eclipse.jetty" % "jetty-server" % V.Jetty
  val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % V.Jetty
  val slf4j = "org.slf4j" % "slf4j-simple" % V.SLF4J
  val rabbitMqClient = "com.rabbitmq" % "amqp-client" % V.RabbitMq

  object Test {
    val junit       = "junit" % "junit" % "4.5" % "test"
    val scalatest   = "org.scalatest" %% "scalatest" % V.Scalatest % "test"
    val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % V.Akka % "test"
    val jettyServer = Dependencies.jettyServer % "test"
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % V.SLF4J % "test"
  }
}

object PlayMoviesBuild extends Build {
    import BuildSettings._
    import Dependencies._

    override lazy val settings = super.settings ++ buildSettings

    lazy val root = Project("playmovies",
                            file("."),
                            settings = projectSettings ++
                              Seq(SbtStartScript.stage in Compile := Unit)) aggregate(common, web, backend)

    lazy val web = Project("web",
                            file("web"),
                            settings = projectSettings ++
                              SbtStartScript.startScriptForClassesSettings ++
                              Seq(libraryDependencies ++= Seq(jettyServer, jettyServlet, slf4j),
                                distJvmOptions in Dist := "-Xms256M -Xmx1024M",
                                outputDirectory in Dist := file("target/playmovieDist"))) dependsOn(common % "compile->compile;test->test")

    lazy val backend = Project("backend",
                            file("backend"),
                            settings = projectSettings ++
                              SbtStartScript.startScriptForClassesSettings ++
                              AkkaKernelPlugin.distSettings ++
                              Seq(libraryDependencies ++= Seq(akkaKernel,sprayClient,sprayJson,scalaUri,rabbitMqClient))) dependsOn(common % "compile->compile;test->test")

    lazy val common = Project("common",
                            file("common"),
                            settings = projectSettings ++
                              Seq(libraryDependencies ++= Seq(akkaActor,Test.junit,Test.scalatest,Test.akkaTestKit)))
}
