package com.logistimo.exception;


/**
 * Created by Mohan Raja on 12/03/15
 */
@SuppressWarnings("serial")
public class InvalidServiceException extends RuntimeException {
  public InvalidServiceException(String message) {
    super(message);
  }

  public InvalidServiceException(Exception e) {
    super(e.getMessage(), e);
  }

  public InvalidServiceException(String message, Exception e) {
    super(message, e);
  }
}
