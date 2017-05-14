/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

/**
 * Represents an exceptions in data format (e.g. JSON exceptions)
 *
 * @author Arun
 */
public class ProtocolException extends Exception {

  public ProtocolException(String message) {
    super(message);
  }
}
