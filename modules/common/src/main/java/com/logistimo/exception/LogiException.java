package com.logistimo.exception;

import com.logistimo.services.Resources;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Exception handler based on error codes
 *
 * @author Mohan Raja
 */
public class LogiException extends Exception {
  private String code;

  public LogiException(String code, Locale locale, Object... arguments) {
    super(constructMessage(code, locale, arguments));
    this.code = code;
  }

  public LogiException(String message) {
    super(message);
  }

  public LogiException(String message, Throwable t) {
    super(message, t);
  }

  public LogiException(String code, Throwable t, Object... arguments) {
    super(constructMessage(code, Locale.ENGLISH, arguments), t);
    this.code = code;
  }

  public LogiException(String code, Object... arguments) {
    super(constructMessage(code, Locale.ENGLISH, arguments));
    this.code = code;
  }

  public LogiException(Throwable t) {
    super(t);
  }

  /**
   * @return error message
   */
  private static String constructMessage(String code, Locale locale, Object[] params) {
    if (locale == null) {
      locale = Locale.ENGLISH;
    }
    ResourceBundle errors = Resources.get().getBundle("errors", locale);
    String message;
    try {
      message = errors.getString(code);
      if (params != null && params.length > 0) {
        return MessageFormat.format(message, params);
      }
    } catch (Exception ignored) {
      // ignored
    }
    return code;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
