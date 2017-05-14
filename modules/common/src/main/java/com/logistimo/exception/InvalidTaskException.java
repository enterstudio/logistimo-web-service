package com.logistimo.exception;

/**
 * Created by Mohan Raja on 16/03/15
 */
@SuppressWarnings("serial")
public class InvalidTaskException extends RuntimeException {
  public InvalidTaskException(String message) {
    super(message);
  }
}