package com.logistimo.services;

import java.util.List;
import java.util.Locale;

/**
 * Created by charan on 27/10/16.
 */
public class UnauthorizedRequestException extends ServiceException {

  public UnauthorizedRequestException(String message) {
    super(message);
  }

  public UnauthorizedRequestException(String message, Throwable t) {
    super(message, t);
  }

  public UnauthorizedRequestException(String message, List<String> entityIds) {
    super(message, entityIds);
  }

  public UnauthorizedRequestException(String code, Locale locale, Object... arguments) {
    super(code, locale, arguments);
  }

  public UnauthorizedRequestException(String code, Object... arguments) {
    super(code, arguments);
  }

  public UnauthorizedRequestException(Throwable t) {
    super(t);
  }
}
