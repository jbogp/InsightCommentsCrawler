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
import externalAPIs.Tweet
import main.scala.kafka.KafkaConsumer
import main.scala.kafka.KafkaProducer
import java.util.Calendar

class KafkaStorm(kafkaZkConnect: String, topic: String, numTopicPartitions: Int = 4,topologyName: String = "kafka-storm-starter") {

  
  
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
      c.setNumWorkers(10)
      c.setMaxSpoutPending(1000)
      c.setMessageTimeoutSecs(120)
      c.setMaxTaskParallelism(50)
      c.put(Config.TOPOLOGY_EXECUTOR_RECEIVE_BUFFER_SIZE, 16384: Integer)
      c.put(Config.TOPOLOGY_EXECUTOR_SEND_BUFFER_SIZE, 16384: Integer)
      c.put(Config.TOPOLOGY_RECEIVER_BUFFER_SIZE, 8: Integer)
      c.put(Config.TOPOLOGY_TRANSFER_BUFFER_SIZE, 32: Integer)
      c.put(Config.TOPOLOGY_STATS_SAMPLE_RATE, 0.05: java.lang.Double)
      c
    }
    
    System.getProperties().list(System.out)
    builder.setSpout(spoutId, kafkaSpout, numSpoutExecutors)
    builder.setBolt("filterTweets", new FilteringBolt(), 4).shuffleGrouping(spoutId)

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
    
    TweetsFilter.filter(new String(tuple.getValueByField("bytes").asInstanceOf[Array[Byte]]))
    this.collector.ack(tuple)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer) {
    //declarer.declare(new Fields("word"))
  }
}


object TweetsFilter {
	val hbr = new WriteToHbase
	val rhb = new ReadFromHbase
	implicit val formats = net.liftweb.json.Serialization.formats(net.liftweb.json.NoTypeHints)
	
	def filter(s: String): Unit = {
		/*Getting the topics*/
		val back = Calendar.getInstance().getTimeInMillis() - 20*60000L
		val topics = rhb.readTrendsComments("topics1h", "val", back)++
		rhb.readTrendsComments("topics12h", "val", back)++
		rhb.readTrendsComments("topicsalltime", "val", back)
		.distinct
		val tweet = net.liftweb.json.parse(s).extract[Tweet]
		tweet.message match {
				/*We don't want retweets, links or replies*/
		    	case x if(!x.contains("RT") && !x.contains("http://") && !x.startsWith("@")) => {
		    		hbr.insertTweets(tweet, topics.toArray)
		    	}
		    	case _ =>
		  }
	}
}





