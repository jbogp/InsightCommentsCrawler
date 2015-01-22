package externalAPIs

import main.scala.rss.Comment
import scala.collection.mutable.ArrayBuffer
import net.liftweb.json.JArray
import net.liftweb.json.JObject
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.Serialization
import net.liftweb.json.NoTypeHints
import net.liftweb.json.JValue
import net.liftweb.json.Extraction
import main.scala.rss.CommentsList

trait ExternalAPI {
  
	implicit val formats = Serialization.formats(NoTypeHints)
	
	def fetchJSONFromURL(params:Array[String]):String
	
	def readJSON(json:String):ArrayBuffer[Comment]
	
	/*Get JSON from the comments list*/
	def getJSON(comments:ArrayBuffer[Comment]):String ={
			val listComments = comments.toList
			val commentsClass = new CommentsList(listComments)
			val listJson = Extraction.decompose(commentsClass)

			write(listJson)
	}
}