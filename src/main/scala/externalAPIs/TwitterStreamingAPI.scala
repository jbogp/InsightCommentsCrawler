package externalAPIs

import com.typesafe.config.ConfigFactory
import twitter4j._
import twitter4j.conf.{ConfigurationBuilder, Configuration}

class OnTweetPosted(cb: Status => Unit) extends StatusListener {	
    override def onStatus(status: Status): Unit = cb(status)
    override def onException(ex: Exception): Unit = throw ex

    // no-op for the following events
    override def onStallWarning(warning: StallWarning): Unit = {}
    override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}
    override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}
    override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}
}

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