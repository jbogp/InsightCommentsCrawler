package main.scala.sql

import java.sql.DriverManager
import java.sql.Connection
import com.typesafe.config.ConfigFactory
import scala.collection.mutable.ArrayBuffer
import java.sql.ResultSet

object MySQLConnector {
 
  
  private val conf = ConfigFactory.load()
  
  def getTopics(queue:String,num:Int):List[String] ={
   
    /*Getting the topics*/
    val topics:ResultSet = MySQLConnector.connection.createStatement()
          .executeQuery("SELECT topic FROM "+queue+" ORDER BY id DESC LIMIT "+num)
    
    val ret = new ArrayBuffer[String]
    
    while(topics.next()) {
      ret.append(topics.getString("topic"))
    }
    
    ret.toList.reverse
  }
  
  def setTweetsCount(counts:List[(String,Int)]) {
    MySQLConnector.connection.createStatement()
    .executeQuery(counts.reduceLeft((l,r)=>{
      (l._1+"UPDATE counts_tweets SET count="+r._2+" WHERE topic='"+r._1+"'",1)
    })._1)
  }
 
  private val getMysqlConfig: (String,String) = {
      new Tuple2(conf.getString("mysql.username"),conf.getString("mysql.password"))
  }

  // connect to the database named "insights" on the localhost
  val driver = "com.mysql.jdbc.Driver"
  val url = "jdbc:mysql://ip-172-31-13-232.us-west-1.compute.internal/Insight"
  val confmysql = getMysqlConfig

  // make the connection
  Class.forName(driver)
  val connection = DriverManager.getConnection(url, confmysql._1, confmysql._2)
  

}