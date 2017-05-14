/**
 *
 */
package com.logistimo.communications;


/**
 * @author Arun
 */
@SuppressWarnings("serial")
public class MessageHandlingException extends Exception {

  public MessageHandlingException(String message) {
    super(message);
  }

  public MessageHandlingException(Exception e) {
    super(e);
  }
}
