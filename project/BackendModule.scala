import BuildSettings._
import Dependencies._
import akka.sbt.AkkaKernelPlugin
import com.typesafe.sbt.SbtStartScript
import sbt._
import sbt.Keys._

object BackendModule {
  lazy val project = Project("backend",
    file("backend"),
    settings = projectSettings ++
      SbtStartScript.startScriptForClassesSettings ++
      Seq(mainClass in Compile := Some("org.risto.playmovie.backend.Bootstrap")) ++
      AkkaKernelPlugin.distSettings ++
      //com.typesafe.sbt.SbtAtmos.atmosSettings ++
      Seq(libraryDependencies ++= Seq(akkaKernel, sprayClient, sprayJson, scalaUri, rabbitMqClient))) dependsOn (CommonModule.project % "compile->compile;test->test")
}