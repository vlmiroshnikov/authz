import sbt._
import sbt.Keys._

object Versions {
  val dotty      = "3.1.0"
  val cats       = "2.7.0"
  val catsEffect = "3.3.1"
  val munit      = "0.7.29"
  val circe      = "0.14.1"
  val codecs     = "1.15"
}

object Settings {

  lazy val settings = Seq(
    scalacOptions ++= Seq("-new-syntax", "-rewrite")
  )

  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-parser"
  ).map(_ % Versions.circe)

  lazy val cats       = Seq("org.typelevel" %% "cats-core").map(_ % Versions.cats)
  lazy val catsEffect = Seq("org.typelevel" %% "cats-effect").map(_ % Versions.catsEffect)
  lazy val codecs     = Seq("commons-codec" % "commons-codec" % Versions.codecs)
  lazy val munit      = Seq("org.scalameta" %% "munit" % Versions.munit % Test)
}
