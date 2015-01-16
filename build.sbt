name := "InsightCommentsCrawler"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
	 "mysql" % "mysql-connector-java" % "5.1.18",
	  "com.typesafe.slick" %% "slick" % "1.0.0",
	  "com.typesafe.akka" %% "akka-actor" % "2.1.4"
)