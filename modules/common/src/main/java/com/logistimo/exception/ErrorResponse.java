package com.logistimo.exception;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.springframework.web.client.HttpClientErrorException;

/**
 * Created by charan on 23/06/17.
 */
public class ErrorResponse {

  @SerializedName("status_code")
  private int statusCode;

  private String code;

  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public static ErrorResponse getErrorResponse(HttpClientErrorException exception) {
    return new Gson().fromJson(exception.getResponseBodyAsString(), ErrorResponse.class);
  }
}
