package org.enterpriseaws.archive;

import java.util.Date;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@PersistJobDataAfterExecution
public class WarmArchiveJob implements Job {

  public void execute(JobExecutionContext context) throws JobExecutionException {

    Date lastUpdated = new Date(0L); // start at the very beginning
    // Get the last update if there is one
    if( context.getJobDetail().getJobDataMap().size() != 0 ) {
      lastUpdated = (Date)context.getJobDetail().getJobDataMap().get("lastUpdated");
    }
   
    // Get the collections
    DB db = MongoConnectionPool.ARCHIVE.getClient().getDB("sqstest");
    DBCollection hotArchive = db.getCollection("hotarchive");
    DBCollection warmArchive = db.getCollection("warmarchive");
    
    // Query and Sort by date
    BasicDBObject query = new BasicDBObject();
    BasicDBObject lastDate = new BasicDBObject();
    lastDate.put("$gt", lastUpdated);
    query.put("timestamp", lastDate);
    DBObject sort = new BasicDBObject();
    sort.put("timestamp", 1); 
    DBCursor cursor = hotArchive.find(query).sort(sort);
    
    // Get all the results and put them in the warm archive
    while(cursor.hasNext()) {
      DBObject next = cursor.next();
      Date lastTimestamp = (Date) next.get("timestamp");
      warmArchive.save(next);
      context.getJobDetail().getJobDataMap().put("lastUpdated", lastTimestamp);
    }
    cursor.close();


  }

}