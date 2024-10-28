import sbt._

ThisBuild / libraryDependencySchemes += "com.github.luben"  % "zstd-jni" % VersionScheme.Always 

lazy val root: Project = project
  .in(file("."))
  .settings(BuildSettings.commonSettings)
  .settings(BuildSettings.macroSettings)
  .settings(BuildSettings.appSettings)
  .enablePlugins(JavaAppPackaging, SnowplowDockerPlugin)

lazy val distroless: Project = project
  .in(file("distroless"))
  .settings(sourceDirectory := (root / sourceDirectory).value)
  .settings(BuildSettings.commonSettings)
  .settings(BuildSettings.macroSettings)
  .settings(BuildSettings.appSettings)
  .enablePlugins(JavaAppPackaging, SnowplowDistrolessDockerPlugin)

lazy val repl: Project = project
  .in(file(".repl"))
  .settings(BuildSettings.commonSettings)
  .settings(BuildSettings.macroSettings)
  .settings(BuildSettings.replSettings)
  .dependsOn(root)
