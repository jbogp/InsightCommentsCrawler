name := "InsightCommentsCrawler"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Apache HBase" at "https://repository.apache.org/content/repositories/releases"

resolvers += "Thrift" at "http://people.apache.org/~rawson/repo/"

libraryDependencies ++= Seq(
	"mysql" % "mysql-connector-java" % "5.1.18",
	"org.apache.kafka" % "kafka_2.10" % "0.8.2-beta",
	"org.apache.spark" % "spark-streaming-kafka_2.10" % "1.2.0",
	"org.apache.spark" % "spark-core_2.10" % "1.2.0",
	"org.apache.spark" % "spark-streaming_2.10" % "1.2.0",
    "org.apache.hadoop" % "hadoop-core" % "0.20.2",
    "org.apache.hbase" % "hbase" % "0.90.4"
)