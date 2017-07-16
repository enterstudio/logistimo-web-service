package com.logistimo.approvals.client.exceptions;

import com.logistimo.approvals.client.models.ErrorResponse;
import com.logistimo.exception.LogiException;

import org.springframework.web.client.HttpClientErrorException;

/**
 * Created by charan on 23/06/17.
 */
public class BadRequestException extends LogiException {

  private final ErrorResponse response;

  public BadRequestException(ErrorResponse response, HttpClientErrorException exception) {
    super(response.getCode(), exception, new Object[0]);
    this.setStatusCode(response.getStatusCode());
    this.response = response;
  }

  public ErrorResponse getResponse() {
    return response;
  }

  public String getMessage() {
    return response.getMessage();
  }
}
