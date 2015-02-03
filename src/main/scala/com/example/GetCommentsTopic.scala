package com.example

import scala.collection.mutable.ArrayBuffer
import net.liftweb.json.DefaultFormats
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import java.sql.DriverManager
import java.sql.Connection
import java.sql.ResultSet
import collection.breakOut
import java.util.Calendar

case class TweetsResponse(tweets:ArrayBuffer[Tweet],timestamp:Long)

object GetCommentsTopic {
 
  
	implicit val formats = Serialization.formats(NoTypeHints)
	
	def getTopicsJson(queue:String):String ={
	  
		/*Getting the topics*/
		val topics:ResultSet = queue match {
		  	case x if(x == "topics1h" || x == "topics12h")  => {
				MySQLConnector.connection.createStatement()
					.executeQuery("SELECT topic FROM "+queue+" ORDER BY id DESC LIMIT 10")
		  	}
		  	case _  => {
				MySQLConnector.connection.createStatement()
					.executeQuery("SELECT topic FROM topicsalltime ORDER BY id DESC LIMIT 100")
		  	}
		}
		
		val ret = new ArrayBuffer[String]
		
		while(topics.next()) {
			ret.append(topics.getString("topic"))
		}
		
		write(ret.toList.reverse)
			
	}
	
  	def getCommentsJson(value:ArrayBuffer[List[Comment]]):String = {
		val json = value
			.reduce((t,i)=> t:::i)
			//.groupBy(_.message).map(_._2.head)
			.filterNot{ var set = Set[String]()
			obj => val b = set(obj.message); set += obj.message; b}

			json.sortBy(_.created_time)
			
		write(json)		
	}
  	
   	def getCommentsJsonByArticle(value:ArrayBuffer[List[Comment]]):String = {
   	  	def dist(l:List[Comment]) = {
   	  	  l.filterNot{ 
   	  		 var set = Set[String]()
   	  		obj => val b = set(obj.message); set += obj.message; b
   	  	  }
   	  	}
   	  	/*Formats for the comments, fb and disqus have t differently*/
   	  	val formatFB = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
		val formatDQ = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
   	  	
   	  	/*Extracting the urls and title while sorting the comments and grouping them by the article they came from*/
		val json = value
			.reduce((t,i)=> t:::i)
			.groupBy(_.url)
			.map(group => {
				/*Sometimes the title and/or the date do not exist (Option) so we handle that*/
				val sorted = dist(group._2).sortBy(_.created_time)
				
				/*Extracting the timestamp*/
				val timestamp = {
					if(sorted.length>1) {
						sorted(1).created_time match{
						  case x if x.length()>19 => formatFB.parse(x).getTime()
						  case x => formatDQ.parse(x).getTime()
						}
					}
					else
						sorted(0).created_time match{
						  case x if x.length()>19 => formatFB.parse(x).getTime()
						  case x => formatDQ.parse(x).getTime()
						}
				}
				
				
				(group._1,group._2(0).title) match {					
				  	case (Some(s),Some(t)) =>
				  		new Article(s,t,timestamp,sorted)
				  	case (Some(s),None) =>
				  		new Article(s,"Did not get this article's title",timestamp,sorted)
				  	case _ =>
				  		new Article("unknown","Did not get this article's title",timestamp	,sorted)
				}
				
			})
			
			
			
			
			write(json)
	}
  	
   	def getTweetsJson(value:ArrayBuffer[Tweet]):String = {
		val jsonTweets = value
			.sortBy(- _.createdAt)
		
		val timestamp = Calendar.getInstance().getTimeInMillis()
		write(new TweetsResponse(jsonTweets,timestamp))		
	}
	

	def getCommentsHTML(value:ArrayBuffer[List[Comment]]):String = {
		value
			.reduce((t,i)=> t:::i)
			.sortBy(- _.like_count)
			.map(t=>t.url match {
				case Some(_) => t.created_time+" "+t.from+"("+t.like_count+"): "+t.message+" <a href='"+t.url.get+"'>link</a>"
				case _ => t.created_time+" "+t.from+"("+t.like_count+"): "+t.message
			})
			.distinct
			.mkString("<hr>")
	}
  
}