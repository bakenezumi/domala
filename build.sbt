import Dependencies._

lazy val _scalaVersion = "2.12.4"
lazy val _version = "0.1.0-beta.5-SNAPSHOT"

lazy val metaMacroSettings: Seq[Def.Setting[_]] = Seq(
  resolvers += Resolver.sonatypeRepo("releases"),
  resolvers += Resolver.bintrayRepo("scalameta", "maven"),
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  scalacOptions in (Compile, console) := Seq()
)

lazy val root = (project in file(".")).settings(
  inThisBuild(List(
    scalaVersion := _scalaVersion,
    version      := _version
  )),
  name := "domala",
  organization := "com.github.domala",
  javacOptions ++= List("-encoding", "utf8"),
  metaMacroSettings,
  libraryDependencies ++= Seq(
    "org.seasar.doma" % "doma" % "2.18.0",
    "org.scalameta" %% "scalameta" % "1.8.0",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "com.h2database" % "h2" % "1.4.193" % Test,
    scalaTest % Test
  )
)

lazy val sample = project.settings (
  inThisBuild(List(
    scalaVersion := _scalaVersion,
    version      := _version
  )),
  metaMacroSettings,
  libraryDependencies ++= Seq(
    "com.h2database" % "h2" % "1.4.193",
    scalaTest % Test
  )
) dependsOn root

licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("https://github.com/bakenezumi"))

publishMavenStyle := true
publishArtifact in Test := false
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/bakenezumi/domala"),
    "scm:git@github.com:/bakenezumi/domala.git"
  )
)

developers := List(
  Developer(
    id    = "bakenezumi",
    name  = "Nobuhiko Hosonishi",
    email = "hosonioshi@gmail.com",
    url   = url("https://github.com/bakenezumi")
  )
)