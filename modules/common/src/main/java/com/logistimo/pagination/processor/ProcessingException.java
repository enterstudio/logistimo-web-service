/**
 *
 */
package com.logistimo.pagination.processor;

/**
 * @author Arun
 */
public class ProcessingException extends Exception {

  private static final long serialVersionUID = 1L;

  public ProcessingException(String message) {
    super(message);
  }

  public ProcessingException(Exception e) {
    super(e);
  }
}
