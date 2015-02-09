package main.scala

import externalAPIs.FBAPI
import hbase.WriteToHbase
import hbase.ReadFromHbase
import externalAPIs.DisqusAPI
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import scala.collection.mutable.ArrayBuffer
import rss.Comment

object CommentsFetcher {
 val hbr = new ReadFromHbase
 val hbw = new WriteToHbase
 val dReader = new DisqusAPI
 val fbReader = new FBAPI
 implicit val formats = Serialization.formats(NoTypeHints)

 def readItems(minMax:Int,minMin:Int,topics1h:Array[String],topics12h:Array[String],topicsAllTime:Array[String]){
  val items = hbr.readTimeFilterLinks("article_links",minMax,minMin)
  println("Starting fetching comments for "+ items.length +" articles")
  var success = 0
  var empty = 0

  items.foreach(item => {
   try{
    val commentsArray:ArrayBuffer[Comment] = item.engine match {
     case "disqus" => {
      /*boring particular cases*/
      if(item.url.contains("abcnews")) {
       if(item.url.contains("story?id=")) {
        val urlParts = item.url.split("/")
        val newUrl = "http://abcnews.go.com/"+urlParts(urlParts.length-3)+"/"+urlParts(urlParts.length-1)
        val json = dReader.fetchJSONFromURL(Array(newUrl,item.engineId))
        dReader.readJSON(json,newUrl,item.desc)

       }
       else {
        new ArrayBuffer[Comment]
       }
      }
      else if(item.url.contains("japantimes") || item.url.contains("washingtontimes")){
       val newUrl = item.url.split("\\?").apply(0)
       val json = dReader.fetchJSONFromURL(Array(newUrl,item.engineId))
       dReader.readJSON(json,newUrl,item.desc)
      }
      else {
       val json = dReader.fetchJSONFromURL(Array(item.url,item.engineId))
       dReader.readJSON(json,item.url,item.desc)
      }
     }
     case "fb" => {
      val json = fbReader.fetchJSONFromURL(Array(item.url,null))
      fbReader.readJSON(json,item.url,item.desc)
     }
     case "fbInternal" => {
      val json = fbReader.fetchJSONFromFB(Array(item.engineId))
      fbReader.readJSON(json,item.url,item.desc)
     }
     case _ => {
      println("error")
      new ArrayBuffer[Comment]
     }
    }
    /*Put in Hbase if not empty*/
    if(!commentsArray.isEmpty) {
     success = success+1
     hbw.insertComments(Array(item.url,item.title,item.desc),commentsArray,topics1h,topics12h,topicsAllTime)
    }
    else{
     empty = empty+1
    }
   }
   catch {
    case e: Exception => {
     println("Error fetching this comment")
    }
   }
   println("added: "+success+ "| skipped: "+empty)
   /*waiting to avoid scaring off the APIS*/
   Thread.sleep(300);
  })

 }

}