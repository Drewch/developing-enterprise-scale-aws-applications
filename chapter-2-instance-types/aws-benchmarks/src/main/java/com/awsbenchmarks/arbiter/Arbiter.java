package com.awsbenchmarks.arbiter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.VirtualizationType;
import com.awsbenchmarks.driver.CliDriver;
/**
 * This class is meant to be an Arbiter for running AWS Benchmarks.
 * It will spin up instances and run the AWS Benchmark tests on them.
 * It can be passed in arguments to spin up any number of instances for
 * a certain instance type, or on all instance type if a type is 
 * not specified.
 * 
 * @author Andrew McNamara
 * @author Adrian Petrescu
 * 
 */

public class Arbiter {
  
  /* 
   *  AWS Constants - KEY_NAME does not exist by default on AWS accounts.
   *  It must be created manually from the EC2 Console for your AWS 
   *  account
   * 
   */
  private static final String KEY_NAME = "EnterpriseAws";
  private static final String SHUTDOWN_BEHAVIOUR = "terminate";
  private static final String DEFAULT_SECURITY_GROUP = "default";
  private static final String UBUNTU_12_04_AMI_ID = "ami-d0f89fb9";
  private static final String[] EC2_INSTANCE_TYPES = {
//      "m1.small",
//      "m1.medium",
//      "m1.large",
//      "m1.xlarge",
//      "m3.xlarge",
//      "m3.2xlarge",
//      "c1.medium",
//      "c1.xlarge",
//+      "cc2.8xlarge",
//      "m2.xlarge",
//      "m2.2xlarge",
//      "m2.4xlarge",
//+      "cr1.8xlarge",
//*      "hi1.4xlarge",
//*      "hs1.8xlarge",
 //     "t1.micro",
//+      "cg1.4xlarge"
  };
  
  private static int numInstancesPerInstanceType;
  private static String[] instanceTypes;
  private static String userData;
  private static AmazonEC2 ec2;

  /**
   * Start the benchmarking process by spinning up a number of ec2 instances of
   * either all of the ec2 instance types, or one instance type specified in args.
   * Each instance will then download a benchmarking jar file from S3 and run it.
   * The benchmarking jar will upload results to DynamoDB.
   * @param args        
   *      Usage: java arbiter <num_instances_per_instance_type> (<instance_type>)
   *             if <instance_type> is left out, it will run on all instance types.    
   */
  public static void main(String [] args) throws IOException {
    
    // Print usage if arguments are blank or is help
    if( (args.length == 0) || (args[0].equals("help")) ) {
      System.out.println("Usage: java arbiter <num_instances_per_instance_type> (<instance_type>)");
      System.out.println("\tif <instance_type> is left out, it will run on all instance types.");
    }
    
    System.out.println("Initializing...");
    
    // Initialize the configuration variables
    numInstancesPerInstanceType = Integer.parseInt(args[0]);
    if( args.length > 1 )
      instanceTypes = new String[] { args[1] };
    else
      instanceTypes = EC2_INSTANCE_TYPES;
    userData = createUserData();
    
    
    // Create EC2 Object
    Properties properties = new Properties();
    properties.load(CliDriver.class.getResourceAsStream("/driver.properties"));
    ec2 = new AmazonEC2Client(
        new PropertiesCredentials(Arbiter.class.getResourceAsStream("/AwsCredentials.properties")));
    
    // Set EC2 Region
    ec2.setRegion(Region.getRegion(Regions.US_EAST_1));
    
    System.out.println("Initialized.");
    
    // Make reservations for each instance type
    for( String instanceType : instanceTypes ) {
      
      System.out.println(String.format("Spinning up %d %s instances...", 
          numInstancesPerInstanceType, instanceType));
      
      // Configure Instance Request
      RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
        .withInstanceType(instanceType)
        .withImageId(UBUNTU_12_04_AMI_ID)
        .withMinCount(numInstancesPerInstanceType)
        .withMaxCount(numInstancesPerInstanceType)
        .withSecurityGroups(DEFAULT_SECURITY_GROUP) //production
        .withKeyName("infrastructure-production")
        .withInstanceInitiatedShutdownBehavior(SHUTDOWN_BEHAVIOUR)
        .withUserData(userData);
      
      // Spin up the instances
      RunInstancesResult runInstances = ec2.runInstances(runInstancesRequest);
      
      // Tag the instances
      List<Instance> instances = runInstances.getReservation().getInstances();
      int instanceNumber = 0;
      for (Instance instance : instances) {
        CreateTagsRequest createTagsRequest = new CreateTagsRequest();
        createTagsRequest.withResources(instance.getInstanceId())
            .withTags(new Tag("Name", String.format("EnterpriseAwsBenchmark: %s (%d)", instanceType, ++instanceNumber)));
        ec2.createTags(createTagsRequest);
      }
      
      System.out.println(String.format("%d %s instances up and running.", 
          numInstancesPerInstanceType, instanceType));
    } 
  }
  
  /**
   * This returns A Base 64 encoding of a shell file that does the following:
   *    - updates apt-get and installs java
   *    - downloads the benchmarking jar from S3
   *    - runs the benchmarking java
   *    - outputs log to /home/ubuntu/aws-benchmarks.log
   *    - shuts down the instance  
   * @return a Base 64 encoding of a shell script described above
   */
  private static String createUserData(){
    ArrayList<String> lines = new ArrayList<>();
    lines.add("#!/bin/bash");
    lines.add("apt-get update");
    lines.add("apt-get -y install openjdk-7-jdk");
    lines.add("wget http://aws-book.s3.amazonaws.com/aws-benchmark.jar -P /home/ubuntu");
    lines.add("java -jar /home/ubuntu/aws-benchmark.jar >> /home/ubuntu/aws-benchmarks.log");
    lines.add("shutdown now");
    String str = new String(Base64.encodeBase64(join(lines, "\n").getBytes()));
    return str;
  }
  
  /**
   * Builds a string from a collection of strings delimiting them 
   * with the provided delimiter.
   * @param s           a collection of strings
   * @param delimiter   a delimiter to be used to separate the strings
   * @return            a string containing each item from the collection
   *                    string s, delimited by delimiter.
   */
  static String join(Collection<String> s, String delimiter) {
    StringBuilder builder = new StringBuilder();
    Iterator<String> iter = s.iterator();
    while (iter.hasNext()) {
        builder.append(iter.next());
        if (!iter.hasNext()) {
            break;
        }
        builder.append(delimiter);
    }
    return builder.toString();
  }

}
