/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.exception;

import com.logistimo.services.Resources;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Exception handler based on error codes
 *
 * @author Mohan Raja
 */
public class LogiException extends Exception {
  private final transient Object[] arguments;
  private final String code;
  private int statusCode = 500;


  public LogiException(String code, Locale locale, Object... arguments) {
    super(constructMessage(code, locale, arguments));
    this.arguments = arguments;
    this.code = code;
  }

  public LogiException(String message) {
    super(message);
    arguments = new Object[0];
    code = null;
  }

  public LogiException(String message, Throwable t) {
    super(message, t);
    arguments = new Object[0];
    code = null;
  }

  public LogiException(String code, Throwable t, Object... arguments) {
    super(constructMessage(code, Locale.ENGLISH, arguments), t);
    this.code = code;
    this.arguments = arguments;
  }

  public LogiException(String code, Object... arguments) {
    super(constructMessage(code, Locale.ENGLISH, arguments));
    this.code = code;
    this.arguments = arguments;
  }

  public LogiException(Throwable t) {
    super(t);
    arguments = new Object[0];
    code = null;
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

  public String getCode() {
    return code;
  }

  public String getLocalisedMessage(Locale locale) {
    if (StringUtils.isNotBlank(code)) {
      return constructMessage(code, locale, arguments);
    }
    return getLocalizedMessage();
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }
}
