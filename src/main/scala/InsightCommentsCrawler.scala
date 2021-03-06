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
import main.scala.hbase.ReadFromHbase
import main.scala.hbase.WriteToHbase
import java.security.MessageDigest
import externalAPIs.TwitterStreamingAPI
import externalAPIs.OnTweetPosted
import twitter4j.FilterQuery
import externalAPIs.OnTweetPosted
import main.scala.kafka.KafkaProducer
import externalAPIs.TweetToJSONToKafka
import main.scala.sql.MySQLConnector
import main.scala.kafka.KafkaConsumer
import main.scala.storm.KafkaStorm
import main.scala.spark.TopicsFinder
import main.scalaspark.BatchQueries




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
            
            /*Hbase connector*/
            val hbaseconnect = new WriteToHbase
            
            
            while(true) {
              try{
                /*Reading the subscription file and iterating on feeds*/
                subreader.itemArray.clear
                for {
                  feedInfo <- Utils.getUrls("subscriptions.xml")
                } subreader.read(feedInfo)
              }
              catch{
                case e:Exception => println("error fetching the feed") 
              }
              
              subreader.itemArray.foreach( item => {
                println(item.link)
                var values = Array(item.link,item.engine,item.engineId,item.desc,item.title)
                /*If successfully inserted in Hbase (new Item) send to Kafka*/ 
                hbaseconnect.insertURL(values) match {
                  case true => {
                    println(item.link+item.engine+item.engineId)
                  }
                  case false => println("Skipping link, already registered")
                }
              })
              Thread.sleep(300000);
            }
          }
          
          /*
           * Refreshing the topics by spark map reduce from hbase
           * This happens at different time scales
           * For 1h to 24h links are loaded in memory
           * For the all time, spark goes to fetch the Hbase table out of this system
           */
           
          case "InferTopics" => {
            
           
            /*Hbase reader*/
            val hbr = new ReadFromHbase
            /*Hbase writer*/
            val hbw = new WriteToHbase
            
            /*kafka connector*/
            def getKafkaProducer(kafkaTop:String):KafkaProducer ={
              new KafkaProducer(kafkaTop,args(1))
            } 
           
            
            def writeTopicsHbase(table:String,topics:Array[String]) {
              topics.zipWithIndex.foreach(topicWithIndex=>{
                val prefix = String.format("%08d", int2Integer(topicWithIndex._2))
                hbw.insert[String](
                  table,
                  prefix+MessageDigest.getInstance("MD5").digest((topicWithIndex._1+Calendar.getInstance().getTimeInMillis().toString).getBytes()).mkString,
                  "infos",
                  Array("val"),
                  Array(topicWithIndex._1),
                  s => Bytes.toBytes(s))
              }) 

            }
            
            val twitterStream = TwitterStreamingAPI.getStream
            val twitterKafkaProducer = getKafkaProducer("tweets")
            val tweetToJSon = new TweetToJSONToKafka(twitterKafkaProducer)
            
            while(true){
              try{
               
                /*If any exit, clear Twitter Listeners*/
                twitterStream.clearListeners()
               
                /* Getting 1h top 10 topics */
                val meta1h = hbr.readTimeFilterArticlesMeta("article_links", 300, 0)
                val topics1h = TopicsFinder.getKeywords(10,meta1h)
                
                /* Getting 12h top 10 topics */
                val meta12h = hbr.readTimeFilterArticlesMeta("article_links", 3600, 0)
                val topics12h = TopicsFinder.getKeywords(10,meta12h)
              
                /* Getting all time topics */
                val topicsAllTime = TopicsFinder.getKeywords(100)
                
                
                /*writing in Hbase*/
                writeTopicsHbase("topics1h", topics1h)
                writeTopicsHbase("topics12h", topics12h)
                writeTopicsHbase("topicsalltime", topicsAllTime)
                
                
                /*Getting the Twitter Streams for every topic*/
                /*Creating the filter*/
                val filterQuery = new FilterQuery()
                filterQuery.track((topics1h++topics12h++topicsAllTime).map(" "+_+" ").toArray[String]) 
                filterQuery.language(Array("en"))
                
                /*Starting the streaming*/
                twitterStream.addListener(new OnTweetPosted(cb =>tweetToJSon.statusHandler(cb)))
                twitterStream.filter(filterQuery)
                

                /*fetching comments */   
                println("fetch")
               
                /*read items published between 0 min and 1 hours ago*/
                CommentsFetcher.readItems(60, 0, topics1h,topics12h,topicsAllTime)
               
                /*read items published between 1 and 2 hours ago*/
                CommentsFetcher.readItems(120, 60,topics1h,topics12h,topicsAllTime)
                
                /*Read items published between 2 and 4 hours ago*/
                CommentsFetcher.readItems(240, 60,topics1h,topics12h,topicsAllTime)

                /*Read items published between 4 and 10 hours ago*/
                CommentsFetcher.readItems(600, 240,topics1h,topics12h,topicsAllTime)
                
                /*writing time of last computation in mysql*/
                val timestamp = Calendar.getInstance().getTimeInMillis()
                /*writing the topics in Mysql*/
                topics1h.foreach(topic => {MySQLConnector.connection
                  .createStatement()
                  .executeUpdate("INSERT INTO topics1h VALUES (NULL,'"+timestamp+"','"+topic+"')")
                })

                topics12h.foreach(topic => {MySQLConnector.connection
                  .createStatement()
                  .executeUpdate("INSERT INTO topics12h VALUES (NULL,'"+timestamp+"','"+topic+"')")
                })
                
                topicsAllTime.foreach(topic => {MySQLConnector.connection
                  .createStatement()
                  .executeUpdate("INSERT INTO topicsalltime VALUES (NULL,'"+timestamp+"','"+topic+"')")
                })
                
                /*Wait 20 minutes*/
                Thread.sleep(1200000);
                
              }
              catch {
                case e: Exception => {
                  e.printStackTrace
                  System.exit(1)
                }
              }
            
            }
            
          }
          
          case "BatchLayer" => {
           
            val batch = new BatchQueries
            while(true){
              batch.registerLikedUsers
              /*Wait 20 minutes*/
              Thread.sleep(1200000);      
            }
            
           
          }
          /*Running the storm topology for real time*/
          case "SpeedLayer" => {
        (MySQLConnector.getTopics("topics1h",10)++
        MySQLConnector.getTopics("topics12h",10)++
        MySQLConnector.getTopics("topicsalltime",100)
        .distinct)
        .foreach(f=>println(f))
           new KafkaStorm(args(1),"tweets").runTopology  
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
