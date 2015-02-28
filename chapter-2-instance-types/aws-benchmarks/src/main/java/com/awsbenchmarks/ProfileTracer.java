package com.awsbenchmarks;

public class ProfileTracer {
  private long lastMarkedTime;

  public ProfileTracer(long startTime) {
    this.lastMarkedTime = startTime;
  }

  public ProfileTracer() {
    this(System.currentTimeMillis());
  }

  public long mark(long currentTime) {
    long lastTime = lastMarkedTime;
    this.lastMarkedTime = currentTime;

    return currentTime - lastTime;
  }

  public long mark() {
    return mark(System.currentTimeMillis());
  }

  public long getLastMarkedTime() {
    return this.lastMarkedTime;
  }

  public long getTimeSinceLastMarkedTime() {
    return System.currentTimeMillis() - lastMarkedTime;
  }
}
