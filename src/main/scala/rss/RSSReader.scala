package main.scala.rss

import java.io._
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import scala.io.Source
import scala.util.Try
import scala.xml._
import net.liftweb.json.DefaultFormats
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

/*Abstract class defining a general RSS reader*/
abstract class Reader{	
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
							(item \\ "id").text
							)
				}
				catch {
				  case e: Exception => {
					  println("error reading item")
					  RssItem("fake","fake","fake","fake")
				  }
				}
			}
			/*Get the general info about the feed and take the 8 most recent items*/
			AtomRssFeed(
					(feed \ "title").text,
					getHtmlLink((feed \ "link")),
					(feed \ "subtitle ").text,
					items.take(100))
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
								(item \\ "guid").text
								)
					}
					catch{
						case e: Exception => {
							e.printStackTrace()
							println("error while parsing item")
							RssItem("fake","fake","fake","fake")
						}
					}
				}
				/*Get the general info about the feed and take the 8 most recent items*/
				XmlRssFeed(
						(channel \ "title").text,
						(channel \ "link").text,
						(channel \ "description").text,
						(channel \ "language").text,
						items.take(100))
			}
	}

	def receive(xml:Elem) = {
		extract(xml) match {
		case head :: tail => print(head)
		case Nil =>
		}
	}
}



/*reading one subscription item and gets the links infos*/
class RssReader{

	val itemArray = new ArrayBuffer[RssItem]
	implicit val formats = Serialization.formats(NoTypeHints)
	
	def read(feedInfo : FeedInfo) = {
	  
		if(!(feedInfo.commentType == "fbInternal")){
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
				  item.engine = feedInfo.commentType
				  item.engineId = feedInfo.engineId
				  itemArray.append(item)
				}
			}
			case Failure(_) =>{}
			}
	
		}
		//Getting facebook page post
		else {
			val json = parse(Source.fromURL(feedInfo.url).mkString)
			val postsJson = (json \ "data").children
			for(post <- postsJson){
				try {
					val fb = post.extract[FBInternalPost]
					val item = new RssItem(fb.name,fb.link,fb.description,fb.postId)
					item.engineId = fb.postId
					itemArray.append(item)
					println(item.link)
				}
				catch{
				  case e:Exception => println("error")//probably not an adequate post
				}
			}
		}
	}

	def receive(path:FeedInfo) = {
		read(path)
	}
}

