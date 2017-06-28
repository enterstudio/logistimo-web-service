package com.logistimo.exception;

/**
 * Created by charan on 22/06/17.
 */
public class ValidationException extends LogiException {

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable t) {
    super(message, t);
  }

  public ValidationException(String code, Throwable t, Object... arguments) {
    super(code, t, arguments);
  }

  public ValidationException(String code, Object... arguments) {
    super(code, arguments);
  }

  public ValidationException(Throwable t) {
    super(t);
  }
}
