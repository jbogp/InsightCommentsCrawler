/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package main.scala.kafka

import kafka.message._
import kafka.serializer._
import kafka.utils._
import java.util.Properties
import kafka.utils.Logging
import scala.collection.JavaConversions._
import kafka.consumer.Whitelist
import kafka.consumer.Consumer
import kafka.consumer.ConsumerConfig
import java.util.Calendar


class KafkaConsumer (topic: String, groupId: String, zookeeperConnect: String, readFromStartOfStream: Boolean = true, timestamp:Long=0L) extends Logging {
 
 val props = new Properties()
 props.put("group.id", groupId)
 props.put("zookeeper.connect", zookeeperConnect)
 props.put("auto.offset.reset", if(readFromStartOfStream) "smallest" else "largest")

 val config = new ConsumerConfig(props)
 val connector = Consumer.create(config)

 val filterSpec = new Whitelist(topic)

 info("setup:start topic=%s for zk=%s and groupId=%s".format(topic,zookeeperConnect,groupId))
 val stream = connector.createMessageStreamsByFilter(filterSpec, 1, new DefaultDecoder(), new DefaultDecoder()).get(0)
 info("setup:complete topic=%s for zk=%s and groupId=%s".format(topic,zookeeperConnect,groupId))
 


 def read(write: (Array[Byte])=>Unit) = {
  info("reading on stream now")
  for(messageAndTopic <- stream) {
   try {
    write(messageAndTopic.message)
    if(Calendar.getInstance().getTimeInMillis()-timestamp >= 60000L){
    	this.close
    }
    
   } catch {
    case e: Throwable =>
     if (true) { //this is objective even how to conditionalize on it
      error("Error processing message, skipping this message: ", e)
     } else {
      throw e
     }
   }
  }   
 }

 def close() {
  connector.shutdown()
 }
}