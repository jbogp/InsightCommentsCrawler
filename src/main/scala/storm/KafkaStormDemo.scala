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

class KafkaStorm(kafkaZkConnect: String, topic: String, numTopicPartitions: Int = 1,topologyName: String = "kafka-storm-starter") {

  def runTopology() {
    val zkHosts = new ZkHosts(kafkaZkConnect)
    val topic = "tweets"
    val zkRoot = "/"
    // The spout appends this id to zkRoot when composing its ZooKeeper path.  You don't need a leading `/`.
    val zkSpoutId = "kafka-storm-starter"
    val kafkaConfig = new SpoutConfig(zkHosts, topic, zkRoot, zkSpoutId)
    val kafkaSpout = new KafkaSpout(kafkaConfig)
    val numSpoutExecutors = numTopicPartitions
    val builder = new TopologyBuilder
    val spoutId = "kafka-spout"
    //builder.setSpout(spoutId, kafkaSpout, numSpoutExecutors)

    // Showcases how to customize the topology configuration
    val topologyConfiguration = {
      val c = new Config
      c.put(Config.NIMBUS_HOST, "ec2-54-67-119-111.us-west-1.compute.amazonaws.com");
      c.put(Config.NIMBUS_THRIFT_PORT,6627:Integer);
      c.setDebug(false)
      c.setNumWorkers(4)
      c.setMaxSpoutPending(1000)
      c.setMessageTimeoutSecs(60)
      c.setNumAckers(0)
      c.setMaxTaskParallelism(50)
      c.put(Config.TOPOLOGY_EXECUTOR_RECEIVE_BUFFER_SIZE, 16384: Integer)
      c.put(Config.TOPOLOGY_EXECUTOR_SEND_BUFFER_SIZE, 16384: Integer)
      c.put(Config.TOPOLOGY_RECEIVER_BUFFER_SIZE, 8: Integer)
      c.put(Config.TOPOLOGY_TRANSFER_BUFFER_SIZE, 32: Integer)
      c.put(Config.TOPOLOGY_STATS_SAMPLE_RATE, 0.05: java.lang.Double)
      c
    }
    
    System.setProperty("storm.jar","/home/ubuntu/scala/InsightCommentsCrawler/target/scala-2.10/something.jar")
    

    builder.setSpout("word", new TestWordSpout(), 10)
    builder.setBolt("exclaim", new ExclamationBolt(), 3).shuffleGrouping("word")

    // Now run the topology in a local, in-memory Storm cluster
    StormSubmitter.submitTopology(topologyName, topologyConfiguration, builder.createTopology())
  }

}



class ExclamationBolt extends BaseRichBolt {
  var collector: OutputCollector = _

  override def prepare(config: JMap[_, _], context: TopologyContext, collector: OutputCollector) {
    this.collector = collector
  }

  override def execute(tuple: Tuple) {
    this.collector.emit(tuple, new Values(Exclaimer.exclaim(tuple.getString(0))))
    this.collector.ack(tuple)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer) {
    declarer.declare(new Fields("word"))
  }
}


object Exclaimer {
  def exclaim(s: String): String = {
    s + "!!!"
  }
}





