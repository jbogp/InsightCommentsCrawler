package main.scala

import main.scala.kafka.KafkaConsumer
import hbase.WriteToHbase
import hbase.ReadFromHbase
import net.liftweb.json.Serialization
import net.liftweb.json.NoTypeHints
import scala.io.Source
import net.liftweb.json._
import scala.collection.mutable.ArrayBuffer
import org.apache.spark._
import org.apache.spark.SparkContext._
import main.scala.rss.ArticleMeta
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.spark.rdd.NewHadoopRDD
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.client.Result
import scala.collection.JavaConverters._
import org.apache.spark.rdd.RDD


object TopicsFinder {
  
  	val hbr = new ReadFromHbase
	val hbw = new WriteToHbase
	
	val conf = new SparkConf().setAppName("Spark Topics").setMaster("local")
    val spark = new SparkContext(conf)
  	

	
	val dictionnary = Source.fromFile("common_words") mkString
							
  		
	def getKeywords(corpus:ArrayBuffer[ArticleMeta]=null):Array[String]= {
  	  
  		/*Getting all infos together either from Hbase or corpus*/
		val titles = {
			 if(corpus.nonEmpty){
		  		val all = corpus.map(article => article.title).reduceLeft(_ + " " +_)
				/*Cleaning*/
				val corpusStriped = all.replaceAll("[^a-zA-Z ]", "").toLowerCase()
				
				spark.parallelize(corpusStriped :: Nil).flatMap(_.split(" "))
			}
			else{
			  
				/*Creating HBase configuration*/
				val hbaseConfiguration = (hbaseConfigFileName: String, tableName: String) => {
				  val hbaseConfiguration = HBaseConfiguration.create()
				  hbaseConfiguration.addResource(hbaseConfigFileName)
				  hbaseConfiguration.set(TableInputFormat.INPUT_TABLE, tableName)
				  hbaseConfiguration
				 }
				
				val rdd = new NewHadoopRDD(
				  spark,
				  classOf[TableInputFormat],
				  classOf[ImmutableBytesWritable],
				  classOf[Result],
				  hbaseConfiguration("/opt/cloudera/parcels/CDH/lib/hbase/conf/", "article_links")
				)
			
				val tokenized = rdd.map(tuple => tuple._2)
				tokenized.map[String](r => {
					  		if(r.containsColumn("contents".getBytes(), "description".getBytes())){
					  			new String(r.getColumnLatest("contents".getBytes(), "description".getBytes()).getValue())
					  		}
					  		else {
					  			""
					  		}
				  })
			}
		}
		  

		  
		val wordCounts = titles
		  .flatMap(_.replaceAll("[^a-zA-Z ]", "").toLowerCase().split(" "))  
		  .map((_,1)).reduceByKey(_ + _)
		
		
		// filter out words with less than threshold occurrences
		val filtered = wordCounts.filter((tuple) =>{
		  (!dictionnary.contains(" "+tuple._1+" ")) && tuple._1.length()<15
		})
		
		val ret = filtered
		    .collect
		    .sortBy(r=>r._2)
		    .takeRight(100)
		    .reverse
		    //Removing empty string
		    .drop(1)
		    .map(_._1)
		
		spark.stop
		
		ret
		
  	}

}