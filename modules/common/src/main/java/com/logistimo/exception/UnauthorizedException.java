package com.logistimo.exception;

import org.springframework.http.HttpStatus;

/**
 * Created by Mohan Raja on 12/03/15
 */

@SuppressWarnings("serial")
public class UnauthorizedException extends RuntimeException {

  private HttpStatus code;

  public UnauthorizedException(String message, HttpStatus code) {
    super(message);
    this.code = code;
  }

  public UnauthorizedException(String message) {
    super(message);
    this.code = HttpStatus.FORBIDDEN;
  }

  public HttpStatus getCode() {
    return code;
  }

  public void setCode(HttpStatus code) {
    this.code = code;
  }
}
