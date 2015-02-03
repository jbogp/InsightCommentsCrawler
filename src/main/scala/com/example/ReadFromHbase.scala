package com.example

import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.util.Bytes
import java.util.Calendar
import org.apache.hadoop.hbase.client.Scan
import scala.collection.mutable.ArrayBuffer
import net.liftweb.json.DefaultFormats
import net.liftweb.json._
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.fs.viewfs.Constants
import org.apache.hadoop.hbase.HBaseConfiguration
import scala.concurrent._
import ExecutionContext.Implicits.global
import org.apache.hadoop.hbase.CellUtil
import org.apache.hadoop.hbase.client.HConnectionManager

case class Comment(created_time:String,from:String,like_count:Int,message:String,url:Option[String],title:Option[String])

case class Article(url:String,title:String,firstPost:Long,comments:List[Comment])
/*Case class for Tweet Message*/
case class Tweet(message:String,createdAt:Long,latitude:Double,longitude:Double,id:Long,rt_count:Int,from:String,from_pic:String,from_url:String)



object ReadFromHbase {
  
  	/*Creating configuration and connecting*/
	val config = HBaseConfiguration.create()
    config.clear();
    config.set("hbase.zookeeper.quorum", "ip-172-31-11-73.us-west-1.compute.internal");
    config.set("hbase.zookeeper.property.clientPort","2181");
    config.set("hbase.master", "ip-172-31-11-73.us-west-1.compute.internal:60000");
	implicit val formats = Serialization.formats(NoTypeHints)
	
	val conn = HConnectionManager.createConnection(config)
		
	
	/*Generic Hbase reader to fetch all the rows of a table beetween 2 times and create objects out of that*/
	def readTimeFilterGeneric[T](table:String,minutesBackMax:Int,minutesBackMin:Int,handleRow:Result=>T,column:String):ArrayBuffer[T] = {
		/*Fetch the table*/
	  
		val httable = conn.getTable(table)
		val offsetMax:Long = minutesBackMax*60000L
		val offsetMin:Long = minutesBackMin*60000L
		
		

		val theScan = new Scan()
			.addColumn("infos".getBytes(),column.getBytes())
			.setTimeRange(Calendar.getInstance().getTimeInMillis()-offsetMax, Calendar.getInstance().getTimeInMillis()-offsetMin)
			
			
		
		
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
	
	
	/*Generic Hbase reader to fetch all the rows of a table beetween 2 times and create objects out of that*/
	def readTimestampGeneric[T](table:String,timestampBack:Long,handleRow:Result=>T,column:String):ArrayBuffer[T] = {
		/*Fetch the table*/
	  
		val httable = conn.getTable(table)
		
		
		

		val theScan = new Scan()
			.addColumn("infos".getBytes(),column.getBytes())
			.setTimeRange(timestampBack, Calendar.getInstance().getTimeInMillis())
			
			
		
		
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
	

	
	
	
	def readFutureTrendsComments(table:String,column:String):Future[ArrayBuffer[String]] =  Future{
		/*function to handle meta link results*/
		def handleRow(next:Result):String = {
			val jsonString = {
			  val col = next.getColumnLatestCell("infos".getBytes(), column.getBytes())
			  val value = CellUtil.cloneValue(col)
			  if(value.length != 0)
				  new String(value)
			  else
				  "empty"
			}
			
			jsonString
		}
		/*Calling the database*/
		readTimeFilterGeneric[String](table, 20, 0, handleRow,column)
	}
 
	
	def readFutureTimeFilterComments(table:String,column:String,minutesBackMax:Int,minutesBackMin:Int):Future[ArrayBuffer[List[Comment]]] = Future {
		/*function to handle meta link results*/
		def handleRow(next:Result):List[Comment] = {
			/*getting comments*/
			val jsonString = {
			  val col = next.getColumnLatestCell("infos".getBytes(), column.getBytes())
			  if(col != null){
				  val value = CellUtil.cloneValue(col)
				  new String(value)
			  }
			  else
				  """[{"message":"noth""created_time":"never","from":"noone","like_count":0,"message":"nothing","url":"none","title":"none"}]"""
			}
			
			val json = parse(jsonString)
			json.extract[List[Comment]]
		}
		/*Calling the database*/
		readTimeFilterGeneric[List[Comment]](table, minutesBackMax, minutesBackMin, handleRow,column)
	}
	
	def readFutureTimeFilterTweets(table:String,column:String,timestampBack:Long):Future[ArrayBuffer[Tweet]] = Future {
		/*function to handle meta link results*/
		def handleRow(next:Result):Tweet = {
			/*getting comments*/
			val jsonString = {
			  val col = next.getColumnLatestCell("infos".getBytes(), column.getBytes())
			  val value = CellUtil.cloneValue(col)
			  new String(value)
			}
			
			val json = parse(jsonString)
			json.extract[Tweet]
		}
		/*Calling the database*/
		readTimestampGeneric[Tweet](table, timestampBack, handleRow,column)
	}

}