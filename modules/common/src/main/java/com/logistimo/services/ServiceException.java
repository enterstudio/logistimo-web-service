package com.logistimo.services;

import com.logistimo.exception.LogiException;

import java.util.List;
import java.util.Locale;

@SuppressWarnings("serial")
public class ServiceException extends LogiException {

  private List<String> entityIds = null; // Ids of entities

  public ServiceException(String message) {
    super(message);
  }

  public ServiceException(String message, Throwable t) {
    super(message, t);
  }

  public ServiceException(String message, List<String> entityIds) {
    super(message);
    this.entityIds = entityIds;
  }

  public ServiceException(String code, Locale locale, Object... arguments) {
    super(code, locale, arguments);
  }

  public ServiceException(String code, Object... arguments) {
    super(code, arguments);
  }

  public ServiceException(Throwable t) {
    super(t);
  }

  /**
   * Get dependent entities
   *
   * @return List of String entity Ids
   */
  public List<String> getEntities() {
    return this.entityIds;
  }
}
