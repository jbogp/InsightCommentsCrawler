name := "InsightCommentsCrawler"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
	 "mysql" % "mysql-connector-java" % "5.1.18",
	  "com.typesafe.slick" %% "slick" % "1.0.0",
	  "org.apache.kafka" % "kafka_2.10" % "0.8.2-beta",
	  "ly.stealth" % "scala-kafka" % "0.1.0.0",
	  "org.apache.avro" % "avro" % "1.4.1"
)