resolvers ++= Seq(
  "Artima Maven Repository" at "http://repo.artima.com/releases"
)

organization := "com.fluency03"

name := "blockchain-scala"

version := "0.0.2"

scalaVersion := "2.12.0"

val scalazVersion = "7.2.21"
val akkaHttpVersion = "10.1.1"
val akkaVersion = "2.5.11"
val akkaJson4sVersion = "1.20.1"
val json4sVersion = "3.5.3"
val scalaTestVersion = "3.0.5"
val scalaMockVersion = "4.1.0"

val httpDependencies = Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "de.heikoseeberger" %% "akka-http-json4s" % akkaJson4sVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion % Test
)

val testDependencies = Seq(
  "org.scalactic" %% "scalactic" % scalaTestVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalamock" %% "scalamock" % scalaMockVersion % Test
)

libraryDependencies ++= {
  Seq(
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "org.json4s" %% "json4s-native" % json4sVersion
  )
} ++ httpDependencies ++ testDependencies
