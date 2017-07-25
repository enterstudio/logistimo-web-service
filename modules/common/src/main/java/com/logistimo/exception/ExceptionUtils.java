package com.logistimo.exception;

import com.logistimo.services.Resources;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 22/07/17.
 */
public class ExceptionUtils {

  private ExceptionUtils() {
  }

  /**
   * @return error message
   */
  public static String constructMessage(String code, Locale locale, Object[] params) {
    ResourceBundle
        errors =
        Resources.get().getBundle("errors", locale != null ? locale : Locale.ENGLISH);
    String message;
    try {
      message = errors.getString(code);
      if (params != null && params.length > 0) {
        return MessageFormat.format(message, params);
      } else if (StringUtils.isNotEmpty(message)) {
        return message;
      }
    } catch (Exception ignored) {
      // ignored
    }
    return code;
  }

}
