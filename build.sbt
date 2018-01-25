import Dependencies._

lazy val _scalaVersion = "2.12.4"
lazy val _version = "0.1.0-beta.9-SNAPSHOT"

lazy val baseSettings = Seq(
  organization := "com.github.domala",
  version := _version,
  scalaVersion := _scalaVersion,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-Xlint:-options"),
  javacOptions in doc := Seq("-source", "1.8")
)

lazy val root = (project in file(".")).settings(
  baseSettings
) aggregate (core, meta, paradise)

lazy val core = (project in file("core")).settings(
  name := "domala",
  baseSettings,
  compile in Test := ((compile in Test) dependsOn (copyResources in Test)).value,
  libraryDependencies ++= Seq(
    "org.seasar.doma" % "doma" % "2.19.1",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "com.h2database" % "h2" % "1.4.193" % Test,
    scalaTest % Test
  )
)

lazy val meta = (project in file("meta")).settings(
  name := "domala-meta",
  baseSettings,
  libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta" % "1.8.0" % Provided,
    "com.h2database" % "h2" % "1.4.193" % Test,
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
    "org.scalameta" %% "scalameta" % "1.8.0" % Provided,
    "com.h2database" % "h2" % "1.4.193" % Test,
    scalaTest % Test
  )
) dependsOn meta

lazy val example = project.settings (
  baseSettings,
  metaMacroSettings,
  compile in Compile := ((compile in Compile) dependsOn (copyResources in Compile)).value,
  libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta" % "1.8.0" % Provided,
    "com.h2database" % "h2" % "1.4.193",
    scalaTest % Test
  )
) dependsOn paradise

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
