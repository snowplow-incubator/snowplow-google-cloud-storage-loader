import sbt._

lazy val root: Project = project
  .in(file("."))
  .settings(BuildSettings.commonSettings)
  .settings(BuildSettings.macroSettings)
  .settings(BuildSettings.appSettings)
  .settings(BuildSettings.dockerSettingsFocal)
  .enablePlugins(JavaAppPackaging)

lazy val repl: Project = project
  .in(file(".repl"))
  .settings(BuildSettings.commonSettings)
  .settings(BuildSettings.macroSettings)
  .settings(BuildSettings.replSettings)
  .dependsOn(root)
