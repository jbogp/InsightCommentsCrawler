package main.scala

import main.scala.kafka.KafkaConsumer
import hbase.WriteToHbase
import hbase.ReadFromHbase
import net.liftweb.json.Serialization
import net.liftweb.json.NoTypeHints

object TopicsFinder {
  
  	val hbr = new ReadFromHbase
	val hbw = new WriteToHbase

	implicit val formats = Serialization.formats(NoTypeHints)
	
	def r {
  			//val kafkaCon = new KafkaConsumer
  	}

}