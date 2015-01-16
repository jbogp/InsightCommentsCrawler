package main.scala.sql

import java.sql.DriverManager
import java.sql.Connection

object MySQLConnector {

	// connect to the database named "insights" on the localhost
	val driver = "com.mysql.jdbc.Driver"
			val url = "jdbc:mysql://localhost/insights"
			val username = "jbpettit"
			val password = ""

			// there's probably a better way to do this
			var connection:Connection = null

			try {
				// make the connection
				Class.forName(driver)
				connection = DriverManager.getConnection(url, username, password)

				// create the statement, and run the select query
				val statement = connection.createStatement()
				val resultSet = statement.executeQuery("SELECT * FROM journals")
				while ( resultSet.next() ) {
					val id = resultSet.getString("id")
							val rss = resultSet.getString("rss")

				}
			} catch {
			case e: Throwable => e.printStackTrace
			}
	connection.close()
	val token = "CAAEalESiyf8BAAwZAV41UsUveQa0KXadoPmLV5VtK9BDI11LhWAlYX8Ob88ZAj57eNKkQv0TcsyhhtCDvuCyolkCcdJ91no1wZCze0Iy90F0YfEQxZAeBAQhPVp2648Nv0svB8USKdlvlRj2RzxDaAWCXmVvEehlZCtiyJzGXNVwVF7k96eZAThQSp79KRPkZC5zZAZCOECfXVAT5cseDOZBUM"
	val urlAPI = "https://graph.facebook.com/v2.1/?fields=og_object{comments}&access_token="+token+"&id=http://latimesblogs.latimes.com/lanow/2011/08/kelly-thomas-beating-death-1.html#comments"
	val result = scala.io.Source.fromURL(urlAPI).mkString
	println(result)


}