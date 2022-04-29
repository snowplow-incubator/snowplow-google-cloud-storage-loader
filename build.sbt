import sbt._

lazy val root: Project = project
  .in(file("."))
  .settings(BuildSettings.commonSettings)
  .settings(BuildSettings.macroSettings)
  .settings(BuildSettings.appSettings)
  .settings(BuildSettings.dockerSettingsFocal)
  .enablePlugins(JavaAppPackaging)

lazy val distroless: Project = project
  .in(file("distroless"))
  .settings(sourceDirectory := (root / sourceDirectory).value)
  .settings(BuildSettings.commonSettings)
  .settings(BuildSettings.macroSettings)
  .settings(BuildSettings.appSettings)
  .settings(BuildSettings.dockerSettingsDistroless)
  .enablePlugins(DockerPlugin, LauncherJarPlugin)

lazy val repl: Project = project
  .in(file(".repl"))
  .settings(BuildSettings.commonSettings)
  .settings(BuildSettings.macroSettings)
  .settings(BuildSettings.replSettings)
  .dependsOn(root)
