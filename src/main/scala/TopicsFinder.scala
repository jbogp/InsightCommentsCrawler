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

object TopicsFinder {
  
  	val hbr = new ReadFromHbase
	val hbw = new WriteToHbase
	
	val conf = new SparkConf().setAppName("Spark Topics").setMaster("local")
    val spark = new SparkContext(conf)

	
	val dictionnary = Source.fromFile("common_words") mkString
	
							
  		
	def getKeywords(corpus:ArrayBuffer[ArticleMeta])= {
  		/*Getting all infos together*/
  		
  		val all = corpus.map(article => article.desc+" "+article.title).reduceLeft(_ + " " +_)
		/*Cleaning*/
		val corpusStriped = all.replaceAll("[^a-zA-Z ]", "")
		
		val tokenized = spark.textFile(corpusStriped).flatMap(_.split(" "))
		
		val wordCounts = tokenized.map((_, 1)).reduceByKey(_ + _)
		
		// filter out words with less than threshold occurrences
		val filtered = wordCounts.filter((tuple) =>{
		  !dictionnary.contains(tuple._1)
		})
		
		println(filtered.collect)
		
  	}

}