/**
 *
 */
package com.logistimo.communications;

import java.util.Date;

/**
 * @author Arun
 */
public class Status {

  private String mobileNo = null; // mobile number involved in the response
  private String code = null; // response code/status
  private String message = null; // any message as part of response
  private Date date = null; // timestamp, if present

  public String getMobileNo() {
    return mobileNo;
  }

  public void setMobileNo(String mobileNo) {
    this.mobileNo = mobileNo;
  }

  public String getStatusCode() {
    return code;
  }

  public void setStatusCode(String code) {
    this.code = code;
  }

  public String getStatusMessage() {
    return message;
  }

  public void setStatusMessage(String message) {
    this.message = message;
  }

  public Date getTimestamp() {
    return date;
  }

  public void setTimestamp(Date date) {
    this.date = date;
  }
}
