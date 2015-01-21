package hbase


import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.HConnectionManager
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.util.Bytes






case class KafkaToHbase() {
	
	val conf = new Configuration()
	val admin = new HBaseAdmin(conf)
	
	// list the tables
	val listtables=admin.listTables() 
	listtables.foreach(println)
	
	// let's insert some data in 'mytable' and get the row
	
	val table = new HTable(conf, "article_links")
	
	val theput= new Put(Bytes.toBytes("rowkey1"))
	
	theput.add(Bytes.toBytes("infos"),Bytes.toBytes("id1"),Bytes.toBytes("one"))
	table.put(theput)
	
	val theget= new Get(Bytes.toBytes("rowkey1"))
	val result=table.get(theget)
	val value=result.value()
	println(Bytes.toString(value))
}
	
