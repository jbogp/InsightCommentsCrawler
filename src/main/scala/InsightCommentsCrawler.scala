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
import main.scala.hbase.WriteToHbase



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
					  		val hbw = new WriteToHbase
					  		val items = hbr.readTimeFilterLinks("article_links",1)
					  		val dReader = new DisqusAPI
					  		val fbReader = new FBAPI
					  		while(true){
						  		try {
							  		items.foreach(item => {
							  			item.engine match {
							  			  	case "disqus" => {
							  			  		println("getting from disqus")
							  			  		val json = dReader.fetchJSONFromURL(Array(item.url,item.engineId))
							  			  		val comments = dReader.readJSON(json)
							  			  		val jsonString = write(comments)
							  			  		hbw.insertComments(Array(item.url,jsonString))
							  			  	}
							  			  	case "fb" => {
							  			  		println("getting from fb")
							  			  		val json = fbReader.fetchJSONFromURL(Array(item.url,null))
							  			  		val comments = fbReader.readJSON(json)
							  			  		val jsonString = write(comments)
							  			  		hbw.insertComments(Array(item.url,jsonString))
							  			  	}
							  			  	case _ => println("error")
							  			}
							  			/*waiting to avoid scaring off the APIS*/
							  			Thread.sleep(5000);
							  		})
						  		}
						  		catch {
									case e: Exception => {
										println("Error fetching comments, probably busted API limits...waiting some more")
										e.printStackTrace
									}
								}
						  		/*waiting 20 minutes*/
						  		Thread.sleep(600000);
					  		}

							

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