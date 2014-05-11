import Dependencies._
import Dependencies.Test
import sbt._
import sbt.Keys._

object WebModule {
  lazy val project = {
    import play.Project._
    Project("web",
      file("web"),
      settings = BuildSettings.projectSettings ++
        playScalaSettings ++
        com.typesafe.sbt.SbtAtmos.atmosSettings ++
        Seq(libraryDependencies ++= Seq(webjarsBootstrap, webjarsPlay, Test.scalatestPlus))) dependsOn (CommonModule.project % "compile->compile;test->test")

    /*++
    distJvmOptions in Dist := "-Xms256M -Xmx1024M",
    outputDirectory in Dist := file("target/playmovieDist"))) */
  }
}