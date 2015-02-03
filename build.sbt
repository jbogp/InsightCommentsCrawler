name := "InsightWebApp"

organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.10.4"

resolvers ++= Seq(
  "Hadoop Releases" at "https://repository.cloudera.com/content/repositories/releases/",
  "Cloudera" at "https://repository.cloudera.com/artifactory/public/",
  "Cloudera2" at "http://repository.cloudera.com/cloudera/cloudera-repos/",
  "releases" at "http://oss.sonatype.org/content/repositories/releases",
  "spra" at "http://repo.spray.io/"
)

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.2.4"
  val sprayV = "1.2.2"
  Seq(
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %   "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.7" % "test",
    "org.apache.hbase" % "hbase-client" % "0.98.6-cdh5.3.0",
    "org.apache.hbase" % "hbase-protocol" % "0.98.6-cdh5.3.0",
    "org.apache.hbase" % "hbase-common" % "0.98.6-cdh5.3.0",
    "org.apache.hbase" % "hbase-server" % "0.98.6-cdh5.3.0",
    "org.apache.hadoop" % "hadoop-core" % "2.5.0-mr1-cdh5.3.0",
    "org.apache.hadoop" % "hadoop-common" % "2.5.0-cdh5.3.0",
    "org.apache.hadoop" % "hadoop-client" % "2.5.0-mr1-cdh5.3.0",
    "net.liftweb" % "lift-json-ext_2.10" % "2.5.1",
    "mysql" % "mysql-connector-java" % "5.1.34"
  )
}

Revolver.settings
