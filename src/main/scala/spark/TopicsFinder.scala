package main.scala.spark
import main.scala.hbase.WriteToHbase
import main.scala.hbase.ReadFromHbase
import scala.io.Source
import net.liftweb.json._
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


object TopicsFinder {
  
  	val hbr = new ReadFromHbase
	val hbw = new WriteToHbase
	


	
	val dictionnary = Source.fromFile("common_words") mkString
							
  		
	def getKeywords(num:Int,corpus:ArrayBuffer[ArticleMeta]=null):Array[String]= {
  	  
  	  	val conf = new SparkConf().setAppName("Spark Topics").setMaster("local")
		val spark = new SparkContext(conf)
  	
  	  
  		/*Getting all infos together either from Hbase or corpus*/
		val titles = {
			 if(corpus != null){
		  		val all = corpus.map(article => article.title).reduceLeft(_ + " " +_)
				/*Cleaning*/
				val corpusStriped = all.replaceAll("'s", "").replaceAll("[^a-zA-Z ]", "").toLowerCase()
				println(corpusStriped)
				spark.parallelize(corpusStriped :: Nil).flatMap(_.split(" "))
			}
			else{
			  
				/*Creating HBase configuration*/
				val hbaseConfiguration = (hbaseConfigFileName: String, tableName: String) => {
				  val hbaseConfiguration = HBaseConfiguration.create()
				  hbaseConfiguration.addResource(hbaseConfigFileName)
				  hbaseConfiguration.set(TableInputFormat.INPUT_TABLE, tableName)
				  hbaseConfiguration
				 }
				
				val rdd = new NewHadoopRDD(
				  spark,
				  classOf[TableInputFormat],
				  classOf[ImmutableBytesWritable],
				  classOf[Result],
				  hbaseConfiguration("/opt/cloudera/parcels/CDH/lib/hbase/conf/", "article_links")
				)
			
				val tokenized = rdd.map(tuple => tuple._2)
				tokenized.map[String](r => {
					  		if(r.containsColumn("contents".getBytes(), "description".getBytes())){
					  			new String(CellUtil.cloneValue(r.getColumnLatestCell("contents".getBytes(), "description".getBytes())))
					  		}
					  		else {
					  			""
					  		}
				  })
			}
		}
		  
		val wordCounts = titles
		  .flatMap(_.replaceAll("'s", "").replaceAll("[^a-zA-Z ]", "").toLowerCase().split(" "))  
		  .map((_,1)).reduceByKey(_ + _)
		
		// filter out words with less than threshold occurrences
		val filtered = wordCounts.filter((tuple) =>{
		  (!dictionnary.contains(" "+tuple._1+" ")) && (!dictionnary.contains(" "+tuple._1+"s ")) && (!dictionnary.contains(" "+tuple._1+"ed ")) && tuple._1.length()<15
		})
		
		val ret = filtered
		    .collect
		    .sortBy(r=>r._2)
		    .takeRight(num+1)
		    .reverse
		    //Removing empty string
		    .drop(1)
		    .map(_._1)
		    
		
		spark.stop
		
		ret
		
  	}

}