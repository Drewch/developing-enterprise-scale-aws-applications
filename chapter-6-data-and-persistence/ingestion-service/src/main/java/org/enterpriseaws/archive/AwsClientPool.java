package org.enterpriseaws.archive;

import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class AwsClientPool {
  
  private static AWSCredentials credentials;

  static {
    try {
      credentials = new PropertiesCredentials(HotArchiveJob.class.getResourceAsStream("/AwsCredentials.properties"));
    } catch (IOException ioe) {
      throw new RuntimeException("Could not load AwsCredentials.properties on classpath!");
    }
  }
  
  public static enum SQS {
    SYNCHRONOUS;

    private AmazonSQS client;

    private SQS() {
      client = new AmazonSQSClient(credentials);
    }

    public AmazonSQS getClient() {
      return client;
    }
  }
  
  public static enum GLACIER {
    SYNCHRONOUS;

    private AmazonGlacierClient client;

    private GLACIER() {
      client = new AmazonGlacierClient(credentials);
    }

    public AmazonGlacierClient getClient() {
      return client;
    }
  }
}
