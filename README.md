#Insight Data Engineering project

## FlockingComments.com

### Motivation and description
Welcome to "FlockingComments", my Insight Data Engineering project. I'm a news addict, that's a fact, but the addiction goes deeper than this, I'm actually hooked on news comments as well...you know the comments section below every news article that makes you go "who the hell are these people commenting and how do they find the time?". Usually very poor in terms of content, often offensive, borederline useless and yet I spend hours reading them....

![Time spent reading the news](http://flockingcomments.com/img/graph.png)

The project is not in anyway attempting to cure this disease, but to save time while I waste my time. Aggregating every comment from a lot of news sources seperated by subject, updated at all time and providing the live stream of tweets concerning all the news subject on a single page, now that's the dream.

### Repository structure
The repository is structured in 3 independent branches corresponding to the 3 main parts of the application.
 - First off, the `master` branch contains all the ingredient that make up the backend data handling pipline. Fully written in Scala, in hanles crawling, comments fetching, data ingestion (with Kafka), batch queries (with Spark), streaming analysis (with Storm), and of course data storage mainly in Hbase for a source of truth and sometimes mySQL when convinient. (more details below)

 - The `InsightJsonAPI` branch contains a scala program based on Spray.io, which creates the views to the pipeline databases, web facing, the API serves JSON file to fetch comments, trends, or tweets from the pipeline. (more details in the brach's readme)

 - The `InsightClientApp` branch contains an AngularJS client accessible on (http://flockingcomments.com that queries and serves the Json API on a client machine. As it is static js/HTML, it can be deployed retty much on any server. (more details in the brach's readme)


### FlockingComments data pipline
I will describe here the contents of this `master` branch. It contains the main pieces of the data pipeline and most of the logic.

The classes are structured as follow

- src.main
  - `InsightCommentsCrawler`: Main class, separated in 3 sub programs that are ran by passing a different parameter: "RssCrawler", "InferTopics", "BatchLayer"
  - `CommentsFetcher`: Logic to fetch the comments from mulitple sources in a timely manner from the article links stored in Hbase, it utilizes heavily the classe in package `externalAPIs`
  - `Utils`: Object containing a few useful methods used throughout the program
  - `rss`
    - `RSSReader`: Logic to fetch and process the RSS feeds from different news sites, it also handles querying the Facebook API to get posts from journals official pages
    - `models`: Models (case classes) used in the RSS feed parsing to read/extract Json objects
  - `externalAPIs`: contains the logic to fetch extract and store the comments from Disqus/Facebook. It also contains the logic to fetch the Twitter Streaming API.
  - `hbase`: Logic to read/writes values from the Hbase Schema implemented (see below)
  - `kafka`: Connectors to read from and write to Kafka queues
  - `spark`: Classes utilizing Spark
    - `BatchQueries`: runs and stores distributed statistics about the comments, namely the most liked users across all site and the potential spam messages

    - `TopicsFinder`: uses Spark to compute the most recuring words in a set of titles either passe in memory or fetching a whole table in Hbase via an RDD

  - `sql`: Connector to mySQL used to store current values so they're easily queriable by the client API
  - `storm`: defines the topology created to filter and classify Tweets read from the Kafka queue, this class should be ran from the storm submitter.

  ### Hbase schema
  Hbase being the central datastore of this project, I'll give a few details concerning the schema used.

  #### article_links
  This table stores the article links as well as some meta information about each link (title, description)

  |                      | link1Hash                           | link2Hash                           | link3Hash                           | ... |
|----------------------|-------------------------------------|-------------------------------------|-------------------------------------|-----|
| Headline             | Json object (url,title,description) | Json object (url,title,description) | Json object (url,title,description) |     |
| (future) sports      | ...                                 | ...                                 |                                     |     |
| (future) Politics... |                                     |                                     |                                     |     |

#### comments

the comments are denormalized twice because I want to be able to query the comments by topics and by user. This is why I store them twice

the first table comments contains the comments by topic, which make querying a topic memory efficient because rows are stored consistently within one region in Hbase.

Each Json object making up every cell is an Array structure as `[{"comments":"the comment","from":"author","created_time":"comment_creation_time","likes_count":number_of_likes,"url":"url of origin"},{...},{...}]`

| row/column | URL1                                               | URL2                                               | URL3                                               | URL...                                             |
|------------|----------------------------------------------------|----------------------------------------------------|----------------------------------------------------|----------------------------------------------------|
| Topic1     | Json list containing all the comments for this URL |                                                    |                                                    | Json list containing all the comments for this URL |
| Topic2     |                                                    |                                                    |                                                    |                                                    |
| Topic3     |                                                    | Json list Containing all the comments for this URL |                                                    |                                                    |
| Topic...   |                                                    |                                                    | Json list Containing all the comments for this URL | Json list Containing all the comments              |



