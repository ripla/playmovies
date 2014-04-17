import Dependencies._
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
        Seq(libraryDependencies ++= Seq(webjarsBootstrap, webjarsPlay))) dependsOn (CommonModule.project % "compile->compile;test->test")

    /*++
    distJvmOptions in Dist := "-Xms256M -Xmx1024M",
    outputDirectory in Dist := file("target/playmovieDist"))) */
  }
}