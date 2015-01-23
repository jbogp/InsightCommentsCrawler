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

  		items.foreach(item => {
  			try{
	  			item.engine match {
	  			  	case "disqus" => {
	  			  		/*boring particular cases*/
	  			  		if(item.url.contains("abcnews") && item.url.contains("story?id=")) {
	  			  			val urlParts = item.url.split("/")
	  			  			println("getting from disqus (abc news)")
	  			  			val newUrl = "http://abcnews.go.com/"+urlParts(urlParts.length-3)+"/"+urlParts(urlParts.length-1)
	  			  			val json = dReader.fetchJSONFromURL(Array(newUrl,item.engineId))
		  			  		val comments = dReader.readJSON(json)
		  			  		val jsonString = write(comments)
		  			  		hbw.insertComments(Array(item.url,jsonString,item.title),topics1h,topics12h,topicsAllTime)
	  			  		}
	  			  		else if(item.url.contains("japantimes")){
	  			  			val newUrl = item.url.split("\\?").apply(0)
	  			  			println("getting from disqus (japannews)")
	  			  			val json = dReader.fetchJSONFromURL(Array(newUrl,item.engineId))
		  			  		val comments = dReader.readJSON(json)
		  			  		val jsonString = write(comments)
		  			  		hbw.insertComments(Array(item.url,jsonString,item.title),topics1h,topics12h,topicsAllTime)
	  			  		}
	  			  		else if(!item.url.contains("abcnews")) {
		  			  		println("getting from disqus")
		  			  		val json = dReader.fetchJSONFromURL(Array(item.url,item.engineId))
		  			  		val comments = dReader.readJSON(json)
		  			  		val jsonString = write(comments)
		  			  		hbw.insertComments(Array(item.url,jsonString,item.title),topics1h,topics12h,topicsAllTime)
	  			  		}
	  			  	}
	  			  	case "fb" => {
	  			  		println("getting from fb")
	  			  		val json = fbReader.fetchJSONFromURL(Array(item.url,null))
	  			  		val comments = fbReader.readJSON(json)
	  			  		val jsonString = write(comments)
	  			  		hbw.insertComments(Array(item.url,jsonString,item.title),topics1h,topics12h,topicsAllTime)
	  			  	}
	  			  	case _ => println("error")
	  			}
  			}
  			catch {
				case e: Exception => {
					println("Error fetching this comment")
				}
  			}
  			/*waiting to avoid scaring off the APIS*/
  			Thread.sleep(500);
  		})

	}

}