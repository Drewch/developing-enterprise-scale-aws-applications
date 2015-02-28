package com.awsbenchmarks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Random;
import java.util.UUID;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

/**
 * Downloads and Uploads 100MB files from and to S3.
 * @author Adrian Petrescu
 * @author Andrew McNamara
 */
public class InternalNetworkIOBenchmark extends AwsBenchmark { 
  private static final String LARGE_FILE_DOWNLOAD_URL = "http://s3.amazonaws.com/aws-book/largefile";
  
  private static final String BUCKET_PREFIX = "aws-benchmark";
  private static final String LARGE_FILE_UPLOAD_KEY = "largefile";
  private static final int LARGE_FILE_UPLOAD_SIZE = 100 * 1024 * 1024;
  
  private TransferManager s3;
  private String s3TempBucketName;
  
  public InternalNetworkIOBenchmark(String instanceId, String instanceType) {
    super(instanceId, instanceType);
  }

  @Override
  public String getBenchmarkId() {
    return "internal-network";
  }
  
  /**
   * Read AWS Credentials and create an Amazon S3 Bucket
   */
  @Override
  public void setUp() throws IOException {
    super.setUp();
    
    s3 = new TransferManager(new PropertiesCredentials(
        this.getClass().getResourceAsStream("/AwsCredentials.properties")));
    s3TempBucketName = String.format("%s-%s", BUCKET_PREFIX, UUID.randomUUID().toString());
    s3.getAmazonS3Client().createBucket(s3TempBucketName);
  }
  
  @Override
  public void tearDown() {
    super.tearDown();
    
    s3.getAmazonS3Client().deleteObject(s3TempBucketName, LARGE_FILE_UPLOAD_KEY);
    s3.getAmazonS3Client().deleteBucket(s3TempBucketName);
    s3.shutdownNow();
  }
  
  /**
   * Download 100MB file from S3 and throw away the bytes as we download them
   * to prevent Disk I/O from bounding our download speed.
   * @throws IOException
   */
  private void performDownload() throws IOException {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(
        new URL(LARGE_FILE_DOWNLOAD_URL).openConnection().getInputStream()))) {
      
      while (in.readLine() != null) { }
    }
  }
  
  /**
   * Upload a 100MB file to S3
   * @throws IOException
   * @throws InterruptedException
   */
  private void performUpload() throws IOException, InterruptedException {
    byte[] tmp = new byte[LARGE_FILE_UPLOAD_SIZE];
    new Random().nextBytes(tmp);
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentLength(LARGE_FILE_UPLOAD_SIZE);
    
    Upload upload = s3.upload(
        s3TempBucketName,
        LARGE_FILE_UPLOAD_KEY,
        new ByteArrayInputStream(tmp),
        objectMetadata);
    upload.waitForCompletion();
  }

  @Override
  protected void performBenchmark() throws Exception {
    performDownload();
    performUpload();
  }
}
