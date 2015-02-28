package com.awsbenchmarks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;

/**
 * The Aws Benchmark abstract class can be extended to run
 * any type of AWS Benchmark that is required. After the
 * benchmark is performed, the results will be put onto
 * DynamoDB into the table AwsBenchmarks.
 * @author Adrian Petrescu
 * @author Andrew McNamara
 */
public abstract class AwsBenchmark {
  private static final String RESULTS_TABLE = "AwsBenchmarks";
  private AmazonDynamoDB dynamo;
  
  protected String instanceId;
  protected String instanceType;

  /**
   * Constructor for AwsBenchmark, sets the instance id and instance
   * type of the aws instance it is running on.
   * @param instanceId    the id of the aws instance running the test
   * @param instanceType  the type of aws instance running the test
   */ 
  public AwsBenchmark(String instanceId, String instanceType) {
    this.instanceId = instanceId;
    this.instanceType = instanceType;
  }
  
  /**
   * Set up AWS Benchmark
   * @throws IOException
   */
  protected void setUp() throws IOException {
    dynamo = new AmazonDynamoDBClient(
        new PropertiesCredentials(this.getClass().getResourceAsStream("/AwsCredentials.properties")));
  }
  
  protected void tearDown() {
    // Do nothing
  }
  
  /**
   * Get the id of the benchmark test
   * @return the id of the benchmark test
   */
  public abstract String getBenchmarkId();
  
  /**
   * This implements the AWS benchmark test that is to be
   * run on the instance.
   * @throws Exception
   */
  protected abstract void performBenchmark() throws Exception;
  
  /**
   * Execute the benchmark implemented in performBenchmark()
   * and report the results to DynamoDB. Return the time elapsed
   * to run the test.
   * @return time it took to run the benchmark in milliseconds
   * @throws Exception
   */
  public long execute() throws Exception {
    setUp();
    
    ProfileTracer tracer = new ProfileTracer();
    long timeElapsed = 0L;
    
    try {
      performBenchmark();
      reportResults(true, timeElapsed = tracer.mark());
    } catch (Exception e) {
      reportResults(false, timeElapsed = tracer.mark());
      throw e;
    } finally {
      tearDown();
    }
    
    return timeElapsed;
  }
  
  /**
   * Report the benchmark results to DynamoDB
   * @param success whether or not the benchmark completed successfully or not
   * @param benchmarkTime the amount of time in milliseconds that the benchmark 
   * took to complete
   */
  private void reportResults(boolean success, long benchmarkTime) {
    Map<String, AttributeValue> key = new HashMap<>(2);
    Map<String, AttributeValueUpdate> item = new HashMap<>(5);
    
    key.put("reservationId",  new AttributeValue(MetaDataUtil.getReservationId()));
    key.put("instanceId",     new AttributeValue(instanceId));
    
    item.put(
        "instanceType",
        new AttributeValueUpdate(
            new AttributeValue(instanceType),
            AttributeAction.PUT));
    item.put(
        getBenchmarkId(),
        new AttributeValueUpdate(
            new AttributeValue().withN(String.valueOf(benchmarkTime)),
            AttributeAction.PUT));
    
    dynamo.updateItem(new UpdateItemRequest(RESULTS_TABLE, key, item));
  }
  
  @Override
  public String toString() {
    return String.format("[%s benchmark]", getBenchmarkId());
  }
}
