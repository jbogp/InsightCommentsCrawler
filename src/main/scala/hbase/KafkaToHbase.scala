package main.scala.hbase


import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.HConnectionManager
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.util.Bytes
import java.security.MessageDigest

/*Stores and reads values in the Hbase*/
case class KafkaToHbase() {
	
  
  
	/*Creating configuration and connecting*/
	val conf = new Configuration()
	val admin = new HBaseAdmin(conf)
	
	/*Generic function to insert in HBase*/
	def insert[T](table:String, rowkey:String, familly:String, column:String, value:T, convertToBytes:(T)=>Array[Byte]) {
		/*Fetch the table*/
		val httable = new HTable(conf, table)
		
		/*Defining the rowkey*/
		val theput= new Put(Bytes.toBytes(rowkey))
		
		theput.add(Bytes.toBytes(familly),Bytes.toBytes(column),convertToBytes(value))
		httable.put(theput)		
	}
	
	/*Is the keyrow/column already defined?*/
	def exists(table:String, rowkey:String, familly:String, column:String): Boolean =  {
		/*Fetch the table*/
		val httable = new HTable(conf, table)	
		val theget= new Get(Bytes.toBytes(rowkey))
		
		val result=httable.get(theget)
		/*return boolean*/
		!result.containsEmptyColumn(Bytes.toBytes(familly), Bytes.toBytes(column))		
	}
	
	/* 
	 * Insert URL link in the article_links table
	 * Parameters are the column the value
	 */
	def insertURL(hashURL:String,URL:String) {
		if(!exists("article_links",hashURL,"infos","URL")) {
			insert[String]("article_links",hashURL,"infos","URL",URL,s => Bytes.toBytes(s))
		}
	}
	
	/*Kafka queue to HBase writer*/
	def HbaseWriter(write:Array[Byte]): Unit = {
		println("Writing URL to Hbase"+new String(write))
		insertURL(new String(MessageDigest.getInstance("MD5").digest(write)), new String(write))
	}
	
}
	
