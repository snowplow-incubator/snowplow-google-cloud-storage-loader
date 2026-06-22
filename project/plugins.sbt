// scalastyle (1.0.0, unmaintained) pulls scala-xml 1.0.6 while the newer
// sbt-native-packager pulls 2.x; allow the newer to win during plugin resolution.
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.1.1")
addSbtPlugin("com.snowplowanalytics" % "sbt-snowplow-release" % "0.7.0")
