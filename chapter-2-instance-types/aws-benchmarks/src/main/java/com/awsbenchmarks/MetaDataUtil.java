package com.awsbenchmarks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MetaDataUtil {
  private static final String META_DATA_BASE_URL = "http://169.254.169.254/latest/meta-data";
  private static final String META_DATA_RESERVATION_ID_ATTRIBUTE = "reservation-id";
  private static final String META_DATA_INSTANCE_ID_ATTRIBUTE = "instance-id";
  private static final String META_DATA_INSTANCE_TYPE_ATTRIBUTE = "instance-type";
  
  private static String getMetadataAttributeId(String attribute) {
    try {
      URLConnection urlConnection = new URL(
          String.format("%s/%s", META_DATA_BASE_URL, attribute))
              .openConnection();
      urlConnection.setConnectTimeout(5 * 1000);
      urlConnection.setReadTimeout(5 * 1000);
      
      urlConnection.connect();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
        return in.readLine();
      }
    } catch (IOException ioe) {
      System.err.println("Can't access reservation-id, assuming localhost");
      return "localhost";
    }
  }

  public static String getReservationId() {
    return getMetadataAttributeId(META_DATA_RESERVATION_ID_ATTRIBUTE);
  }
  
  public static String getInstanceId() {
    return getMetadataAttributeId(META_DATA_INSTANCE_ID_ATTRIBUTE);
  }
  
  public static String getInstanceType() {
    return getMetadataAttributeId(META_DATA_INSTANCE_TYPE_ATTRIBUTE);
  }
  
}
