import Dependencies._

lazy val metaMacroSettings: Seq[Def.Setting[_]] = Seq(
  resolvers += Resolver.sonatypeRepo("releases"),
  resolvers += Resolver.bintrayRepo("scalameta", "maven"),
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M9" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  scalacOptions in (Compile, console) := Seq()
)

lazy val root = (project in file(".")).settings(
  inThisBuild(List(
    scalaVersion := "2.12.2",
    version      := "0.1.0-SNAPSHOT"
  )),
  name := "domala",
  metaMacroSettings,
  libraryDependencies ++= Seq(
    "org.seasar.doma" % "doma" % "2.16.1",
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
    "org.scalameta" %% "scalameta" % "1.8.0",
    "org.scalameta" %% "contrib" % "1.8.0",
    "com.h2database" % "h2" % "1.4.193" % Test,
    scalaTest % Test
  )
)

lazy val sample = (project in file("sample")).settings(
  inThisBuild(List(
    scalaVersion := "2.12.2",
    version      := "0.1.0-SNAPSHOT"
  )),
  libraryDependencies ++= Seq(
    "com.h2database" % "h2" % "1.4.193",
    scalaTest % Test
  ),
  metaMacroSettings
) dependsOn (root) aggregate (root)
