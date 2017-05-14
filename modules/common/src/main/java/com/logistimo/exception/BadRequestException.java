package com.logistimo.exception;

import com.logistimo.services.ServiceException;

/**
 * Created by Mohan Raja on 16/03/15.
 */
@SuppressWarnings("serial")
public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(ServiceException e) {
    super(e.getMessage(), e);
  }
}