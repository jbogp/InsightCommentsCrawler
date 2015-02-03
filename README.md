#Insight Data Engineering project

## FlockingComments.com

### Motivation and description
Welcome to "FlockingComments", my Insight Data Engineering project. I'm a news addict, that's a fact, but the addiction goes deeper than this, I'm actually hooked on news comments as well...you know the comments section below every news article that makes you go "who the hell are these people commenting and how do they find the time?". Usually very poor in terms of content, often offensive, borederline useless and yet I spend hours reading them....

The project is not in anyway attempting to cure this disease, but to save time while I waste my time. Aggregating every comment from a lot of news sources seperated by subject, updated at all time and providing the live stream of tweets concerning all the news subject on a single page, now that's the dream.

### Repository structure
The repository is structured in 3 independent branches corresponding to the 3 main parts of the application I built.
 - First off, the `master` branch contains all the ingredient that make up the backend data handling pipline. Fully written in Scala, in hanles crawling, comments fetching, data ingestion (with Kafka), batch queries (with Spark), streaming analysis (with Storm), and of course data storage mainly in Hbase for a source of truth and sometimes mySQL when convinient. (more details below)

 - The `InsightJsonAPI` branch contains a scala program based on Spray.io, which creates the views to the pipeline databases, web facing, the API serves JSON file to fetch comments, trends, or tweets from the pipeline. (more details in the brach's readme)

 - The `InsightClientApp` branch contains an AngularJS client accessible on (http://flockingcomments.com that queries and serves the Json API on a client machine. As it is static js/HTML, it can be deployed retty much on any server. (more details in the brach's readme)


### FlockingComments data pipline


