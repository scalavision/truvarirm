val stdOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-explaintypes",
  "-Yrangepos",
  "-feature",
  "-Xfuture",
  "-Ypartial-unification",
  "-language:higherKinds",
  "-language:existentials",
  "-unchecked",
  "-Yno-adapted-args",
  "-Xlint:_,-type-parameter-shadow",
  "-Xsource:2.13",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfatal-warnings"
)

def extraOptions(scalaVersion: String) =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 13)) =>
      Seq(
        "-opt-warnings",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_,imports",
        "-Ywarn-unused:imports",
        "-opt:l:inline",
        "-opt-inline-from:<source>"
      )
    case Some((2, 12)) =>
      Seq(
        "-opt-warnings",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_,imports",
        "-Ywarn-unused:imports",
        "-opt:l:inline",
        "-opt-inline-from:<source>"
      )
    case _ =>
      Seq(
        "-Xexperimental",
        "-Ywarn-unused-import"
      )
  }

def stdSettings(prjName: String) = Seq(
    name := s"vcf-analytics4s",
    scalacOptions := stdOptions,
    scalaVersion := "2.13.2",
//    crossScalaVersions := Seq("2.13.2"),
    maxErrors := 5,
    test in assembly := {},
    triggeredMessage := Watched.clearWhenTriggered,
//    scalaVersion in ThisBuild := crossScalaVersions.value.head,
    scalacOptions := stdOptions ++ extraOptions(scalaVersion.value),
//    libraryDependencies ++= compileOnlyDeps ++ testDeps ++ Seq(
    libraryDependencies ++= Seq(
      compilerPlugin("org.spire-math"         % "kind-projector_2.13"  % "0.11.0"),
      compilerPlugin("com.github.tomasmikula" % "pascal_2.13"          % "0.4.0"),
      //compilerPlugin("com.github.ghik"        %% "silencer-plugin" % "1.0")
    ),
    incOptions ~= (_.withLogRecompileOnMacro(false))
)

lazy val projectName = "TruvariReportManager"
lazy val ooVersion = "6.4.3"

lazy val TruvariReportManager = project
  .in(file(projectName))
  .settings(stdSettings(projectName))
  .settings(
    mainClass in assembly := Some("truvarirm.Main"),
    assemblyJarName in assembly := "truvarirm.jar",
    libraryDependencies ++= Seq(
      
      // OpenOffice libraries
      "org.libreoffice" % "unoil" % ooVersion,
      "org.libreoffice" % "juh" % ooVersion,
      "org.libreoffice" % "officebean" % ooVersion,
      // This seems to do the magic ...
      "org.libreoffice" % "ridl" % ooVersion,
        // java.lang.NoClassDefFoundError: com/sun/star/comp/servicemanager/ServiceManager
      "org.libreoffice" % "jurt" % ooVersion,
      "org.libreoffice" % "unoloader" % ooVersion,
      "com.github.jeremysolarz" % "bootstrap-connector" % "1.0.0",
      
      // Scala IO helpers
      "com.lihaoyi" %% "sourcecode" % "0.2.1",
      "com.lihaoyi" %% "pprint" % "0.5.9",
      "com.lihaoyi" %% "os-lib" % "0.7.0",

      // Testing libraries
      "org.specs2" %% "specs2-core"          % "4.9.4" % Test,
      "org.specs2" %% "specs2-scalacheck"    % "4.9.4" % Test,
      "org.specs2" %% "specs2-matcher-extra" % "4.9.4" % Test,
      "org.specs2" %% "specs2-scalaz"        % "4.9.4" % Test
//      "dev.zio" %% "zio-test" % "1.0.0-RC18-2" % Test,
      // Others that could be added if necessary
      //"org.typelevel" %%% "cats-effect-laws" % "1.1.0" % "test",
      // "co.fs2" %% "fs2-io" % "1.0.4",
      // "org.scalaz" %% "scalaz-core" % "7.2.27",
      // "org.typelevel" %% "cats-effect" % "1.2.0",
      // "org.typelevel" %% "cats-mtl-core" % "0.4.0",
      // "org.typelevel" %% "cats-core" % "1.6.0",
      // "com.github.samtools" % "htsjdk" % "2.19.0",
      // "com.chuusai" %% "shapeless" % "2.3.3",
      // "com.github.scopt" %% "scopt" % "4.0.0-RC2",
      // "org.wvlet.airframe" %% "airframe-log" % "0.78",
      // "com.monovore" %% "decline" % "0.5.0",
      // "dev.zio" %% "zio" % "1.0.0-RC11-1",
      // "co.fs2" %% "fs2-core" % "1.0.4"
      // "org.scalatest" %%% "scalatest" % "3.0.5" % "test",
      // "org.scalacheck" %%% "scalacheck" % "1.13.5" % "test"
    ),
    scalacOptions in Test ++= Seq("-Yrangepos")
    //publishLocal in ThisBuild := 
    //  Some(Resolver.file("file", new File("../gflow/scripts/lib/")))
  )
  .settings(
    // In the repl most warnings are useless or worse.
    // This is intentionally := as it's more direct to enumerate the few
    // options we do want than to try to subtract off the ones we don't.
    // One of -Ydelambdafy:inline or -Yrepl-class-based must be given to
    // avoid deadlocking on parallel operations, see
    //   https://issues.scala-lang.org/browse/SI-9076
    scalacOptions in Compile in console := Seq(
      "-Ypartial-unification",
      "-language:higherKinds",
      "-language:existentials",
      "-Yno-adapted-args",
      "-Xsource:2.13",
      "-Yrepl-class-based"
    ),
    initialCommands in Compile in console := """
                                               |import scalaz._
                                               |import scalaz.zio._
                                               |import scalaz.zio.console._
                                               |object replRTS extends RTS {}
                                               |import replRTS._
                                               |implicit class RunSyntax[E, A](io: IO[E, A]){ def unsafeRun: A = replRTS.unsafeRun(io) }
    """.stripMargin
  )
/*  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, isSnapshot),
    buildInfoPackage := "scalaz.zio",
    buildInfoObject := "BuildInfo"
  )*/

lazy val root = project.in(file("."))
  .aggregate(TruvariReportManager)
