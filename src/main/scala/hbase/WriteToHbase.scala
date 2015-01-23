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
	
	

	def rowExists(table:String, rowkey:String):Boolean = {
		/*Fetch the table*/
		val httable = new HTable(conf, table)
		val theGet = new Get(Bytes.toBytes(rowkey))
		httable.exists(theGet)
	}
	
	/* 
	 * Insert URL link in the article_links table
	 * Parameters are the columns values
	 * returns true if the row was inserted (didn't exist before)
	 */
	def insertURL(values:Array[String]):Boolean = {
			val columns = Array("URL","engine","engineId","title","description")
			val row = MessageDigest.getInstance("MD5").digest(values(0).getBytes()).map("%02X".format(_)).mkString
			rowExists("article_links", row) match {
				  case false => {
					  insert[String]("article_links",row,"infos",columns.take(3),values.take(3),s => Bytes.toBytes(s))
					  insert[String]("article_links",row,"contents",columns.takeRight(2),values.takeRight(2),s => Bytes.toBytes(s))
					  true
				  }
				  case true => false
			}
	}
	
	/* 
	 * Insert comments the general comments table
	 * Parameters are the columns values
	 * returns true if the row was inserted (didn't exist before)
	 */
	def insertComments(values:Array[String],topics1h:Array[String],topics12h:Array[String],topicsAllTime:Array[String]) {
			val columns = Array("URL","json")
			val row = MessageDigest.getInstance("MD5").digest(values(0).getBytes()).map("%02X".format(_)).mkString
			/*Writing on the All comments*/
			insert[String]("comments_all",row,"infos",columns,values.take(2),s => Bytes.toBytes(s))
			
			/*Writing on topics tables*/
			val title = values(2).replaceAll("[^a-zA-Z ]", "").toLowerCase().split(" ").drop(1)
			
			val in = title.foreach(word =>{
				if(topics1h.contains(word)) {
					insert[String]("comments1h",row,"infos",Array(word),Array(values(1)),s => Bytes.toBytes(s))
				}
				else if(topics12h.contains(word)) {
					insert[String]("comments12h",row,"infos",Array(word),Array(values(1)),s => Bytes.toBytes(s))				  
				}
				else if(topicsAllTime.contains(word)) {
					insert[String]("commentsalltime",row,"infos",Array(word),Array(values(1)),s => Bytes.toBytes(s))				  
				}
			})
			
			
			
			

	}
	
	

	
}
