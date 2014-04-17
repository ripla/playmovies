import sbt._

object Resolvers {
  val sonatypeReleases = "Sonatype Release" at "http://oss.sonatype.org/content/repositories/releases"
  val scalaToolsSnapshots = "Scala Tools" at "http://scala-tools.org/repo-snapshots/"
  val jboss = "JBoss" at "http://repository.jboss.org/nexus/content/groups/public/"
  val akka = "Akka" at "http://akka.io/repository/"
  val typesafeReleases = "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
  val sprayRepo = "Spray repo" at "http://repo.spray.io"
  val sprayNightly = "Spray nightly" at "http://nightlies.spray.io"
  val sonatypeOss = "Sonatype OSS" at "http://oss.sonatype.org/content/public"
}