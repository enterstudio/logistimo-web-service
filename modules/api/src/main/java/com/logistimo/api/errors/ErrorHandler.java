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

package com.logistimo.api.errors;

import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.exception.BadRequestException;
import com.logistimo.exception.ConfigurationServiceException;
import com.logistimo.exception.ErrorResource;
import com.logistimo.exception.ExceptionUtils;
import com.logistimo.exception.ExceptionWithCodes;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.InvalidTaskException;
import com.logistimo.exception.LogiException;
import com.logistimo.exception.SystemException;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.exception.ValidationException;
import com.logistimo.logger.XLog;
import com.logistimo.security.BadCredentialsException;
import com.logistimo.security.UserDisabledException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.netflix.hystrix.exception.HystrixBadRequestException;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Locale;

/**
 * Created by Mohan Raja on 12/03/15
 */

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

  private static final XLog XLOGGER = XLog.getLog(ErrorHandler.class);
  public static final String GENERAL_SYSTEM_ERROR = "G001";


  @ExceptionHandler({InvalidServiceException.class})
  protected ResponseEntity<Object> handleInvalidServiceRequest(RuntimeException e,
                                                               WebRequest request) {
    logWarning(request, e);
    ErrorResource error = new ErrorResource("[Internal Server Error]", e.getMessage());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return handleExceptionInternal(e, error, headers, HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler({UnauthorizedException.class})
  protected ResponseEntity<Object> handleUnauthorizedRequest(RuntimeException e,
                                                             WebRequest request) {
    logWarning(request, e);
    String message = e.getMessage();
    if (StringUtils.isBlank(message)) {
      message = ExceptionUtils.constructMessage("G002",
          getLocale(), null);
    }
    ErrorResource error = new ErrorResource("[Unauthorized]", message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return handleExceptionInternal(e, error, headers, ((UnauthorizedException) e).getCode(),
        request);
  }

  @ExceptionHandler({InvalidDataException.class})
  protected ResponseEntity<Object> handleInvalidDataRequest(RuntimeException e,
                                                            WebRequest request) {
    return handleBadRequest(e, request);
  }

  @ExceptionHandler({ConfigurationServiceException.class})
  protected ResponseEntity<Object> handleConfigurationServiceRequest(RuntimeException e,
                                                                     WebRequest request) {
    return handleBadRequest(e, request);
  }

  @ExceptionHandler({BadRequestException.class, BadCredentialsException.class})
  protected ResponseEntity<Object> handleBadRequest(Exception e, WebRequest request) {
    logWarning(request, e);
    ErrorResource error = new ErrorResource("[Bad Request]", e.getMessage());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return handleExceptionInternal(e, error, headers, HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler({InvalidTaskException.class})
  protected ResponseEntity<Object> handleInvalidTaskRequest(RuntimeException e,
                                                            WebRequest request) {
    return handleInvalidServiceRequest(e, request);
  }

  @ExceptionHandler({ValidationException.class})
  protected ResponseEntity<Object> handleValidationException(ValidationException e,
                                                             WebRequest request) {
    return handleBadRequest(e, request);
  }

  @ExceptionHandler({ObjectNotFoundException.class})
  protected ResponseEntity<Object> handleObjectNotFoundException(ObjectNotFoundException e,
                                                                 WebRequest request) {
    logWarning(request, e);
    ErrorResource
        error =
        new ErrorResource(StringUtils.isNotBlank(e.getCode()) ? e.getCode() : "[Not found]",
            e.getLocalisedMessage(
                getLocale()));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return handleExceptionInternal(e, error, headers, HttpStatus.NOT_FOUND, request);
  }

  @ExceptionHandler({UserDisabledException.class})
  protected ResponseEntity<Object> handleUserDisabled(RuntimeException e, WebRequest request) {
    return handleUnauthorizedRequest(e, request);
  }

  private ResponseEntity<Object> handleBadRequest(ValidationException e, WebRequest request) {
    logWarning(request, e);
    ErrorResource
        error =
        new ErrorResource("[Bad Request]", e.getLocalisedMessage(getLocale()));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return handleExceptionInternal(e, error, headers, HttpStatus.BAD_REQUEST, request);
  }

  private Locale getLocale() {
    try {
      return SecurityUtils.getLocale();
    } catch (Exception e) {
      return Locale.ENGLISH;
    }
  }

  @ExceptionHandler({HystrixBadRequestException.class})
  protected ResponseEntity<Object> handleBadRequest(HystrixBadRequestException e, WebRequest request) {
    logWarning(request, e);
    String message = e.getMessage();
    String code = "[Bad Request]";
    int statusCode = 400;
    if(e.getCause() instanceof LogiException){
      message = e.getCause().getMessage();
      code = ((LogiException) e.getCause()).getCode();
      statusCode = ((LogiException) e.getCause()).getStatusCode();
      //409 is used in Logi to reload when domain Id changes, hence changing it.
      if (statusCode == 409 || statusCode == 0) {
        statusCode = 400;
      }

    }
    ErrorResource
        error =
        new ErrorResource(code, message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return handleExceptionInternal(e, error, headers, HttpStatus.valueOf(statusCode), request);
  }

  @ExceptionHandler({ServiceException.class, SystemException.class, Exception.class})
  @Order(Ordered.LOWEST_PRECEDENCE)
  protected ResponseEntity<Object> handleServiceException(Exception e,
                                                          WebRequest request) {
    log(request, e);
    ErrorResource
        error =
        new ErrorResource(getCode(e), getMessage(e));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return handleExceptionInternal(e, error, headers, HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  private String getCode(Exception e) {
    if (e instanceof ExceptionWithCodes) {
      return ((ExceptionWithCodes) e).getCode();
    } else {
      return GENERAL_SYSTEM_ERROR;
    }
  }

  private String getMessage(Exception e) {
    if (e instanceof ExceptionWithCodes) {
      return ((ExceptionWithCodes) e).getLocalisedMessage(getLocale());
    } else {
      return ExceptionUtils.constructMessage(GENERAL_SYSTEM_ERROR,
          getLocale(), null);
    }
  }

  private void log(WebRequest request, Throwable throwable) {
    try {
      XLOGGER.severe("{2}: {0} failed for user {1}", request.getContextPath(),
          SecurityUtils.getUserDetails(), throwable);
    } catch (UnauthorizedException uae) {
      //ignored;
    }
  }

  private void logWarning(WebRequest request, Throwable throwable) {
    try {
      XLOGGER.warn("{2}: {0} failed for user {1}", request.getContextPath(),
          SecurityUtils.getUserDetails(), throwable);
    } catch (UnauthorizedException uae) {
      //ignored;
    }
  }

}
