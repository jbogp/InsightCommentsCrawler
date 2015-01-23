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
	  hbaseConfiguration("/opt/cloudera/parcels/CDH-5.3.0-1.cdh5.3.0.p0.30/etc/hbase/conf.dist/", "article_links")
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
		  .map(result => result.getColumnCells("contents".getBytes(), "description".getBytes()))
		
		val wordCounts = tokenized.map(cell => (new String(cell.get(0).getValue()), 1)).reduceByKey(_ + _)
		
		// filter out words with less than threshold occurrences
		val filtered = wordCounts.filter((tuple) =>{
		  (!dictionnary.contains(" "+tuple._1+" ")) && tuple._2 >1
		})
		
		println(filtered.collect.reduceLeft((s,i) => (s._1 +" "+ i._1,1)))
		
		spark.stop
		
  	}

}