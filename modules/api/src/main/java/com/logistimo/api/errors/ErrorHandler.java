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