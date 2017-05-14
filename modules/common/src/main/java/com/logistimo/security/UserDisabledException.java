/**
 *
 */
package com.logistimo.security;

/**
 * @author Arun
 */
public class UserDisabledException extends Exception {

  private static final long serialVersionUID = 1L;

  public UserDisabledException() {
    super();
  }

  public UserDisabledException(String message) {
    super(message);
  }
}
