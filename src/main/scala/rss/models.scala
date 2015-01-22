package main.scala.rss

import java.net.URL
import java.util.{Locale, Date}
import main.scala.Utils


/*General trait defining what a RSS feed is*/
trait RssFeed {
  val link:String
  val title:String
  val desc:String
  val items:Seq[RssItem]
  override def toString = title + "\n" + desc + "\n**"
}

/*defining case classe for comments*/
case class Comment(
  created_time: String, 
  from: String,
  like_count: Int,
  message:String
)

case class CommentsList(
  comments: List[Comment]
)

/*Case class for the JSON message that will be sent to kafka for URLS*/
case class KafkaMessageURL(link:String,engine:String,engineId:String)

/*Case class for feed info*/
case class FeedInfo(info:String){
  val arrayInfo = info.split(",")
  val url = new URL(arrayInfo(0))
  val commentType = arrayInfo(1)
  val tag = arrayInfo(2)
  val engineId = arrayInfo(3) 
}

/*Case class of Atom Rss feed*/
case class AtomRssFeed(title:String, link:String, desc:String, items:Seq[RssItem]) extends RssFeed
/*Case class of xml Rss feed*/
case class XmlRssFeed(title:String, link:String, desc:String, language:String, items:Seq[RssItem]) extends RssFeed

case class SimpleRssItem(url:String,engine:String,engineId:String)

/*Case class of an rss item within a rss feed*/
case class RssItem(title:String, link:String, desc:String, guid:String) {
  /*Create a hash value unique to this item*/
  val hash = Utils.md5(this.toString)
  var engine:String = null
  var engineId:String = null
  override def toString = title
}