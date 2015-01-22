package externalAPIs

import scala.io.Source
import net.liftweb.json.DefaultFormats
import net.liftweb.json._
import main.scala.rss.Comment
import scala.collection.mutable.ArrayBuffer

/*defining case classes*/
case class FBComment(
  id: String,
  can_remove: Boolean, 
  created_time: String, 
  from: FBUser,
  like_count: Int,
  message: String,
  user_likes: Boolean
)

case class FBUser(
  id: String,
  name: String
)

/*Get FB Comments*/
class FBAPI extends ExternalAPI {
  
  
	def fetchJSONFromURL(params:Array[String]):String = {
			println(params(0))
			val html = Source.fromURL("https://graph.facebook.com/v2.1/?fields=og_object{comments}&access_token=334762316566120|jbMVNQY0mIuS_Bw9aJzUIsfcOOc&id="+params(0))
			html.mkString
	}

	def readJSON(jsonString:String):ArrayBuffer[Comment] ={
			/*Parsing*/
			val json = parse(jsonString)
			val comments = (json \\ "data").children
			val ret = new ArrayBuffer[Comment]
			/*Extracting the comments*/
			for ( comment <- comments) {
				val m = comment.extract[FBComment]
				ret.append(new Comment(m.created_time,m.from.name,m.like_count,m.message))
			}
			ret
	}

}