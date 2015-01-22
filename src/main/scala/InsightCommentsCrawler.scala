package main.scala

import java.sql.DriverManager
import java.sql.Connection
import main.scala.rss._
import java.net.URL
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Try, Success, Failure}
import hbase.WriteToHbase
import kafka.KafkaProducer
import kafka.KafkaConsumer
import java.util.Calendar
import externalAPIs.FBAPI
import externalAPIs.DisqusAPI
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import main.scala.hbase.ReadFromHbase



/*
 * Main function, start the program, the input argument 
 * will tell us what part of the pipeline to start
 */
object InsightCommentsCrawler {
  
implicit val formats = Serialization.formats(NoTypeHints)

	/*Defining the main function*/
	def main(args : Array[String]):Unit = {
			/*we need at least an argument otherwise, throw an exception*/
			try {
				args(0) match {
					/*Fetching the article URLs from RSS and send them to Kafka*/
					case "RssCrawler" => {
						
						/*Creating the RSS reader object*/
						val subreader = new RssReader
						
						/*Kafka and Hbase connectors*/
						val kafkaProducer = new KafkaProducer("article_links",args(1))
						val hbaseconnect = new WriteToHbase
						
						
						while(true) {
							/*Reading the subscription file and iterating on feeds*/
							for {
								feedInfo <- Utils.getUrls("subscriptions.xml")
							} subreader.read(feedInfo)
							
							/*Sending messages to Kafka article_links queue*/

							subreader.itemArray.foreach( item => {
								println(item.link)
								var values = Array(item.link,item.engine,item.engineId,item.desc,item.title)
								/*If successfully inserted in Hbase (new Item) send to Kafka*/ 
								hbaseconnect.insertURL(values) match {
									case true => {
										kafkaProducer.send(write(Extraction.decompose(KafkaMessageURL(item.link,item.engine,item.engineId))), "1")
									}
									case false => println("Skipping link, already registered")
								}
							})
							Thread.sleep(300000);
						}
					}
					
					/*
					 * Reading the article_links from Hbase and fetching the comments
					 */
					case "CommentsFetcher" => {
					  		val hbr = new ReadFromHbase
					  		val items = hbr.readTimeFilterLinks("article_links",1)
					  		items.foreach(item => {
					  		  println(item.url)
					  		})
					  		/*
							val fbReader = new DisqusAPI
							val json = fbReader.fetchJSONFromURL(Array("link:http://www.telegraph.co.uk/news/science/science-news/11348010/Close-your-eyes-if-you-want-to-find-your-car-keys.html","telegraphuk"))
							val comments = fbReader.readJSON(json)*/
					}
					
					/*test*/
					case "TestJSON" => {

						
					}
					
					case _ => {
					  println("Sorry, did not understand this command")
					  exit(0)
					}
				}
			}
			catch {
				case e: Exception => {
					e.printStackTrace
					System.exit(1)
				}
			}



			//system.shutdown()
	}
}