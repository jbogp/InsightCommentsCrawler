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

class ReadFromHbase {
  
  	/*Creating configuration and connecting*/
	val conf = new Configuration()
	val admin = new HBaseAdmin(conf)
 
	def readTimeFilterLinks(table:String,minutesBack:Int):ArrayBuffer[SimpleRssItem] =  {
		/*Fetch the table*/
		val httable = new HTable(conf, table)
		val offset:Long = minutesBack*60000L
		val theScan = new Scan().setTimeRange(offset, Calendar.getInstance().getTimeInMillis());
		
		/*Adding timestamp filter*/
		
		val res = httable.getScanner(theScan)
		
		val iterator = res.iterator()
		val ret = new ArrayBuffer[SimpleRssItem]
		while(iterator.hasNext()) {
			val next = iterator.next()
			ret.append(new SimpleRssItem(
			    new String(next.getColumn("infos".getBytes(), "URL".getBytes()).get(0).getValue()),
			    new String(next.getColumn("infos".getBytes(), "engine".getBytes()).get(0).getValue()),
			    new String(next.getColumn("infos".getBytes(), "engineId".getBytes()).get(0).getValue())
			))		
		}
		ret
	}

}