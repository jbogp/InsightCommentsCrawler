package main.scala.sql

import java.sql.DriverManager
import java.sql.Connection
import com.typesafe.config.ConfigFactory

object MySQLConnector {
  
   
	private val conf = ConfigFactory.load()
  
    private val getMysqlConfig: (String,String) = {
			new Tuple2(conf.getString("mysql.username"),conf.getString("mysql.password"))
	}

	// connect to the database named "insights" on the localhost
	val driver = "com.mysql.jdbc.Driver"
	val url = "jdbc:mysql://ip-172-31-15-117.us-west-1.compute.internal/Insight"
	val confmysql = getMysqlConfig

	// make the connection
	Class.forName(driver)
	val connection = DriverManager.getConnection(url, confmysql._1, confmysql._2)

}