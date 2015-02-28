package org.enterpriseaws.archive;

import java.net.UnknownHostException;

import com.mongodb.MongoClient;

public enum MongoConnectionPool {
  ARCHIVE;
  
  private static final String dbHost = "localhost";
  private static final int dbPort = 27017;
  private static MongoClient mongoClient;
  
  static { 
    try {
      mongoClient = new MongoClient(dbHost, dbPort);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } 
  }
  
  public MongoClient getClient() {
    return mongoClient;
  }

}
