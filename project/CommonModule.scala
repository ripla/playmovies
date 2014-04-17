import sbt._
import sbt.Keys._
import Dependencies.akkaActor
import Dependencies.Test
import BuildSettings.projectSettings

object CommonModule {

  lazy val project = Project("common",
    file("common"),
    settings = projectSettings ++
      Seq(libraryDependencies ++= Seq(akkaActor, Test.junit, Test.scalatest, Test.akkaTestKit, Test.mockito)))
}