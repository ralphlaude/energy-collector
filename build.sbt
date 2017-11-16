import sbt.project

name := "vermietetde-energy-collector"

organization in ThisBuild := "vermietetde-energy-collector"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java


lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(energyCollectorServiceApi, energyCollectorServiceImpl)


lazy val energyCollectorServiceApi = (project in file("energy-collector-service-api"))
  .settings(commonSettings: _*)
    .settings(
      version := "1.0-SNAPSHOT",
      libraryDependencies ++= Seq(lagomJavadslApi, lagomJavadslImmutables, lagomLogback)
    )

lazy val energyCollectorServiceImpl = (project in file("energy-collector-service-impl"))
    .enablePlugins(LagomJava)
    .settings(commonSettings: _*)
    .settings(
      version := "1.0-SNAPSHOT",
      libraryDependencies ++= Seq(
        lagomJavadslPersistenceCassandra,
        lagomJavadslImmutables,
        lagomJavadslTestKit,
        "org.hamcrest" % "hamcrest-all" % "1.3" % Test
      )
    )
    .settings(lagomForkedTestSettings: _*)
    .dependsOn(energyCollectorServiceApi)


lagomCassandraCleanOnStart in ThisBuild := true

def commonSettings: Seq[Setting[_]] = Seq(
  javacOptions in compile ++=
    Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation", "-parameters")
)

