package com.awsbenchmarks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

import com.amazonaws.services.ec2.model.InstanceType;

/**
 * Runs a test to calculate the amount of time to write and
 * read to disk.
 * Write will be done both buffered and unbuffered.
 * @author Adrian Petrescu
 * @author Andrew McNamara
 *
 */
public class DiskIOBenchmark extends AwsBenchmark {
  
  public DiskIOBenchmark(String instanceId, String instanceType) {
    super(instanceId, instanceType);
  }

  private static final int NUM_SEQUENTIAL_BLOCKS = 1024 * 1024 * 5;
  private static final int NUM_RANDOM_ACCESS_BLOCKS = 1024 * 5;
  
  /**
   * Runs a buffered write test by writing 5MB of sequential data
   * to disk.
   * @throws IOException
   */
  private static void performBufferedSequentialIOTest() throws IOException {
    File tmpFile = File.createTempFile("buffered-sequential", ".tmp");
    try (BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile))) {
      for (int block = 0; block < NUM_SEQUENTIAL_BLOCKS; block++) {
        out.write(block);
      }
    } finally {
      tmpFile.delete();
    }
  }
  
  /**
   * Runs an unbuffered write test by writing 5MB of sequential data
   * to disk.
   * @throws IOException
   */
  private static void performUnbufferedSequentialIOTest() throws IOException {
    File tmpFile = File.createTempFile("unbuffered-sequential", ".tmp");
    try (FileWriter out = new FileWriter(tmpFile)) {
      for (int block = 0; block < NUM_SEQUENTIAL_BLOCKS; block++) {
        out.write(block);
      }
    } finally {
      tmpFile.delete();
    }
  }
  
  /**
   * Performs a write test accessing bytes randomly across a 5MB buffer.
   * This is to counteract potential buffering done at the VM layer.
   * 
   * @throws IOException
   */
  private static void performRandomAccessIOTest() throws IOException {
    File tmpFile = File.createTempFile("unbuffered-random-access", ".tmp");
    try (RandomAccessFile out = new RandomAccessFile(tmpFile, "rws")) {
      // Use a fixed seed, for consistency
      Random r = new Random(0xcafebabe);
      for (int i = 0; i < NUM_RANDOM_ACCESS_BLOCKS; i++) {
        long n = r.nextLong();
        out.seek(n % NUM_RANDOM_ACCESS_BLOCKS + ((n < 0) ? NUM_RANDOM_ACCESS_BLOCKS : 0));
        out.write(i);
      }
    } finally {
      tmpFile.delete();
    }
  }

  @Override
  public String getBenchmarkId() {
    return "disk";
  }

  /**
   * Run the IO tests: buffered write, unbuffered write,
   * random access read.
   */
  @Override
  protected void performBenchmark() throws Exception {
    performBufferedSequentialIOTest();
    performUnbufferedSequentialIOTest();
    performRandomAccessIOTest();
  }
}
