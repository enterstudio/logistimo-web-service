package com.logistimo.api.models;

/**
 * Created by Mohan Raja on 03/04/15.
 */
public class AuthModel {
  /**
   * Has error
   */
  public boolean isError;
  /**
   * Error message
   */
  public String errorMsg;
  /**
   * Error code
   */
  public int ec;

  public AuthModel() {
  }

  public AuthModel(boolean isError, String errorMsg) {
    this.isError = isError;
    this.errorMsg = errorMsg;
  }
}
