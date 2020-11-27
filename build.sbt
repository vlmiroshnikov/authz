import Settings._

val versionV = "0.0.5"

inThisBuild(
  scalaVersion := Versions.dotty
)

lazy val authz = project
  .in(file("."))
  .aggregate(`authz-core`, `authz-circe`, `test`)

lazy val `authz-core` = project
  .in(file("authz-core"))
  .settings(
    name                 := "authz-core",
    publishMavenStyle    := true,
    organization         := "com.github.vlmiroshnikov",
    version              := versionV,
    libraryDependencies ++= authzCoreDeps.map(_.withDottyCompat(scalaVersion.value))
  )

lazy val `authz-circe` = project
  .in(file("authz-circe"))
  .dependsOn(`authz-core`)
  .settings(
    name                 := "authz-circe",
    organization         := "com.github.vlmiroshnikov",
    version              := versionV,
    libraryDependencies ++= authzCirceDeps.map(_.withDottyCompat(scalaVersion.value))
  )

lazy val `test` = project
  .in(file("authz-test"))
  .dependsOn(`authz-core`, `authz-circe`)
  .settings(
    name                 := "authz-test",
    organization         := "com.github.vlmiroshnikov",
    version              := versionV,
    libraryDependencies ++= authzCoreDeps.map(_.withDottyCompat(scalaVersion.value))
  )
