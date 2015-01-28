package main.scala.storm

import java.util.Properties
import backtype.storm.generated.KillOptions
import backtype.storm.topology.TopologyBuilder
import backtype.storm.{Config, LocalCluster}
import kafka.admin.AdminUtils
import kafka.utils.ZKStringSerializer
import org.I0Itec.zkclient.ZkClient
import storm.kafka.{KafkaSpout, SpoutConfig, ZkHosts}
import scala.concurrent.duration._
import backtype.storm.StormSubmitter
import backtype.storm.task.{ OutputCollector, TopologyContext }
import backtype.storm.topology.base.BaseRichBolt
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.tuple.{ Fields, Tuple, Values }
import java.util.{ Map => JMap }
import backtype.storm.testing.TestWordSpout
import main.scala.hbase.WriteToHbase
import main.scala.hbase.ReadFromHbase
import main.scala.sql.MySQLConnector
import net.liftweb.json._
import externalAPIs.Tweet
import main.scala.kafka.KafkaConsumer
import main.scala.kafka.KafkaProducer

class KafkaStorm(kafkaZkConnect: String, topic: String, numTopicPartitions: Int = 1,topologyName: String = "kafka-storm-starter") {

  
  
def runTopology() {
    val zkHosts = new ZkHosts(kafkaZkConnect)
    val zkRoot = ""
    // The spout appends this id to zkRoot when composing its ZooKeeper path.  You don't need a leading `/`.
    val zkSpoutId = "kafka-storm-starter"
    val kafkaConfig = new SpoutConfig(zkHosts, topic, zkRoot, zkSpoutId)
    val kafkaSpout = new KafkaSpout(kafkaConfig)
    val numSpoutExecutors = numTopicPartitions
    val builder = new TopologyBuilder
    val spoutId = "kafka-spout"
    

    // Showcases how to customize the topology configuration
    val topologyConfiguration = {
      val c = new Config
      c.put(Config.NIMBUS_HOST, "ec2-54-67-119-111.us-west-1.compute.amazonaws.com");
      c.put(Config.NIMBUS_THRIFT_PORT,6627:Integer);
      c.setDebug(true)
      c.setNumWorkers(4)
      c
    }
    
    System.setProperty("storm.jar","/home/ubuntu/scala/InsightCommentsCrawler/target/scala-2.10/something.jar")
    System.getProperties().list(System.out)
    builder.setSpout(spoutId, kafkaSpout, numSpoutExecutors)
    builder.setBolt("filterTweets", new FilteringBolt(), 1).shuffleGrouping(spoutId)

    // Now run the topology
    StormSubmitter.submitTopology(topologyName, topologyConfiguration, builder.createTopology())
  }

}



class FilteringBolt extends BaseRichBolt {
  var collector: OutputCollector = _

  override def prepare(config: JMap[_, _], context: TopologyContext, collector: OutputCollector) {
    this.collector = collector
  }

  override def execute(tuple: Tuple) {
    //this.collector.emit(tuple, new Values(TweetsFilter.filter(tuple.getString(0))))
    TweetsFilter.filter(new String(tuple.getBinary(0)))
    this.collector.ack(tuple)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer) {
    //declarer.declare(new Fields("word"))
  }
}


object TweetsFilter {
	val hbr = new WriteToHbase
	val rhb = new ReadFromHbase
	implicit val formats = Serialization.formats(NoTypeHints)
	val kcPro = new KafkaProducer("test","ec2-54-67-99-96.us-west-1.compute.amazonaws.com:9092")
	
	def filter(s: String): Unit = {
		val timestamp = MySQLConnector.getLastTimestamp
		/*Getting the topics*/
		val topics = rhb.readTrendsComments("topcics1h", "val", timestamp)++
		rhb.readTrendsComments("topcics12h", "val", timestamp)++
		rhb.readTrendsComments("topcicsalltime", "val", timestamp)
		.distinct
		s match {
				/*We don't want retweets, links or replies*/
		    	case x if(!x.contains("RT") && !x.contains("http://")) && !x.startsWith("@") => {
		    		hbr.insertTweets(parse(x).extract[Tweet], topics.toArray)
		    		kcPro.send(x)
		    	}
		    	case _ =>
		  }
	}
}





