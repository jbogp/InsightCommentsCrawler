package main.scala

import externalAPIs.FBAPI
import hbase.WriteToHbase
import hbase.ReadFromHbase
import externalAPIs.DisqusAPI
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

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
	  			val jsonString:String = item.engine match {
	  			  	case "disqus" => {
	  			  		/*boring particular cases*/
	  			  		if(item.url.contains("abcnews")) {
	  			  			if(item.url.contains("story?id=")) {
		  			  			val urlParts = item.url.split("/")
		  			  			val newUrl = "http://abcnews.go.com/"+urlParts(urlParts.length-3)+"/"+urlParts(urlParts.length-1)
		  			  			val json = dReader.fetchJSONFromURL(Array(newUrl,item.engineId))
			  			  		val comments = dReader.readJSON(json,newUrl)
			  			  		write(comments)
			  			  	}
			  			  	else {
			  			  		"[]"
			  			  	}
	  			  		}
	  			  		else if(item.url.contains("japantimes")){
	  			  			val newUrl = item.url.split("\\?").apply(0)
	  			  			val json = dReader.fetchJSONFromURL(Array(newUrl,item.engineId))
		  			  		val comments = dReader.readJSON(json,newUrl)
		  			  		write(comments)
	  			  		}
	  			  		else {
		  			  		val json = dReader.fetchJSONFromURL(Array(item.url,item.engineId))
		  			  		val comments = dReader.readJSON(json,item.url)
		  			  		write(comments)
	  			  		}
	  			  	}
	  			  	case "fb" => {
	  			  		val json = fbReader.fetchJSONFromURL(Array(item.url,null))
	  			  		val comments = fbReader.readJSON(json,item.url)
	  			  		write(comments)
	  			  	}
	  			  	case _ => {
	  			  		println("error")
	  			  		"[]"
	  			  	}
	  			}
	  			/*Put in Hbase if not empty*/
	  			if(jsonString != "[]") {
	  				success = success+1
	  				hbw.insertComments(Array(item.url,jsonString,item.title,item.desc),topics1h,topics12h,topicsAllTime)
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
  			Thread.sleep(10);
  		})

	}

}