package main.scala.rss

import scala.xml._
import java.net.URL
import java.text.SimpleDateFormat
import java.util.{Locale, Date}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Try, Success, Failure}
import java.io._
import scala.collection.mutable.ArrayBuffer


/*Abstract class defining a general RSS reader*/
abstract class Reader{
	/*Prints the latest item from the feed*/
	def print(feed:RssFeed) {
		println(feed.latest)
	}
	
	/*Extract the items from a well formed XML*/
	def extract(xml:Elem) : Seq[RssFeed]
}


/*Implementation of the Reader abstract class, to read Atom formatted Atom feeds*/
class AtomReader extends Reader {

	/*Formater to adapt Atom dates to Date*/
	val dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);

	/*Function to parse AtomDate*/
	private def parseAtomDate(date:String, formatter:SimpleDateFormat):Date = {
			val newDate = date.reverse.replaceFirst(":", "").reverse
			return formatter.parse(newDate)
	}
	
	/*Getting the html links from one xml item node*/
	private def getHtmlLink(node:NodeSeq) = {
		node
		.filter(n => (n \ "@type").text == "text/html")
		.map( n => (n \ "@href").text).head
	}

	/*Extract all informations about the items in each feed node*/
	def extract(xml:Elem) : Seq[RssFeed] = {
		/*For each feed*/
		for (feed <- xml \\ "feed") yield {
			/*Loop on the items*/
			val items = for (item <- (feed \\ "entry")) yield {
				try {
					/*Read the item and store it*/
					RssItem(
							(item \\ "title").text,
							getHtmlLink((item \\ "link")),
							(item \\ "summary").text,
							parseAtomDate((item \\ "published").text, dateFormatter),
							(item \\ "id").text
							)
				}
				catch {
				  case e: Exception => {
					  println("error reading item")
					  RssItem("fake","fake","fake",parseAtomDate("2015-01-01",dateFormatter),"fake")
				  }
				}
			}
			/*Get the general info about the feed and take the 8 most recent items*/
			AtomRssFeed(
					(feed \ "title").text,
					getHtmlLink((feed \ "link")),
					(feed \ "subtitle ").text,
					items.take(20))
		}
	}

	def receive(xml:Elem) = {
		extract(xml) match {
		case head :: tail => print(head)
		case Nil =>
		}
	}
}

/*Implementation of the Reader abstract class, to read RSS formatted feeds*/
class XmlReader(tag:String) extends Reader {
	/*Formater to adapt RSS dates to Date*/
	val dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

	/*Extract all informations about the items in each feed node*/
	def extract(xml:Elem) : Seq[RssFeed] = {
			/*For each channel*/
			for (channel <- xml \\ "channel") yield {
				/*Loop on the items*/
				val items = for (item <- (channel \\ "item")) yield {
					try{
						RssItem(
								(item \\ "title").text,
								(item \\ tag).text,
								(item \\ "description").text,
								dateFormatter.parse((item \\ "pubDate").text),
								(item \\ "guid").text
								)
					}
					catch{
						case e: Exception => {
							println("error while parsing item")
							RssItem("fake","fake","fake",dateFormatter.parse("Thu, 15 Jan 2015 12:49:28 GMT"),"fake")
						}
					}
				}
				/*Get the general info about the feed and take the 8 most recent items*/
				XmlRssFeed(
						(channel \ "title").text,
						(channel \ "link").text,
						(channel \ "description").text,
						(channel \ "language").text,
						items.take(20))
			}
	}

	def receive(xml:Elem) = {
		extract(xml) match {
		case head :: tail => print(head)
		case Nil =>
		}
	}
}


class RssReader{

	val itemArray = new ArrayBuffer[RssItem]
	
	def read(feedInfo : FeedInfo) = {
	  
	  
		Try(feedInfo.url.openConnection.getInputStream) match {
		case Success(u) => {
			val xml = XML.load(u)
					val actor = {
				if((xml \\ "channel").length == 0) {
					new AtomReader
				}
				else{
					new XmlReader(feedInfo.tag)
				}
			}
			val res = actor.extract(xml)
			for{feed <- res; item <- feed.items} {
			  println(item.link)
			  itemArray.append(item)
			}
		}
		case Failure(_) =>

		}
	}

	def receive(path:FeedInfo) = {
		read(path)
	}
}

