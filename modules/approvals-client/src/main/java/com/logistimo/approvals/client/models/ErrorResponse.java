package com.logistimo.approvals.client.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

/**
 * Created by charan on 23/06/17.
 */
public class ErrorResponse {

  private Long timestamp;

  private int status;

  private String error;

  private String exception;

  private String message;

  private String path;

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getException() {
    return exception;
  }

  public void setException(String exception) {
    this.exception = exception;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public static ErrorResponse getErrorResponse(HttpClientErrorException exception) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(exception.getResponseBodyAsString(), ErrorResponse.class);
    } catch (IOException e) {
      return new ErrorResponse();
    }
  }
}
