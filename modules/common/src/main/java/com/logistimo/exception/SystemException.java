package com.logistimo.exception;

/**
 * Created by charan on 22/06/17.
 */
public class SystemException extends RuntimeException {
  public SystemException(Exception e) {
    super(e);
  }

  public SystemException(String message) {
    super(message);
  }

  public SystemException(String message, Exception e) {
    super(message, e);
  }
}
