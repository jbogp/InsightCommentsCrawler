package main.scala.hbase

import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.util.Bytes
import java.util.Calendar
import org.apache.hadoop.hbase.client.Scan
import main.scala.rss.SimpleRssItem
import scala.collection.mutable.ArrayBuffer
import main.scala.rss.SimpleRssItem
import main.scala.rss.SimpleRssItem
import org.apache.hadoop.hbase.client.Result
import main.scala.rss.SimpleRssItem
import main.scala.rss.SimpleRssItem
import main.scala.rss.ArticleMeta
import main.scala.rss.ArticleMeta
import main.scala.rss.ArticleMeta
import main.scala.rss.ArticleMeta

class ReadFromHbase {
  
  	/*Creating configuration and connecting*/
	val conf = new Configuration()
	val admin = new HBaseAdmin(conf)
	
	/*Generic Hbase reader to fetch all the rows of a table beetween 2 times and create objects out of that*/
	def readTimeFilterGeneric[T](table:String,minutesBackMax:Int,minutesBackMin:Int,handleRow:Result=>T):ArrayBuffer[T] = {
		/*Fetch the table*/
		val httable = new HTable(conf, table)
		val offsetMax:Long = minutesBackMax*60000L
		val offsetMin:Long = minutesBackMin*60000L
		val theScan = new Scan().setTimeRange(Calendar.getInstance().getTimeInMillis()-offsetMax, Calendar.getInstance().getTimeInMillis()-offsetMin);
		
		/*Adding timestamp filter*/
		val res = httable.getScanner(theScan)
		
		val iterator = res.iterator()
		val ret = new ArrayBuffer[T]
		while(iterator.hasNext()) {
			val next = iterator.next()
			ret.append(handleRow(next))		
		}
		ret		
	}
 
	def readTimeFilterLinks(table:String,minutesBackMax:Int,minutesBackMin:Int):ArrayBuffer[SimpleRssItem] =  {
			/*function to handle links results*/
			def handleRow(next:Result):SimpleRssItem = {
				new SimpleRssItem(
			    new String(next.getColumn("infos".getBytes(), "URL".getBytes()).get(0).getValue()),
			    new String(next.getColumn("infos".getBytes(), "engine".getBytes()).get(0).getValue()),
			    new String(next.getColumn("infos".getBytes(), "engineId".getBytes()).get(0).getValue()),
			    new String(next.getColumn("contents".getBytes(), "title".getBytes()).get(0).getValue())
				)		
			}
			/*Calling the database*/
			readTimeFilterGeneric[SimpleRssItem](table, minutesBackMax, minutesBackMin, handleRow)
	}
	
	def readTimeFilterTopics(table:String,minutesBackMax:Int,minutesBackMin:Int):ArrayBuffer[String] =  {
			/*function to handle links results*/
			def handleRow(next:Result):String = {
			    new String(next.getColumn("infos".getBytes(), "URL".getBytes()).get(0).getValue())		
			}
			/*Calling the database*/
			readTimeFilterGeneric[String](table, minutesBackMax, minutesBackMin, handleRow)
	}
	
	def readTimeFilterArticlesMeta(table:String,minutesBackMax:Int,minutesBackMin:Int):ArrayBuffer[ArticleMeta] =  {
		/*function to handle meta link results*/
		def handleRow(next:Result):ArticleMeta = {
			new ArticleMeta(
		    new String(next.getRow()),
		    new String(next.getColumn("contents".getBytes(), "description".getBytes()).get(0).getValue()),
		    new String(next.getColumn("contents".getBytes(), "title".getBytes()).get(0).getValue())
			)		
		}
		/*Calling the database*/
		readTimeFilterGeneric[ArticleMeta](table, minutesBackMax, minutesBackMin, handleRow)
	}

}