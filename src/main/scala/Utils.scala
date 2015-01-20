package main.scala

import java.security.MessageDigest
import rss.FeedInfo

object Utils {
	def md5(s: String) = {
		MessageDigest.getInstance("MD5").digest(s.getBytes)
	}
	
	def getUrls(fileName:String) = getFileLines(fileName).map(info => {
	  println("test");
	  new FeedInfo(info)
	})
	def getFileLines(fileName : String): Array[String] = scala.io.Source.fromFile(fileName).mkString.split("\n").filter( !_.startsWith("#") )
	
}