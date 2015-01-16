package main.scala

import java.sql.DriverManager
import java.sql.Connection
import main.scala.rss._
import java.net.URL
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Try, Success, Failure}

/*Main function, starts the crawler*/
object InsightCommentsCrawler {
  def getUrls(fileName:String) = getFileLines(fileName).map(info => new FeedInfo(info))
  def getFileLines(fileName : String): Array[String] =
    scala.io.Source.fromFile(fileName).mkString.split("\n").filter( !_.startsWith("#") )

  def main(args : Array[String]):Unit = {

	val subreader = new RssReader

    for {
      feedInfo <- getUrls("subscriptions.xml")
    } subreader.read(feedInfo)

    //system.shutdown()
  }
}