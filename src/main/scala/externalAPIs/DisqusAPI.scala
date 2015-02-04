package externalAPIs

import scala.io.Source
import net.liftweb.json.DefaultFormats
import net.liftweb.json._
import main.scala.rss.Comment
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/*defining case classes*/
case class DisqusComment(
  parent: JValue,
  likes: Int, 
  forum: JValue, 
  thread:JValue,
  isApproved:JValue,
  author:DisqusUser,
  media:List[JArray],
  isFlagged:JValue,
  dislikes:JValue,
  raw_message:String,
  createdAt:String,
  id:JValue,
  numReports:JValue,
  isDeleted:JValue,
  isEdited:JValue,
  message:JValue,
  isSpam:JValue,
  isHighlighted:JValue,
  points:JValue
)

case class DisqusUser(
  username:String,
  about:String,
  name:String,
  disable3rdPartyTrackers:Boolean,
  url:String,
  isAnonymous:Boolean,
  rep:Double,
  profileUrl:String,
  reputation:Double,
  location:String,
  isPrivate:Boolean,
  isPrimary:Boolean,
  joinedAt:String,
  id:String,
  avatar:JObject
)

case class DisqusAnonComment(
  parent: JValue,
  likes: Int, 
  forum: JValue, 
  thread:JValue,
  isApproved:JValue,
  author:DisqusAnonUser,
  media:List[JArray],
  isFlagged:JValue,
  dislikes:JValue,
  raw_message:String,
  createdAt:String,
  id:JValue,
  numReports:JValue,
  isDeleted:JValue,
  isEdited:JValue,
  message:JValue,
  isSpam:JValue,
  isHighlighted:JValue,
  points:JValue
)

case class DisqusAnonUser(
  name:String,
  url:String,
  profileUrl: String,
  emailHash: String,
  isAnonymous:Boolean,
  avatar:JObject
)

/*Get FB Comments*/
class DisqusAPI extends ExternalAPI {
  
  
	def fetchJSONFromURL(params:Array[String]):String = {
			val keys = Array("wnii9oY7d5a7yay80egKAnF7jAAkytjMZfFTPSqyjMqpCOI7WpCUQl7XPCDfti7V",
			    "mx8SdLAdFQRFncQAYlt1qwVpFCOjTjO7q9YctA4dPL57tQ1ERzt3iN16yKYWKB9V",
			    "JWuvdKJkgmUDTCiCpqAz7yHECoiMpaSLKYa3LY0XkI0yLDlCPwzXVwFI2FoBXY0N",
			    "IDXDDVf4RafAwx870i3XfxBg3jW75U7CUXeUsKN5cHpgNfVdsxLSoH8RRNPViTl1",
			    "Ci9eNvHTCld09sQ0G5dfFGi0Ogmtg6shAFh5vI5shORXpHvwQH0rpREIcVOi5u9B")
			val rand = new Random(System.currentTimeMillis());
			val random_index = rand.nextInt(keys.length);
			val i = keys(random_index);
			val html = Source.fromURL("http://disqus.com/api/3.0/threads/listPosts.json?api_key="+i+"&limit=100&thread=link:"+params(0)+"&forum="+params(1))
			html.mkString
	}

	def readJSON(jsonString:String,url:String,title:String):ArrayBuffer[Comment] ={
			/*Parsing*/
			val json = parse(jsonString)
			val comments = (json \\ "response").children
			val ret = new ArrayBuffer[Comment]
			println(title)
			/*Extracting the comments*/
			for ( comment <- comments) {
				try{
					val m = comment.extract[DisqusComment]
					ret.append(new Comment(m.createdAt,m.author.name,m.likes,m.raw_message,url,title))
				}
				catch {
				  	case e:Exception => {
				  		val m = comment.extract[DisqusAnonComment]
				  		ret.append(new Comment(m.createdAt,m.author.name,m.likes,m.raw_message,url,title))
				  	}
				}
			}
			ret
	}

}