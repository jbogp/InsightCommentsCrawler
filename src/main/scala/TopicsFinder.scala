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


object TopicsFinder {
  
  	val hbr = new ReadFromHbase
	val hbw = new WriteToHbase
	
	val conf = new SparkConf().setAppName("Spark Topics").setMaster("local")
    val spark = new SparkContext(conf)
  	
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
	
	val dictionnary = Source.fromFile("common_words") mkString
	
							
  		
	def getKeywords(corpus:ArrayBuffer[ArticleMeta])= {
  		/*Getting all infos together*/
  		
  		val all = corpus.map(article => article.title).reduceLeft(_ + " " +_)
		/*Cleaning*/
		val corpusStriped = all.replaceAll("[^a-zA-Z ]", "").toLowerCase()
		
		//val tokenized = spark.parallelize(corpusStriped :: Nil).flatMap(_.split(" "))
		
		
		val tokenized = rdd
		  .map(tuple => tuple._2)
		  
		val test =  tokenized.map[String](r => {
			  		if(r.containsColumn("contents".getBytes(), "description".getBytes())){
			  			new String(r.getColumnLatest("contents".getBytes(), "description".getBytes()).getValue())
			  		}
			  		else {
			  			""
			  		}
		  })
		  
		val wordCounts = test
		  .flatMap(_.replaceAll("[^a-zA-Z ]", "").toLowerCase().split(" "))  
		  .map((_,1)).reduceByKey(_ + _)
		
		
		// filter out words with less than threshold occurrences
		val filtered = wordCounts.filter((tuple) =>{
		  (!dictionnary.contains(" "+tuple._1+" ")) && tuple._2 >3 && tuple._1.length()<15
		})
		
		println(filtered.collect.sortBy(r=>r._2).takeRight(10).reduceLeft((s,i) => ("("+s._1+","+s._2+"),("+ i._1+","+i._2+")",1)))
		//tokenized.collect.foreach(res => println(new String(res)))
		
		spark.stop
		
  	}

}