name := "CITelegramBot"

scalaVersion := "3.1.0"

val CatsVersion = "3.1.1"
val Http4sVersion = "0.23.3"
val CirceVersion = "0.14.1"
val SttpVersion = "3.3.14"
val Log4CatsVersion = "2.1.1"
val DoobieVersion = "1.0.0-RC1"
val CirisVersion = "2.2.0"

maintainer := "VladKopanev"

libraryDependencies ++= Seq(
  "com.bot4s"     %% "telegram-core"       % "5.1.0",
  "org.tpolecat"  %% "doobie-core"         % DoobieVersion,
  "org.tpolecat"  %% "doobie-postgres"     % DoobieVersion,
  "org.tpolecat"  %% "doobie-hikari"       % DoobieVersion,
  "org.http4s"    %% "http4s-dsl"          % Http4sVersion,
  "org.http4s"    %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"    %% "http4s-circe"        % Http4sVersion,
  "org.typelevel" %% "cats-effect"         % CatsVersion,
  "org.typelevel" %% "log4cats-slf4j"      % Log4CatsVersion,
  "is.cir"        %% "ciris"               % CirisVersion,
  "is.cir"        %% "ciris-refined"       % CirisVersion,
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % SttpVersion
).map(_.cross(CrossVersion.for3Use2_13))

libraryDependencies ++= Seq(
  "com.davegurnell" %% "unindent"       % "1.7.0",
  "eu.timepit"      %% "refined"        % "0.9.27"
)

enablePlugins(JavaAppPackaging)
