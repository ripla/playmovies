import sbt._
import Keys._
import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, outputDirectory, distJvmOptions}

object BackendBuild extends Build {

  lazy val BackedKernel = Project(
    id = "playmovie-backend",
    base = file("."),
    settings = defaultSettings ++ AkkaKernelPlugin.distSettings
      ++ Seq(
      libraryDependencies ++= Dependencies.backendKernel,
      distJvmOptions in Dist := "-Xms256M -Xmx1024M",
      outputDirectory in Dist := file("target/playmovieDist")
    )
  )

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "org.risto",
    version      := "0.1-SNAPSHOT",
    scalaVersion := "2.10.2"
  )

  lazy val defaultSettings = buildSettings ++ Seq(
    resolvers += "Typesafe Repo" at
      "http://repo.typesafe.com/typesafe/releases/",

    // compile options
    scalacOptions ++= Seq("-encoding", "UTF-8",
      "-deprecation",
      "-unchecked"),
    javacOptions  ++= Seq("-Xlint:unchecked",
      "-Xlint:deprecation")

  )
}
// Dependencies

object Dependencies {
  import Dependency._
  val backendKernel = Seq(akkaActor,
    akkaKernel,
    akkaCamel,
    Test.junit,
    Test.scalatest,
    Test.akkaTestKit)
}

object Dependency {

  // Versions
  object V {
    val Scalatest    = "1.9.1"
    val Akka         = "2.2.0"
  }

  // Compile
  val akkaActor     = "com.typesafe.akka" %% "akka-actor"   % V.Akka
  val akkaKernel    = "com.typesafe.akka" %% "akka-kernel"  % V.Akka
  val akkaCamel     = "com.typesafe.akka" %% "akka-camel"   % V.Akka
  val akkaSlf4j     = "com.typesafe.akka" % "akka-slf4j"    % V.Akka
  val scalatest     = "org.scalatest"     %% "scalatest"    % V.Scalatest

  object Test {
    val junit       = "junit" % "junit" %
      "4.5"        % "test"
    val scalatest   = "org.scalatest"     %% "scalatest" %
      V.Scalatest  % "test"
    val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" %
      V.Akka % "test"
  }
}
