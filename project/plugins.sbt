resolvers ++= Seq(
  "Artima Maven Repository" at "http://repo.artima.com/releases"
)

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.artima.supersafe" %% "sbtplugin" % "1.1.3")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
