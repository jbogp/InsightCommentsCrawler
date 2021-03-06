
package main.scala.hbase


import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.HConnectionManager
import org.apache.hadoop.conf.Configuration
import net.liftweb.json.Serialization.{read, write}
import org.apache.hadoop.hbase.util.Bytes
import java.security.MessageDigest
import java.util.Calendar
import org.apache.hadoop.hbase.HBaseConfiguration
import externalAPIs.Tweet
import net.liftweb.json.Serialization
import net.liftweb.json.NoTypeHints
import scala.collection.mutable.ArrayBuffer
import main.scala.rss.Comment

/*Some case classes*/
case class UserComment(url:String,message:String,title:String,like_count:Int,created_at:String)

/*Stores and reads values in the Hbase*/
case class WriteToHbase() {
  
 
  /*Creating configuration and connecting*/
  val conf = HBaseConfiguration.create()
  conf.clear();
  conf.set("hbase.zookeeper.quorum", "ip-172-31-11-73.us-west-1.compute.internal");
  conf.set("hbase.zookeeper.property.clientPort","2181");
  conf.set("hbase.master", "ip-172-31-11-73.us-west-1.compute.internal:60000");
  
  implicit val formats = Serialization.formats(NoTypeHints)
  
  /*Generic function to insert in HBase*/
  def insert[T](table:String, rowkey:String, familly:String, column:Array[String], value:Array[T], convertToBytes:(T)=>Array[Byte], overwrite:Boolean=true) {
    /*Fetch the table*/
    val httable = new HTable(conf, table)
    
    /*Adding the columns*/
    (column,value).zipped.foreach((col,valueTowrite) => {
      /*Defining the rowkey*/
      val theput= new Put(Bytes.toBytes(rowkey))
      theput.add(Bytes.toBytes(familly),Bytes.toBytes(col),convertToBytes(valueTowrite))
      if(overwrite)
        httable.put(theput)
      else
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
           insert[String]("article_links",row,"infos",columns.take(3),values.take(3),s => Bytes.toBytes(s),false)
           insert[String]("article_links",row,"contents",columns.takeRight(2),values.takeRight(2),s => Bytes.toBytes(s),false)
           true
         }
         case true => false
      }
  }
  
  /* 
   * Insert comments in Hbase
   */
  def insertComments(values:Array[String],comments:ArrayBuffer[Comment],topics1h:Array[String],topics12h:Array[String],topicsAllTime:Array[String]) {
    val alltopics = topicsAllTime++topics1h++topics12h
    
    val url = values(0)
    val titleStr = values(2)
    val commentsJson = write(comments)
      
    /*Writing on comments table*/
    val title = (values(1)+values(2)).replaceAll("[^a-zA-Z ]", "").toLowerCase().split(" ").filter(_.length()<15).drop(1)
    title.foreach(word =>{
      if((topicsAllTime++topics1h++topics12h).contains(word)) {
        insert[String]("comments",word,"infos",Array(url),Array(commentsJson),s => Bytes.toBytes(s))
        println(word)
      }
    })

    /*Writing on users table*/
    comments.foreach(comment =>{
        val col = titleStr+comment.created_time
        val row = comment.from.replaceAll("[^a-zA-Z0-9]", "")
        val jsonUser = write(new UserComment(url,comment.message,col,comment.like_count,comment.created_time))
        insert[String]("users",row,"infos",Array(col),Array(jsonUser),s => Bytes.toBytes(s))
    })
  }
  
  
  /* 
   * Insert Tweets in Hbase
   * returns the topics where the tweet was inserted
   */
  def insertTweets(tweet:Tweet,topics:Array[String]): List[String]= {
      val column = tweet.id.toString
      
      /*Writing on tweets tables*/
      val content = tweet.message
        .replaceAll("[^a-zA-Z ]", "")
        .toLowerCase()
        .split(" ")
        .filter(_.length()<15)
        .drop(1)
        
      /* Creating the return value */
      val ret = List[String]()

      val in = content.foreach(word =>{
        if((topics).contains(word)) {
          insert[String]("tweets",word,"infos",Array(column),Array(write(tweet)),s => Bytes.toBytes(s))
          ret:::(List(word))
        }
      })
      ret
  }
  
  

  
}
