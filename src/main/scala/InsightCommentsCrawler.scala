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


/*
 * Main function, start the program, the input argument 
 * will tell us what part of the pipeline to start
 */
object InsightCommentsCrawler {

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
									case true => kafkaProducer.send(item.link, "1")
									case false => println("Skipping link, already registered")
								}
							})
							Thread.sleep(300000);
						}
					}
					
					/*
					 * Reading the article_links from Kafka and fetching the comments
					 * TODO? Also input them in a queue to batch process on them as well
					 */
					case "CommentsFetcher" => {
						val consumer = new KafkaConsumer("article_links","CommentsFetcher",args(1),true)
						
						consumer.read(println)
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