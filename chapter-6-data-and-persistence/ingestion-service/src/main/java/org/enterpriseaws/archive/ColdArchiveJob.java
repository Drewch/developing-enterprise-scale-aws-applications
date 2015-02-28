package org.enterpriseaws.archive;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.enterpriseaws.archive.AwsClientPool.GLACIER;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import com.amazonaws.services.glacier.model.UploadArchiveRequest;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ColdArchiveJob implements Job {

  public void execute(JobExecutionContext context) throws JobExecutionException {

    // Get last update if there is one
    Date endDate = new Date(System.currentTimeMillis());
    Date lastUpdated = new Date(0L);
    if( !context.getJobDetail().getJobDataMap().isEmpty() ) {
      lastUpdated = (Date)context.getJobDetail().getJobDataMap().get("lastUpdated");
    }

    Writer writer = null;

    try {
        // WRite the data in order
        DBObject sort = new BasicDBObject();
        sort.put("timestamp", 1);
        DB db = MongoConnectionPool.ARCHIVE.getClient().getDB("archive");
        DBCollection warmArchive = db.getCollection("warmarchive");



        BasicDBObject query = new BasicDBObject();
        BasicDBObject lastDate = new BasicDBObject();
        lastDate.put("$gt", lastUpdated);
        query.put("timestamp", lastDate);
        DBCursor cursor = warmArchive.find(query).sort(sort);
        System.out.println("count is " + cursor.count());

        if( cursor.count() > 0 ) {
          writer = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream("tmpfile"), "utf-8"));
          while(cursor.hasNext()) {
            DBObject next = cursor.next();
            endDate = (Date) next.get("timestamp");
            writer.write(String.format("%s\n",next));
            context.getJobDetail().getJobDataMap().put("lastUpdated", endDate);
          }
          writer.close();

          File tmpfile = new File("tmpfile");
          String fnHolder = String.format("%s_%s.archive", lastUpdated, endDate);
          String filename = fn.replaceAll(" ", "_");
          File perm = new File(filename);
          tmpfile.renameTo(perm);

//          final ArchiveTransferManager atm = new ArchiveTransferManager(GLACIER.SYNCHRONOUS.getClient(), credentials);
//          final String archiveId = atm.upload("enterpriseAwsArchive", tmp.getName(), tmp).getArchiveId();
//          final String archiveId2 = atm.upload("enterpriseAwsArchive", perm.getName(), perm).getArchiveId();
          MessageDigest md = null;
          try {
            md = MessageDigest.getInstance("MD5");
          } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          try (InputStream is = Files.newInputStream(Paths.get(perm.toString()))) {
            DigestInputStream dis = new DigestInputStream(is, md);
            /* Read stream to EOF as normal... */
          }
          byte[] digest = md.digest();
          System.out.println("XXX ABOUT TO UPLOAD");
          GLACIER.SYNCHRONOUS.getClient().uploadArchive(new UploadArchiveRequest(
              "enterpriseAwsArchive",
              String.format("This cold archive is from %s to %s",lastUpdated, endDate),
              digest.toString(),
              Files.newInputStream((Paths.get(perm.toString())))
              ));
          System.out.println("UPLOADING");
//          System.out.println("The archive is " + archiveId);
//          System.out.println("The archive is2 " + archiveId2);
          cursor.close();
        }



    } catch (IOException ex) {
      // report
      ex.printStackTrace();
    }

    // take everything that is in the hot database and put it in the
    // warm database


  }

}