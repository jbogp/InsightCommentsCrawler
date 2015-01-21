package main.scala.hbase


import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.HConnectionManager
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.util.Bytes
import java.security.MessageDigest
import java.util.Calendar

/*Stores and reads values in the Hbase*/
case class WriteToHbase() {
	
  
  
	/*Creating configuration and connecting*/
	val conf = new Configuration()
	val admin = new HBaseAdmin(conf)
	
	/*Generic function to insert in HBase*/
	def insert[T](table:String, rowkey:String, familly:String, column:Array[String], value:Array[T], convertToBytes:(T)=>Array[Byte]) {
		/*Fetch the table*/
		val httable = new HTable(conf, table)
		
		/*Adding the columns*/
		(column,value).zipped.foreach((col,valueTowrite) => {
			/*Defining the rowkey*/
			val theput= new Put(Bytes.toBytes(rowkey))
			theput.add(Bytes.toBytes(familly),Bytes.toBytes(col),convertToBytes(valueTowrite))
			httable.checkAndPut(Bytes.toBytes(rowkey),Bytes.toBytes(familly),Bytes.toBytes(col),null,theput)	
		})
	
	}
	
	/* 
	 * Insert URL link in the article_links table
	 * Parameters are the columns values
	 */
	def insertURL(values:Array[String]) {
			val columns = Array("URL","engine","engineId")
			val timestamp = Calendar.getInstance().getTime()
			val row = timestamp.toString()+MessageDigest.getInstance("MD5").digest(columns(1).getBytes()).map("%02X".format(_)).mkString
			insert[String]("article_links",row,"infos",columns,values,s => Bytes.toBytes(s))
	}
	
	
	
	/*Kafka queue to HBase writer*/
	def HbaseByteWriter(write:Array[Byte]): Unit = {
		println("Writing URL to Hbase"+new String(write))
		//insertURL(MessageDigest.getInstance("MD5").digest(write).map("%02X".format(_)).mkString, new String(write))
	}
	
}
	
