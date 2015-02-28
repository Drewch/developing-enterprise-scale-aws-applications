package com.awsbenchmarks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
/**
 * Downloads an 18MB File from a speedtest.net server
 * @author Adrian Petrescu
 * @author Andrew McNamara
 *
 */
public class ExternalNetworkIOBenchmark extends AwsBenchmark {

  private static final String LARGE_FILE_DOWNLOAD_URL = "http://speedtest.xecu.net/speedtest/random3000x3000.jpg";
  private static final int NUM_ITERATIONS = 4;
  
  public ExternalNetworkIOBenchmark(String instanceId, String instanceType) {
    super(instanceId, instanceType);
  }

  @Override
  public String getBenchmarkId() {
    return "external-network";
  }

  /**
   * Downloads an 18MB file 4 times.
   * @throws IOException
   */
  private void doDownload() throws IOException {
    for (int i = 0; i < NUM_ITERATIONS; i++) {
      try (BufferedReader in = new BufferedReader(new InputStreamReader(
          new URL(LARGE_FILE_DOWNLOAD_URL).openConnection().getInputStream()))) {
        
        while (in.readLine() != null) { }
      } 
    }
  }
  
  @Override
  protected void performBenchmark() throws Exception {
    doDownload();
  }

}
