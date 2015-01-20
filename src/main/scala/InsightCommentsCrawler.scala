package main.scala

import java.sql.DriverManager
import java.sql.Connection
import main.scala.rss._
import java.net.URL
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Try, Success, Failure}
import kafka.consumer.KafkaConsumer
import kafka.producer.KafkaProducer
import hbase.KafkaToHbase


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
	
						/*Reading the subscription file and iterating on feeds*/
						for {
							feedInfo <- Utils.getUrls("subscriptions.xml")
						} subreader.read(feedInfo)
						
						/*Sending messages to Kafka article_links queue*/
						val kafkaProducer = new KafkaProducer("links",args(1))
						subreader.itemArray.foreach( item => {
							println(item.link)
							kafkaProducer.send(item.link, "1")
						})
					}
					
					/*
					 * Reading the article_links from Kafka and fetching the comments
					 * TODO? Also input them in a queue to batch process on them as well
					 */
					case "CommentsFetcher" => {
						val consumer = new KafkaConsumer("links","CommentsFetcher",args(1),true)
						val hbaseconnect = new KafkaToHbase
						consumer.read(msg => println(new String(msg)))
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