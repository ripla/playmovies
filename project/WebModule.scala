import Dependencies._
import Dependencies.Test
import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.rjs.Import.RjsKeys._
import com.typesafe.sbt.web.SbtWeb
import sbt._
import sbt.Keys._
import play.Play.autoImport._
import PlayKeys._
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._
import com.typesafe.sbt.less.Import.LessKeys._
import com.typesafe.sbt.web.Import.Assets._
import com.typesafe.sbt.web.pipeline.Pipeline._
import com.typesafe.sbt.web.SbtWeb.autoImport._
import com.typesafe.sbt.less.Import.LessKeys
import WebJs._
import com.typesafe.sbt.rjs.Import._
import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._

object WebModule {

  lazy val project = {
    Project("web",
      file("web"),
      settings = BuildSettings.projectSettings ++
        //playScalaSettings ++
        //packagerSettings ++
        //packageArchetype.java_application ++
        //com.typesafe.sbt.SbtAtmos.atmosSettings ++
        Seq(pipelineStages := Seq(rjs, digest, gzip)) ++
        Seq(includeFilter in (Assets, LessKeys.less) := "*.less") ++
        Seq(libraryDependencies ++= Seq(webjarsBootstrap, webjarsPlay, Test.scalatestPlusPlay))) dependsOn (CommonModule.project % "compile->compile;test->test")

    /*++
    distJvmOptions in Dist := "-Xms256M -Xmx1024M",
    outputDirectory in Dist := file("target/playmovieDist"))) */
  }.enablePlugins(play.PlayScala, SbtWeb)
}