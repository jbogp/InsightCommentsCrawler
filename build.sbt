name := "InsightCommentsCrawler"

version := "1.0"

scalaVersion := "2.10.4"


resolvers ++= Seq(
  "Hadoop Releases" at "https://repository.cloudera.com/content/repositories/releases/",
  "Cloudera" at "https://repository.cloudera.com/artifactory/public/",
  "Cloudera2" at "http://repository.cloudera.com/cloudera/cloudera-repos/",
  "releases" at "http://oss.sonatype.org/content/repositories/releases",
  "typesafe-repository" at "http://repo.typesafe.com/typesafe/releases/",
  "clojars-repository" at "https://clojars.org/repo"
)
 
  val excludeJBossNetty = ExclusionRule(organization = "org.jboss.netty")
  val excludeIONetty = ExclusionRule(organization = "io.netty")
  val excludeEclipseJetty = ExclusionRule(organization = "org.eclipse.jetty")
  val excludeMortbayJetty = ExclusionRule(organization = "org.mortbay.jetty")
  val excludeAsm = ExclusionRule(organization = "org.ow2.asm")
  val excludeOldAsm = ExclusionRule(organization = "asm")
  val excludeCommonsLogging = ExclusionRule(organization = "commons-logging")
  val excludeSLF4J = ExclusionRule(organization = "org.slf4j")
  val excludeScalap = ExclusionRule(organization = "org.scala-lang", artifact = "scalap")
  val excludeHadoop = ExclusionRule(organization = "org.apache.hadoop")
  val excludeCurator = ExclusionRule(organization = "org.apache.curator")
  val excludePowermock = ExclusionRule(organization = "org.powermock")
  val excludeFastutil = ExclusionRule(organization = "it.unimi.dsi")
  val excludeJruby = ExclusionRule(organization = "org.jruby")
  val excludeThrift = ExclusionRule(organization = "org.apache.thrift")
  val excludeServletApi = ExclusionRule(organization = "javax.servlet")
  val excludeJUnit = ExclusionRule(organization = "junit")

libraryDependencies ++= Seq(
	"org.apache.kafka" % "kafka_2.10" % "0.8.2-beta" excludeAll(excludeJBossNetty, excludeMortbayJetty, excludeAsm, excludeCommonsLogging, excludeOldAsm, excludeServletApi, excludeEclipseJetty,excludeIONetty,excludeJBossNetty),
    "org.apache.hadoop" % "hadoop-core" % "2.5.0-mr1-cdh5.3.0" excludeAll(excludeJBossNetty, excludeMortbayJetty, excludeAsm, excludeCommonsLogging, excludeSLF4J, excludeOldAsm, excludeServletApi,excludeEclipseJetty,excludeIONetty,excludeJBossNetty), 
    "org.apache.hadoop" % "hadoop-common" % "2.5.0-cdh5.3.0" excludeAll(excludeJBossNetty, excludeMortbayJetty, excludeAsm, excludeCommonsLogging, excludeSLF4J, excludeOldAsm, excludeServletApi,excludeEclipseJetty,excludeIONetty,excludeJBossNetty),
  	"org.apache.hadoop" % "hadoop-client" % "2.5.0-mr1-cdh5.3.0" excludeAll(excludeJBossNetty, excludeMortbayJetty, excludeAsm, excludeCommonsLogging, excludeSLF4J, excludeOldAsm, excludeServletApi,excludeEclipseJetty,excludeIONetty,excludeJBossNetty),
    "org.apache.hbase" % "hbase-client" % "0.98.6-cdh5.3.0" excludeAll(excludeJBossNetty, excludeMortbayJetty, excludeAsm, excludeCommonsLogging, excludeOldAsm, excludeServletApi,excludeEclipseJetty,excludeIONetty,excludeJBossNetty),
    "org.apache.hbase" % "hbase-protocol" % "0.98.6-cdh5.3.0" excludeAll(excludeJBossNetty, excludeMortbayJetty, excludeAsm, excludeCommonsLogging, excludeOldAsm, excludeServletApi,excludeEclipseJetty,excludeIONetty,excludeJBossNetty),
    "org.apache.hbase" % "hbase-common" % "0.98.6-cdh5.3.0" excludeAll(excludeJBossNetty, excludeMortbayJetty, excludeAsm, excludeCommonsLogging, excludeOldAsm, excludeServletApi,excludeEclipseJetty,excludeIONetty,excludeJBossNetty),
    "org.apache.hbase" % "hbase-server" % "0.98.6-cdh5.3.0" excludeAll(excludeJBossNetty, excludeMortbayJetty, excludeAsm, excludeCommonsLogging, excludeOldAsm, excludeServletApi,excludeEclipseJetty,excludeIONetty,excludeJBossNetty),
    "org.apache.spark" % "spark-core_2.10" % "1.2.0-cdh5.3.0" excludeAll(excludeServletApi,excludeJBossNetty),
    "net.liftweb" %% "lift-json" % "2.5" excludeAll(excludeJBossNetty, excludeMortbayJetty, excludeAsm, excludeCommonsLogging, excludeOldAsm, excludeServletApi,excludeEclipseJetty,excludeIONetty,excludeJBossNetty),
    "org.apache.storm" % "storm-core" % "0.9.3" % "provided" 
    exclude("org.apache.zookeeper", "zookeeper")
    exclude("org.slf4j", "log4j-over-slf4j"),
    "org.apache.storm" % "storm-kafka" % "0.9.3"
    exclude("org.apache.zookeeper", "zookeeper"),
    "org.twitter4j" % "twitter4j-stream" % "4.0.2",
    "mysql" % "mysql-connector-java" % "5.1.34",
    "org.slf4j" % "log4j-over-slf4j" % "1.7.7"
)

assemblyMergeStrategy in assembly := {
    case PathList(ps @ _*) if ps.last endsWith ".RSA" => MergeStrategy.first
    case PathList("META-INF", "mailcap") => MergeStrategy.first
    case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
    case "application.conf"                            => MergeStrategy.concat
    case "unwanted.txt"                                => MergeStrategy.discard
    case PathList("META-INF", "maven","io.netty","netty", xs @ _* ) => MergeStrategy.first
    case PathList("org", "jboss","netty", xs @ _* ) => MergeStrategy.first
    case PathList("META-INF", "maven","org.apache.curator","curator-client", xs @ _* ) => MergeStrategy.first
    case PathList("META-INF", "maven","org.apache.curator","curator-framework", xs @ _* ) => MergeStrategy.first
    case PathList("META-INF", "maven","org.apache.httpcomponents","httpclient", xs @ _* ) => MergeStrategy.first
    case PathList("META-INF", "maven","org.apache.httpcomponents","httpcore", xs @ _* ) => MergeStrategy.first
    case PathList("META-INF", "mimetypes.default") => MergeStrategy.first
    case PathList("com", "esotericsoftware","minlog","Log$Logger.class") => MergeStrategy.first
    case PathList("com", "esotericsoftware","minlog","Log.class") => MergeStrategy.first
    case PathList("com", "google","common","base", xs @ _*) => MergeStrategy.first
    case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
    case PathList("javax", "activation", xs @ _*)         => MergeStrategy.first
    case PathList("org", "apache","commons","beanutils", xs @ _*)         => MergeStrategy.first
    case PathList("org", "apache","commons","collections", xs @ _*)         => MergeStrategy.first
    case PathList("org", "apache","commons","logging", xs @ _*)         => MergeStrategy.first
    case PathList("org", "apache","jute", xs @ _*)         => MergeStrategy.first
    case PathList("org", "slf4j","impl", xs @ _*)         => MergeStrategy.first
    case x if x.startsWith("plugin.properties") => MergeStrategy.last
    case x if x.startsWith("project.clj") => MergeStrategy.last
    case x =>
       val oldStrategy = (assemblyMergeStrategy in assembly).value
       oldStrategy(x)
}

