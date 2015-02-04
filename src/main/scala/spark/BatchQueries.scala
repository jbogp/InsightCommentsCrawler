package main.scalaspark
import main.scala.hbase.WriteToHbase
import main.scala.hbase.ReadFromHbase
import scala.io.Source
import scala.collection.mutable.ArrayBuffer
import org.apache.spark._
import org.apache.spark.SparkContext._
import main.scala.rss.ArticleMeta
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.spark.rdd.NewHadoopRDD
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.client.Result
import scala.collection.JavaConverters._
import org.apache.hadoop.hbase.CellUtil
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import scala.Array.canBuildFrom
import main.scala.hbase.UserComment
import java.util.Calendar
import org.apache.hadoop.hbase.util.Bytes
import net.liftweb.json.JInt



class BatchQueries extends Serializable{
  
  	val hbr = new ReadFromHbase
	val hbw = new WriteToHbase
	
							
  		
	/*Map reduce procedure to aggregate the number of likes users get*/
	def registerLikedUsers():Unit= {
  	  
  	  	val conf = new SparkConf().setAppName("Spark Topics").setMaster("local")
		val spark = new SparkContext(conf)
  	  	

  	
  	  

		  
		/*Creating HBase configuration*/
		val hbaseConfiguration = (hbaseConfigFileName: String, tableName: String) => {
		  val hbaseConfiguration = HBaseConfiguration.create()
		  hbaseConfiguration.addResource(hbaseConfigFileName)
		  hbaseConfiguration.set(TableInputFormat.INPUT_TABLE, tableName)
		  hbaseConfiguration
		 }
		
		/*get the users table as a RDD*/
		val rdd = new NewHadoopRDD(
		  spark,
		  classOf[TableInputFormat],
		  classOf[ImmutableBytesWritable],
		  classOf[Result],
		  hbaseConfiguration("/opt/cloudera/parcels/CDH/lib/hbase/conf/", "users")
		)
			
		/*Map every user to it's like*/
		val likes_map = rdd.map(tuple => tuple._2)
		/*transforming into an arraybuffer of lists of properties for every user*/
		.flatMap(r => {
			/*getting the family map for the current row*/
			val col = r.getFamilyMap("infos".getBytes()).keySet()
			val it = col.iterator()
			val ret = new ArrayBuffer[(String,Int)]
			while(it.hasNext()){
				val currentCol = it.next()
				val comment = new String(CellUtil.cloneValue(r.getColumnLatestCell("infos".getBytes(), currentCol)))
				
				/*hacking json parse*/
				val likes = """"like_count" ?: ?(\d+),""".r
				val like_count = comment match {
				  	case count => count
				} 
				
				/*emitting the tuples*/
				ret.append((new String(r.getRow()),like_count.toInt))
			}
			ret
		})
		
		
		/*Map every comment to the number they've been posted to detect potential spam messages
		val spam_detect = rdd.map(tuple => tuple._2)
		/*transforming into an arraybuffer of lists of properties for every user*/
		.flatMap[(String,Int)](r => {
			/*getting the family map for the current row*/
			val col = r.getFamilyMap("infos".getBytes()).keySet()
			val it = col.iterator()
			val ret = new ArrayBuffer[(String,Int)]
			while(it.hasNext()){
				val currentCol = it.next()
				val comment = net.liftweb.json.parse(new String(CellUtil.cloneValue(r.getColumnLatestCell("infos".getBytes(), currentCol))))
							.extract[UserComment]
				/*emitting the tuples*/
				ret.append((comment.message,1))
			}
			ret
		})
		*/
		  
  	  	/*reduce step*/
		val likes_reduce = likes_map.reduceByKey(_ + _).collect.sortBy(_._2).reverse
		val (users, likes) = likes_reduce.unzip
		//val spam_reduce = spam_detect.reduceByKey(_+_).filter(_._2>1).sortByKey(false, 1).collect
		//val (columns_spam, values_spam) = spam_reduce.unzip 
		
		/*register the values in Hbase*/
		val timestamp = Calendar.getInstance().getTimeInMillis()
		//hbw.insert[Int]("spam", timestamp.toString, "infos", columns_spam.toArray, values_spam.toArray, s => Bytes.toBytes(s),true)	
		hbw.insert[Int]("users_aggregates", timestamp.toString, "infos", users.toArray, likes.toArray, s => s.toString.getBytes(),true)
						
		spark.stop
		
		
  	}

}