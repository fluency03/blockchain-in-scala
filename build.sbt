resolvers ++= Seq(
  "Artima Maven Repository" at "http://repo.artima.com/releases"
)

organization := "com.fluency03"

name := "blockchain-scala"

version := "0.0.1"

scalaVersion := "2.12.5"

libraryDependencies ++= {
  Seq(
    "org.scalaz" %% "scalaz-core" % "7.2.21",
    "org.scalactic" %% "scalactic" % "3.0.5",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test"
  )
}