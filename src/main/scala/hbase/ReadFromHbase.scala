package main.scala.hbase

import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.util.Bytes
import java.util.Calendar
import org.apache.hadoop.hbase.client.Scan

class ReadFromHbase {
  
  	/*Creating configuration and connecting*/
	val conf = new Configuration()
	val admin = new HBaseAdmin(conf)
 
	def readTimeFilter(table:String) {
		/*Fetch the table*/
		val httable = new HTable(conf, table)
		val test:Long = 1421878066639L
		val theScan = new Scan().setTimeRange(test, Calendar.getInstance().getTimeInMillis()+10);
		
		/*Adding timestamp fileter*/
		
		val res = httable.getScanner(theScan)
		
		val iterator = res.iterator()
		
		while(iterator.hasNext()) {
			println(new String(iterator.next().toString()))
		}
	}

}