name := """finagle-fun"""

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Maven central" at "http://repo1.maven.org/maven2/"
resolvers += "Twitter Repository" at "http://maven.twttr.com"

libraryDependencies += "com.twitter" % "finagle-http_2.11" % "6.39.0"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
