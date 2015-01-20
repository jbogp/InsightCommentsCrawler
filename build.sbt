name := "InsightCommentsCrawler"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
	 "mysql" % "mysql-connector-java" % "5.1.18",
	  "org.apache.kafka" % "kafka_2.10" % "0.8.2-beta"
)