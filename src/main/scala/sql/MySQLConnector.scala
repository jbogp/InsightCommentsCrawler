package main.scala.sql

import java.sql.DriverManager
import java.sql.Connection

object MySQLConnector {

	// connect to the database named "insights" on the localhost
	val driver = "com.mysql.jdbc.Driver"
	val url = "jdbc:mysql://172.31.15.117/Insight"
	val username = "ubuntu"
	val password = ""

	// make the connection
	Class.forName(driver)
	val connection = DriverManager.getConnection(url, username, password)

}