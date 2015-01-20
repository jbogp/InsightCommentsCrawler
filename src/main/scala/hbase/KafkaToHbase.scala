package hbase

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{HBaseAdmin,HTable,Put,Get}
import org.apache.hadoop.hbase.util.Bytes


case class KafkaToHbase() {
	
	val conf = new HBaseConfiguration()
	val admin = new HBaseAdmin(conf)
	
	// list the tables
	val listtables=admin.listTables() 
	listtables.foreach(println)
	
	// let's insert some data in 'mytable' and get the row
	
	val table = new HTable(conf, "article_links")
	
	val theput= new Put(Bytes.toBytes("rowkey1"))
	
	theput.add(Bytes.toBytes("ids"),Bytes.toBytes("id1"),Bytes.toBytes("one"))
	table.put(theput)
	
	val theget= new Get(Bytes.toBytes("rowkey1"))
	val result=table.get(theget)
	val value=result.value()
	println(Bytes.toString(value))
}
	
