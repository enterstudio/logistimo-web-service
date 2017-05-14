package com.logistimo.api.response;

import java.io.Serializable;

public class BaseResponse implements Serializable {
  private static final long serialVersionUID = 651229732322637012L;

  public int statusCode;
  public String message;

  public BaseResponse() {
    this.statusCode = 200;
    this.message = "OK";
  }
}
