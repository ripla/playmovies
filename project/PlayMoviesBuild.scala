import sbt._
import com.typesafe.sbt.SbtStartScript
import BuildSettings._

object PlayMoviesBuild extends Build {

  override lazy val settings = super.settings ++ buildSettings

  lazy val root = Project("playmovies",
    file("."),
    settings = projectSettings ++
      Seq(SbtStartScript.stage in Compile := Unit)) aggregate(CommonModule.project, WebModule.project, BackendModule.project)

  lazy val common = CommonModule.project

  lazy val backend = BackendModule.project

  lazy val web = WebModule.project
}
