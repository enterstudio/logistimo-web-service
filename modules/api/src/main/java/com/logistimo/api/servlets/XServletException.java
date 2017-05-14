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
