package org.enterpriseaws;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

public class CreateUserResponse {

  @JsonSerialize(include = Inclusion.NON_NULL)
  private int status;

  @JsonSerialize(include = Inclusion.NON_NULL)
  private String err;

  @JsonSerialize(include = Inclusion.NON_NULL)
  private String accessKeyId;

  @JsonSerialize(include = Inclusion.NON_NULL)
  private String secretAccessKey;

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getErr() {
    return err;
  }

  public void setErr(String err) {
    this.err = err;
  }

  public String getAccessKeyId() {
    return accessKeyId;
  }

  public void setAccessKeyId(String accessKeyId) {
    this.accessKeyId = accessKeyId;
  }

  public String getSecretAccessKey() {
    return secretAccessKey;
  }

  public void setSecretAccessKey(String secretAccessKey) {
    this.secretAccessKey = secretAccessKey;
  }

}