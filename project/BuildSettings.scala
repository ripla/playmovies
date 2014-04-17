import sbt._
import Keys._
import Dependencies.Test
import Resolvers._

object BuildSettings {

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