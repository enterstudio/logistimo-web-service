/**
 *
 */
package com.logistimo.services;

/**
 * @author Arun
 */
public class InsufficientDataException extends Exception {

  private static final long serialVersionUID = 1L;

  public InsufficientDataException(String msg) {
    super(msg);
  }
}
