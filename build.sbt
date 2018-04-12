resolvers ++= Seq(
  "Artima Maven Repository" at "http://repo.artima.com/releases"
)

organization := "com.fluency03"

name := "blockchain-scala"

version := "0.0.1"

scalaVersion := "2.12.0"

val scalazVersion = "7.2.21"
val finagleVersion = "0.17.0"
val json4sVersion = "3.5.3"
val scalaTestVersion = "3.0.5"

libraryDependencies ++= {
  Seq(
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "org.json4s" %% "json4s-native" % json4sVersion,
    "com.github.finagle" %% "finch-core" % finagleVersion,
    "com.github.finagle" %% "finch-json4s" % finagleVersion,
    "org.scalactic" %% "scalactic" % scalaTestVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  )
}
