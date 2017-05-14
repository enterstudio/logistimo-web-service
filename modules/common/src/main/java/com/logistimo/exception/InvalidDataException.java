package com.logistimo.exception;

/**
 * Created by Mohan Raja on 13/03/15
 */
@SuppressWarnings("serial")
public class InvalidDataException extends RuntimeException {
  public InvalidDataException(String message) {
    super(message);
  }
}
