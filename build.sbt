import Settings._

val versionV = "0.0.5"

ThisBuild / version      := versionV
ThisBuild / scalaVersion := Versions.dotty

ThisBuild / baseVersion := versionV

ThisBuild / githubWorkflowTargetTags           ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / githubWorkflowPublish               := Seq(WorkflowStep.Sbt(List("ci-release")))
ThisBuild / githubWorkflowJavaVersions          := Seq("adopt@1.11")
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("compile"))
)
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE"    -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

ThisBuild / scmInfo := Some(
  ScmInfo(url("https://github.com/vmiroshnikov/authz"), "git@github.com:vmiroshnikov/authz.git")
)
ThisBuild / publishGithubUser := "vmiroshnikov"
ThisBuild / publishFullName   := "Vyacheslav Miroshnikov"
ThisBuild / developers ++= List(
  "vmiroshnikov" -> "Vyacheslav Miroshnikov"
).map { case (username, fullName) =>
  Developer(username, fullName, s"@$username", url(s"https://github.com/$username"))
}

ThisBuild / organization     := "io.github.vlmiroshnikov"
ThisBuild / organizationName := "vlmiroshnikov"

lazy val authz = project
  .in(file("."))
  .enablePlugins(NoPublishPlugin, SonatypeCiRelease)
  .settings(scalaVersion := Versions.dotty)
  .aggregate(`authz-core`, `authz-circe`, `test`)

lazy val `authz-core` = project
  .in(file("authz-core"))
  .settings(
    name                 := "authz-core",
    scalaVersion         := Versions.dotty,
    libraryDependencies ++= authzCoreDeps.map(_.withDottyCompat(scalaVersion.value))
  )

lazy val `authz-circe` = project
  .in(file("authz-circe"))
  .dependsOn(`authz-core`)
  .settings(
    name                 := "authz-circe",
    scalaVersion         := Versions.dotty,
    libraryDependencies ++= authzCirceDeps.map(_.withDottyCompat(scalaVersion.value))
  )

lazy val `test` = project
  .in(file("authz-test"))
  .dependsOn(`authz-core`, `authz-circe`)
  .settings(
    name                 := "authz-test",
    libraryDependencies ++= authzCoreDeps.map(_.withDottyCompat(scalaVersion.value))
  )
