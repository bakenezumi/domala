import Dependencies._
import sbt.Keys.publishArtifact

lazy val _scalaVersion = "2.12.4"
lazy val _version = "0.1.0-beta.9"

lazy val domaVersion = "2.19.1"
lazy val h2Version = "1.4.196"
lazy val scalametaVersion = "1.8.0"

lazy val baseSettings = Seq(
  organization := "com.github.domala",
  version := _version,
  scalaVersion := _scalaVersion,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-Xlint:-options"),
  javacOptions in doc := Seq("-source", "1.8"),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
    ),
  licenses := _licenses,
  homepage := _homepage,
  scmInfo := _scmInfo,
  developers := _developers
)

lazy val root = (project in file(".")).settings(
  baseSettings,
  publish := {},
  publishLocal := {},
  skip in publish := true
) aggregate (core, meta, paradise)

lazy val core = (project in file("core")).settings(
  name := "domala",
  baseSettings,
  compile in Test := ((compile in Test) dependsOn (copyResources in Test)).value,
  libraryDependencies ++= Seq(
    "org.seasar.doma" % "doma" % domaVersion,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "com.h2database" % "h2" % h2Version % Test,
    scalaTest % Test
  )
)

lazy val meta = (project in file("meta")).settings(
  name := "domala-meta",
  baseSettings,
  libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta" % scalametaVersion % Provided,
    "com.h2database" % "h2" % h2Version % Test,
    scalaTest % Test
  )
) dependsOn core

lazy val metaMacroSettings: Seq[Def.Setting[_]] = Seq(
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  scalacOptions in (Compile, console) ~= (_ filterNot (_ contains "paradise")) // macroparadise plugin doesn't work in repl yet.
)

lazy val paradise = (project in file("paradise")).settings(
  name := "domala-paradise",
  baseSettings,
  metaMacroSettings,
  compile in Test := ((compile in Test) dependsOn (copyResources in Test)).value,
  libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta" % scalametaVersion % Provided,
    "com.h2database" % "h2" % h2Version % Test,
    scalaTest % Test
  )
) dependsOn meta

lazy val example = project.settings (
  baseSettings,
  metaMacroSettings,
  compile in Compile := ((compile in Compile) dependsOn (copyResources in Compile)).value,
  libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta" % scalametaVersion % Provided,
    "com.h2database" % "h2" % h2Version,
    scalaTest % Test
  )
) dependsOn paradise

lazy val _licenses = Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
lazy val _homepage = Some(url("https://github.com/bakenezumi"))

lazy val _scmInfo = Some(
  ScmInfo(
    url("https://github.com/bakenezumi/domala"),
    "scm:git@github.com:/bakenezumi/domala.git"
  )
)

lazy val _developers = List(
  Developer(
    id    = "bakenezumi",
    name  = "Nobuhiko Hosonishi",
    email = "hosonioshi@gmail.com",
    url   = url("https://github.com/bakenezumi")
  )
)
