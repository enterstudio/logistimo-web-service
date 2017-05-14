package com.logistimo.exception;

/**
 * Created by Mohan Raja on 14/03/15
 */

@SuppressWarnings("serial")
public class ConfigurationServiceException extends RuntimeException {
  public ConfigurationServiceException(String message) {
    super(message);
  }
}
