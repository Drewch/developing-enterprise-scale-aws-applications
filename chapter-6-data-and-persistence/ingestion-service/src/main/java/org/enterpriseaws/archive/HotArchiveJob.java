package org.enterpriseaws.archive;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bson.BSONObject;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.enterpriseaws.archive.AwsClientPool.SQS;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class HotArchiveJob implements Job {

  private final String ARCHIVE_QUEUE = "https://sqs.us-east-1.amazonaws.com/869186500505/archive-server";
  private final Integer MAX_MESSAGES = 5;

  public void execute(JobExecutionContext context) throws JobExecutionException {

    // Read from the SQS queue
    ReceiveMessageRequest sqsRequest = new ReceiveMessageRequest(ARCHIVE_QUEUE);
    sqsRequest.setMaxNumberOfMessages(MAX_MESSAGES);
    ReceiveMessageResult sqsResult = SQS.SYNCHRONOUS.getClient().receiveMessage(sqsRequest);
    List<Message> messageList = sqsResult.getMessages();

    // Connect to MongoDB
    DB db = MongoConnectionPool.ARCHIVE.gwetClient().getDB("archive");
    DBCollection collection = db.getCollection("hotarchive");


    for(Message m : messageList) {

      // put in MongoDB
      HashMap<String,Object> result = new HashMap<String,Object>();
      try {
        result = new ObjectMapper().readValue(m.getBody(), HashMap.class);
      } catch (JsonParseException e) {
        e.printStackTrace();
      } catch (JsonMappingException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      Date timestamp = new Date((Long)result.get("timestamp")); //java.util.date
      result.put("timestamp", timestamp);
      BasicDBObject mongoObject = new BasicDBObject();
      mongoObject.putAll(result);
      WriteResult writeResult = collection.save(mongoObject);

      if(writeResult.getError() == null) {
        SQS.SYNCHRONOUS.getClient().deleteMessage(new DeleteMessageRequest(
          ARCHIVE_QUEUE,
          m.getReceiptHandle()
          ));
      }

    }



  }

}