import sbt._

object Versions {
  val dotty      = "3.0.0-RC3"
  val circe      = "0.14.0-M6"
  val codecs     = "1.15"
  val cats       = "2.6.0"
  val catsEffect = "3.1.0"
  val munit      = "0.7.25"
}

object Settings {
  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-parser"
  ).map(_ % Versions.circe)

  lazy val cats       = Seq("org.typelevel" %% "cats-core").map(_ % Versions.cats)
  lazy val catsEffect = Seq("org.typelevel" %% "cats-effect").map(_ % Versions.catsEffect)
  lazy val codecs     = Seq("commons-codec" % "commons-codec" % Versions.codecs)
  lazy val munit      = Seq("org.scalameta" %% "munit" % Versions.munit % Test)
}
