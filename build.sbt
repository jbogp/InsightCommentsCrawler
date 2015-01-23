name := "InsightCommentsCrawler"

version := "1.0"

scalaVersion := "2.10.4"


resolvers ++= Seq(
  "Hadoop Releases" at "https://repository.cloudera.com/content/repositories/releases/",
  "Cloudera" at "https://repository.cloudera.com/artifactory/public/",
  "Cloudera2" at "http://repository.cloudera.com/cloudera/cloudera-repos/",
  "releases" at "http://oss.sonatype.org/content/repositories/releases"
)
 
libraryDependencies ++= Seq(
	"com.google.guava" % "guava" % "12.0",
	"org.apache.kafka" % "kafka_2.10" % "0.8.2-beta",
    "org.apache.hadoop" % "hadoop-core" % "2.5.0-mr1-cdh5.3.0" % "provided", 
    "org.apache.hadoop" % "hadoop-common" % "2.5.0-cdh5.3.0" % "provided",
  	"org.apache.hadoop" % "hadoop-client" % "2.5.0-mr1-cdh5.3.0" % "provided",
    "org.apache.hbase" % "hbase-client" % "0.98.6-cdh5.3.0",
    "org.apache.hbase" % "hbase-protocol" % "0.98.6-cdh5.3.0",
    "org.apache.hbase" % "hbase-common" % "0.98.6-cdh5.3.0",
    "org.apache.hbase" % "hbase-server" % "0.98.6-cdh5.3.0",
    "org.apache.spark" % "spark-core_2.10" % "1.2.0-cdh5.3.0" % "provided",
    "org.apache.spark" % "spark-streaming_2.10" % "1.2.0-cdh5.3.0" % "provided",
    "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.2.0-cdh5.3.0" % "provided",
    "net.liftweb" %% "lift-json" % "2.5"
)

ivyXML := 
  <dependencies>
    <exclude org="log4j" name="log4j" />
    <exclude org="commons-logging" name="commons-logging" />
    <exclude org="org.slf4j" name="slf4j-log4j12" />
  </dependencies>
