import sbt._

object Versions {
  val dotty  = "3.0.0-M2"
  val circe  = "0.13.0"
  val codecs = "1.15"
  val cats   = "2.2.0"
}

object Settings {
  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-parser"
  ).map(_ % Versions.circe)

  lazy val cats = Seq(
    "org.typelevel" %% "cats-core",
    "org.typelevel" %% "cats-effect"
  ).map(_ % Versions.cats)

  lazy val codecs = Seq("commons-codec" % "commons-codec" % Versions.codecs)
  lazy val test   = Seq("org.scalameta" %% "munit" % "0.7.19" % Test)

  val authzCoreDeps  = cats ++ codecs ++ test
  val authzCirceDeps = authzCoreDeps ++ circe
}
