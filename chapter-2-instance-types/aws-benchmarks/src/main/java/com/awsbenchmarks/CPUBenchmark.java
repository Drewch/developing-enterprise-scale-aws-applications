package com.awsbenchmarks;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.ec2.model.InstanceType;

/**
 * This Benchmark calculates a fibonacci sequence up to 45
 * through recursion. A thread is created for each CPU on
 * the system. The time recorded is the time from starting
 * the test until all CPU's are finished.
 *
 * @author Andrew McNamara
 * @author Adrian Petrescu
 */
public class CPUBenchmark extends AwsBenchmark {

  private static final int NUM_ITERATIONS = 8;
  private ExecutorService e;

  CPUBenchmark(String instanceId, String instanceType) {
    super(instanceId, instanceType);
  }

  /**
   * Calculates the numbers of the fibonacci sequence.
   * @param n the max number of fibonacci sequence
   * to calculate
   * @return the numbers of the fibonacci sequence
   */
  private static int fibonacci(int n) {
    if( n < 2 ) {
      return n;
    } else {
      return fibonacci(n-1) + fibonacci(n-2);
    }
  }

  @Override
  protected void setUp() throws IOException {
    super.setUp();

    e = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors(),
        new ThreadFactory() {
          @Override
          public Thread newThread(Runnable r) {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            return thread;
          }
        });
  }

  @Override
  public String getBenchmarkId() {
    return "cpu";
  }

  @Override
  protected void performBenchmark() throws Exception {
    for (int i = 0; i < NUM_ITERATIONS; i++) {
      e.execute(new Runnable() {
        public void run() {
          fibonacci(45);
        }
      });
    }
    e.shutdown();
    e.awaitTermination(60, TimeUnit.MINUTES);
  }
}

