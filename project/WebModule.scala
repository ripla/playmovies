import Dependencies._
import Dependencies.Test
import com.typesafe.sbt.SbtNativePackager
import sbt._
import sbt.Keys._
import play.Play.autoImport._
import PlayKeys._
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

object WebModule {

  lazy val project = {
    Project("web",
      file("web"),
      settings = BuildSettings.projectSettings ++
        //playScalaSettings ++
        //packagerSettings ++
        //packageArchetype.java_application ++
        //com.typesafe.sbt.SbtAtmos.atmosSettings ++
        Seq(libraryDependencies ++= Seq(webjarsBootstrap, webjarsPlay, Test.scalatestPlusPlay))) dependsOn (CommonModule.project % "compile->compile;test->test")

    /*++
    distJvmOptions in Dist := "-Xms256M -Xmx1024M",
    outputDirectory in Dist := file("target/playmovieDist"))) */
  }.enablePlugins(play.PlayScala)
}