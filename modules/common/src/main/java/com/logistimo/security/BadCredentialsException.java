/**
 *
 */
package com.logistimo.security;

/**
 * @author Arun
 */
public class BadCredentialsException extends Exception {

  private static final long serialVersionUID = 1L;

  public BadCredentialsException() {
    super();
  }

  public BadCredentialsException(String message) {
    super(message);
  }
}
