package externalAPIs

import com.typesafe.config.ConfigFactory
import twitter4j._
import twitter4j.conf.{ConfigurationBuilder, Configuration}
import main.scala.kafka.KafkaProducer
import net.liftweb.json.JArray
import net.liftweb.json.JObject
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.Serialization
import net.liftweb.json.NoTypeHints
import net.liftweb.json.JValue
import net.liftweb.json.Extraction

class OnTweetPosted(cb: Status => Unit) extends StatusListener {	
    override def onStatus(status: Status): Unit = cb(status)
    override def onException(ex: Exception): Unit = throw ex

    // no-op for the following events
    override def onStallWarning(warning: StallWarning): Unit = {}
    override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}
    override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}
    override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}
}

case class TweetToJSONToKafka(kafkaProducer:KafkaProducer) {
  
	implicit val formats = Serialization.formats(NoTypeHints)
	
	/* Handling new status to get JSON*/
	def statusHandler(cb: Status):Unit = {
			/*Getting Latitude/Longitude*/
			val latlong:(Double,Double) = {
					cb.getGeoLocation() match {
						/* If not provided, north pole */
					  	case null => {
					  		new Tuple2(90.0,0.0)
					  	}
					  	case _ => {
					  		new Tuple2(cb.getGeoLocation().getLatitude(),cb.getGeoLocation().getLongitude())
					  	}
					}
			}
			/*Getting a Tweet object in order to serialize later*/
			val tweet = new Tweet(
			    cb.getText(),
			    cb.getCreatedAt().getTime(),
			    latlong._1,
			    latlong._2,
			    cb.getId(),
			    cb.getRetweetCount(),
			    cb.getUser().getName(),
			    cb.getUser().getProfileImageURL(),
			    cb.getUser().getURL()
			)
			println(tweet.from)
			/*Serialize and send to kafka*/
			kafkaProducer.send(write(tweet), null)
	}
}

/*Case class for Tweet Message*/
case class Tweet(message:String,createdAt:Long,latitude:Double,longitude:Double,id:Long,rt_count:Int,from:String,from_pic:String,from_url:String)

object TwitterStreamingAPI {
  
	private val conf = ConfigFactory.load()
  
    private val getTwitterConf: Configuration = {
	    val twitterConf = new ConfigurationBuilder()
	      .setOAuthConsumerKey(conf.getString("twitter.consumerKey"))
	      .setOAuthConsumerSecret(conf.getString("twitter.consumerSecret"))
	      .setOAuthAccessToken(conf.getString("twitter.accessToken"))
	      .setOAuthAccessTokenSecret(conf.getString("twitter.accessTokenSecret"))
	      .build()
	    twitterConf
	}
	
	def getStream = new TwitterStreamFactory(getTwitterConf).getInstance()
	

	

}