package sparkStreaming

import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming._
import org.apache.spark.SparkConf

object SparkLinksStream {
  
	val Array(zkQuorum, group, topics, numThreads) = Array("test","spark_streaming")
    val sparkConf = new SparkConf().setAppName("KafkaWordCount")
    val ssc =  new StreamingContext(sparkConf, Seconds(2))
    ssc.checkpoint("checkpoint")
	
	//val kafkaStream = KafkaUtils.createStream(streamingContext, [zookeeperQuorum], [group id of the consumer], [per-topic number of Kafka partitions to consume])

}