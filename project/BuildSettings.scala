import sbt._
import Keys._
import Dependencies.Test
import Resolvers._

object BuildSettings {

  lazy val buildSettings = Seq(
    organization := "org.risto",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.1",
    scalacOptions ++= Seq("-encoding", "UTF-8",
      "-deprecation", "-unchecked"),
    fork in test := true,
    libraryDependencies ++= Seq(Test.scalatest),
    resolvers := Seq(scalaToolsSnapshots, jboss, akka, sonatypeOss, sprayRepo, sprayNightly, typesafeReleases)
  )

  val projectSettings = buildSettings ++ net.virtualvoid.sbt.graph.Plugin.graphSettings
}