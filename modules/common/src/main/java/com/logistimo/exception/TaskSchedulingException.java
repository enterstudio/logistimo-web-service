/**
 *
 */
package com.logistimo.exception;

/**
 * Thrown if there is an exception in scheduling tasks with GAE.
 *
 * @author arun
 */
@SuppressWarnings("serial")
public class TaskSchedulingException extends Exception {

  public TaskSchedulingException() {
    super();
  }

  public TaskSchedulingException(String message) {
    super(message);
  }

  public TaskSchedulingException(Throwable e) {
    super(e);
  }
}
