import AssemblyKeys._ // put this at the top of the file

assemblySettings

jarName in assembly := "something.jar"

test in assembly := {}

mainClass in assembly := Some("main.scala.InsightCommentsCrawler")