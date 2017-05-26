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

import com.logistimo.exception.BadRequestException;
import com.logistimo.exception.ConfigurationServiceException;
import com.logistimo.exception.ErrorResource;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.InvalidTaskException;
import com.logistimo.exception.UnauthorizedException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Created by Mohan Raja on 12/03/15
 */

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({InvalidServiceException.class})
  protected ResponseEntity<Object> handleInvalidServiceRequest(RuntimeException e,
                                                               WebRequest request) {
    ErrorResource error = new ErrorResource("[Internal Server Error]", e.getMessage());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return handleExceptionInternal(e, error, headers, HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler({UnauthorizedException.class})
  protected ResponseEntity<Object> handleUnauthorizedRequest(RuntimeException e,
                                                             WebRequest request) {
    ErrorResource error = new ErrorResource("[Unauthorized]", e.getMessage());
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

  @ExceptionHandler({BadRequestException.class})
  protected ResponseEntity<Object> handleBadRequest(RuntimeException e, WebRequest request) {
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

}