
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
import org.apache.hadoop.hbase.Cell

case class Comment(created_time:String,from:String,like_count:Int,message:String,url:Option[String],title:Option[String])

case class Article(url:String,title:String,firstPost:Long,comments:List[Comment])
/*Case class for Tweet Message*/
case class Tweet(message:String,createdAt:Long,latitude:Double,longitude:Double,id:Long,rt_count:Int,from:String,from_pic:String,from_url:String)

/*Some case classes*/
case class UserComment(url:String,message:String,title:String,like_count:Int,created_at:String)

case class UserLikes(pseudo:String,likes:Int)

case class SpamMess(message:String,num:Int)


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
	def readTimeFilterGeneric[T](table:String,minutesBackMax:Int,minutesBackMin:Int,handleRow:Cell=>T,row:String):ArrayBuffer[T] = {
		/*Fetch the table*/
	  
		val httable = conn.getTable(table)
		val offsetMax:Long = minutesBackMax*60000L
		val offsetMin:Long = minutesBackMin*60000L
		
		

		val theGet = new Get(row.getBytes())
			.setTimeRange(Calendar.getInstance().getTimeInMillis()-offsetMax, Calendar.getInstance().getTimeInMillis()-offsetMin)
			
			
		
		
		/*Adding timestamp filter*/
		val res = httable.get(theGet)
		
		val columns = res.getFamilyMap("infos".getBytes()).keySet()

		val iterator = columns.iterator()
		val ret = new ArrayBuffer[T]
		while(iterator.hasNext()) {
			val nextColumn = iterator.next()
			ret.append(handleRow(res.getColumnLatestCell("infos".getBytes(), nextColumn)))		
		}
		ret		
	}
	
	
	/*Generic Hbase reader to fetch all the rows of a table beetween 2 times and create objects out of that*/
	def readTimestampGeneric[T](table:String,handleRow:Cell=>T,row:String,timestampBack:Long=0L):ArrayBuffer[T] = {
		/*Fetch the table*/
	  
		val httable = conn.getTable(table)
		
		val theGet = {
		  if(timestampBack == 0L){
			new Get(row.getBytes())
		  }
		  else{
		    new Get(row.getBytes())
				.setTimeRange(timestampBack, Calendar.getInstance().getTimeInMillis())
		  }
		}
			
		/*Adding timestamp filter*/
		val res = httable.get(theGet)

		val columns = res.getFamilyMap("infos".getBytes()).keySet()

		val iterator = columns.iterator()
		val ret = new ArrayBuffer[T]
		while(iterator.hasNext()) {
			val nextColumn = iterator.next()
			ret.append(handleRow(res.getColumnLatestCell("infos".getBytes(), nextColumn)))		
		}
		ret		
	}
	

	
	
	
	def readFutureTrendsComments(table:String,row:String):Future[ArrayBuffer[String]] =  Future{
		/*function to handle meta link results*/
		def handleRow(next:Cell):String = {
			val jsonString = {
			  val value = CellUtil.cloneValue(next)
			  if(value.length != 0)
				  new String(value)
			  else
				  "empty"
			}
			
			jsonString
		}
		/*Calling the database*/
		readTimeFilterGeneric[String](table, 20, 0, handleRow,row)
	}
	
	def readUserComments(table:String,row:String):Future[ArrayBuffer[UserComment]] = Future {
		/*function to handle meta link results*/
		def handleRow(next:Cell):UserComment = {
			/*getting comments*/
			val jsonString = {
			  
			  if(next != null){
				  val value = CellUtil.cloneValue(next)
				  new String(value)
			  }
			  else
				  """{"url":"noth""message":"never","title":"noone","like_count":0,"created_at":"nothing"}"""
			}
			
			val json = parse(jsonString)
			json.extract[UserComment]
		}
		/*Calling the database*/
		readTimestampGeneric[UserComment](table, handleRow,row)
	}
 
	
	def readFutureTimeFilterComments(table:String,row:String,minutesBackMax:Int,minutesBackMin:Int):Future[ArrayBuffer[List[Comment]]] = Future {
		/*function to handle meta link results*/
		def handleRow(next:Cell):List[Comment] = {
			/*getting comments*/
			val jsonString = {
			  
			  if(next != null){
				  val value = CellUtil.cloneValue(next)
				  new String(value)
			  }
			  else
				  """[{"message":"noth""created_time":"never","from":"noone","like_count":0,"message":"nothing","url":"none","title":"none"}]"""
			}
			
			val json = parse(jsonString)
			json.extract[List[Comment]]
		}
		/*Calling the database*/
		readTimeFilterGeneric[List[Comment]](table, minutesBackMax, minutesBackMin, handleRow,row)
	}
	
	def readFutureTimeFilterTweets(table:String,row:String,timestampBack:Long):Future[ArrayBuffer[Tweet]] = Future {
		/*function to handle meta link results*/
		def handleRow(next:Cell):Tweet = {
			/*getting comments*/
			val jsonString = {
			  val value = CellUtil.cloneValue(next)
			  new String(value)
			}
			
			val json = parse(jsonString)
			json.extract[Tweet]
		}
		/*Calling the database*/
		readTimestampGeneric[Tweet](table, handleRow,row, timestampBack)
	}
	
	/*getting the most liked users*/
	def get_users_aggregates(num:Int=100):Future[ArrayBuffer[UserLikes]] = Future{
		/*Fetch the table*/
		val httable = conn.getTable("users_aggregates")
		
		val theScan = new Scan()
		/*getting the first row*/
		val firstRow = httable.getScanner(theScan).next()
		val columns = firstRow.getFamilyMap("infos".getBytes()).keySet()

		val iterator = columns.iterator()
		val ret = new ArrayBuffer[UserLikes]
		var count = 0
		while(iterator.hasNext()) {
			val nextColumn = iterator.next()
			val num = new String(CellUtil.cloneValue(firstRow.getColumnLatestCell("infos".getBytes(), nextColumn))).toInt
			val user = new String(nextColumn)
			ret.append(new UserLikes(user,num))
			count = count+1
		}
		ret.sortBy(_.likes).takeRight(num).reverse	
			  
	}
	
	def get_spam(num:Int=100):Future[ArrayBuffer[SpamMess]] = Future{
		/*Fetch the table*/
		val httable = conn.getTable("spam")
		
		val theScan = new Scan()
		/*getting the first row*/
		val firstRow = httable.getScanner(theScan).next()
		val columns = firstRow.getFamilyMap("infos".getBytes()).keySet()

		val iterator = columns.iterator()
		val ret = new ArrayBuffer[SpamMess]
		var count = 0
		while(iterator.hasNext()) {
			val nextColumn = iterator.next()
			val num = new String(CellUtil.cloneValue(firstRow.getColumnLatestCell("infos".getBytes(), nextColumn))).toInt
			val mess = new String(nextColumn)
			ret.append(new SpamMess(mess,num))
			count = count+1
		}
		ret.sortBy(_.num).takeRight(num).reverse	
			  
	}

}