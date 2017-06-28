package com.logistimo.approvals.client.exceptions;

import com.logistimo.approvals.client.models.ErrorResponse;

import org.springframework.web.client.HttpClientErrorException;

/**
 * Created by charan on 23/06/17.
 */
public class ApprovalBadRequestException extends Exception {

  private final ErrorResponse response;

  public ApprovalBadRequestException(ErrorResponse response, HttpClientErrorException exception) {
    super(exception);

    this.response = response;
  }

  public ErrorResponse getResponse() {
    return response;
  }
}
