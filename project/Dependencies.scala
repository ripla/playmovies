import sbt._

object Dependencies {

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
  val webjarsPlay = "org.webjars" %% "webjars-play" % "2.2.0"
  val webjarsBootstrap ="org.webjars" % "bootstrap" % "2.3.1"

  // Versions
  object V {
    val Scalatest = "2.0.RC3"
    val Akka = "2.2.0"
    val Spray = "1.2-20131004"
    //nightly required for Akka 2.2 compatibility
    val SprayJson = "1.2.5"
    val ScalaUri = "0.3.6"
    val Jetty = "7.4.0.v20110414"
    val SLF4J = "1.7.5"
    val RabbitMq = "3.1.4"
  }

  object Test {
    val junit = "junit" % "junit" % "4.5" % "test"
    val scalatest = "org.scalatest" %% "scalatest" % V.Scalatest % "test"
    val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % V.Akka % "test"
    val jettyServer = Dependencies.jettyServer % "test"
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % V.SLF4J % "test"
    val mockito = "org.mockito" % "mockito-core" % "1.9.5" % "test"
  }
}