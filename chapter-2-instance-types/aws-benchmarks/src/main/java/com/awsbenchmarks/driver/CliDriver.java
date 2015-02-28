package com.awsbenchmarks.driver;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.awsbenchmarks.AwsBenchmark;
import com.awsbenchmarks.MetaDataUtil;

public class CliDriver {
  
  public static void main(String [] args) throws Exception {
    Properties properties = new Properties();
    properties.load(CliDriver.class.getResourceAsStream("/driver.properties"));
    
    List<AwsBenchmark> benchmarks = new LinkedList<>();
    for (String benchmarkClass : properties.getProperty("benchmarkClasses", "").split(",")) {
      try {
        benchmarks.add((AwsBenchmark) Class.forName(benchmarkClass)
            .getConstructor(String.class, String.class)
            .newInstance(MetaDataUtil.getInstanceId(), MetaDataUtil.getInstanceType()));
      } catch (ClassNotFoundException cnfe) {
        System.err.println(String.format("Couldn't load benchmark class %s, skipping...", benchmarkClass));
      }
    }
    
    System.out.println("Loaded benchmarks: " + benchmarks);
    
    System.out.println("\n==================BEGINNING BENCHMARK SUITE====================");
    for (AwsBenchmark benchmark : benchmarks) {
      System.out.println(String.format("\n------------------BEGINNING %s BENCHMARK--------------------", benchmark.getBenchmarkId()));
      long timeElapsed = 0L;
      try {
        timeElapsed = benchmark.execute();

        System.out.println("  Success: true");
        System.out.println("  Time elapsed: " + timeElapsed);
      } catch (Exception e) {
        System.out.println("  Success: false");
        System.out.println("  Time elapsed: " + timeElapsed);
      }
      System.out.println(String.format("------------------COMPLETED %s BENCHMARK--------------------", benchmark.getBenchmarkId()));
    }
    System.out.println("\n==================COMPLETED BENCHMARK SUITE====================");
  }
}
