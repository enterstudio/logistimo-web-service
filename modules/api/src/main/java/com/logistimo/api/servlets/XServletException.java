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

package com.logistimo.api.servlets;

import com.logistimo.logger.XLog;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class XServletException extends ServletException {
  private ErrorCode errorCode;
  private int httpStatusCode;

  public XServletException(ErrorCode errorCode, Object... params) {
    this(HttpServletResponse.SC_BAD_REQUEST, errorCode, params);
  }

  public XServletException(int statusCode, ErrorCode errorCode, Object... params) {
    super(XLog.format(errorCode.template, params), XLog.getCause(params));
    this.errorCode = errorCode;
    this.httpStatusCode = statusCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public int getHttpStatusCode() {
    return httpStatusCode;
  }

  public static enum ErrorCode {
    WS_INVALID_RESOURCE("Resource {0} not available"),
    WS_INVALID_CONTENT_TYPE("Resource {0} does not support content type {1}"),
    WS_INVALID_METHOD("Resource {0} does not support method {1}"),
    WS_INVALID_PARAM("Resource {0} does not support parameter {1} in method {2}"),
    WS_INVALID_PARAM_TYPE("Invalid parameter type: resource {0} parameter {1} expected type {2}"),
    WS_INVALID_PARAM_VALUE("Invalid parameter value: resource {0} parameter {1} value {2}"),
    WS_MISSING_PARAM("Missing parameter: resource {0} parameter {1}");

    private String template;

    private ErrorCode(String msgTemplate) {
      this.template = msgTemplate;
    }

    public String getTemplate() {
      return template;
    }
  }
}
