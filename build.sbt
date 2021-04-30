import Settings._
import xerial.sbt.Sonatype._

val versionV = "0.2.1"

ThisBuild / version      := versionV
ThisBuild / scalaVersion := Versions.dotty

ThisBuild / githubWorkflowTargetTags           ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / githubWorkflowPublish               := Seq(WorkflowStep.Sbt(List("release")))
ThisBuild / githubWorkflowJavaVersions          := Seq("adopt@1.11")
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("compile"))
)
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("release"),
    env = Map(
      "PGP_PASSPHRASE"    -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)
ThisBuild / credentials += Credentials("Sonatype Nexus Repository Manager",
                                       "oss.sonatype.org",
                                       sys.env.getOrElse("SONATYPE_USERNAME", ""),
                                       sys.env.getOrElse("SONATYPE_PASSWORD", "")
)

ThisBuild / scmInfo := Some(
  ScmInfo(url("https://github.com/vlmiroshnikov/authz"), "git@github.com:vlmiroshnikov/authz.git")
)
ThisBuild / developers ++= List(
  "vlmiroshnikov" -> "Vyacheslav Miroshnikov"
).map { case (username, fullName) =>
  Developer(username, fullName, s"@$username", url(s"https://github.com/$username"))
}

ThisBuild / organization     := "io.github.vlmiroshnikov"
ThisBuild / organizationName := "vlmiroshnikov"

lazy val release = taskKey[Unit]("Release")
addCommandAlias("release", "; reload; project /; publishSigned; sonatypeBundleRelease")

def publishSettings = Seq(
  sonatypeProfileName := "io.github.vlmiroshnikov",
  publishMavenStyle   := true,
  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
  sonatypeProjectHosting := Some(GitHubHosting("vlmiroshnikov", "authz", "vlmiroshnikov@gmai.com")),
  homepage               := Some(url("https://github.com/vlmiroshnikov/authz")),
  publishTo              := sonatypePublishToBundle.value,
  useGpgPinentry         := Option(System.getenv("PGP_PASSPHRASE")).isDefined
)

lazy val authz = project
  .in(file("."))
  .settings(scalaVersion := Versions.dotty)
  .aggregate(`authz-core`, `authz-circe`)
  .settings(
    publish         := {},
    publishLocal    := {},
    publishArtifact := false,
    publish / skip  := true
  )

lazy val `authz-core` = project
  .in(file("authz-core"))
  .settings(
    name         := "authz-core",
    scalaVersion := Versions.dotty,
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= cats ++ codecs ++ munit
  )
  .settings(publishSettings)

lazy val `authz-circe` = project
  .in(file("authz-circe"))
  .dependsOn(`authz-core`)
  .settings(
    name         := "authz-circe",
    scalaVersion := Versions.dotty,
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= circe ++ cats ++ munit
  )
  .settings(publishSettings)

lazy val example = project
  .in(file("authz-test"))
  .dependsOn(`authz-core`, `authz-circe`)
  .settings(
    name := "authz-example",
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= munit ++ catsEffect
  )
