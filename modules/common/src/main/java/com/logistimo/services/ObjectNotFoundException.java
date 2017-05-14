/**
 *
 */
package com.logistimo.services;

/**
 * @author arun
 */
@SuppressWarnings("serial")
public class ObjectNotFoundException extends Exception {

  public ObjectNotFoundException(String message) {
    super(message);
  }

  public ObjectNotFoundException(String message, Throwable t) {
    super(message, t);
  }

  public ObjectNotFoundException(Exception e) {
    super(e);
  }
}
