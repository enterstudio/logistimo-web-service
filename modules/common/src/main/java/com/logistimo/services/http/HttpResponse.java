package com.logistimo.services.http;

/**
 * Created by charan on 16/03/15.
 */
public class HttpResponse {

  private int responseCode;

  private byte[] content;

  public HttpResponse(int responseCode, byte[] content) {
    this.responseCode = responseCode;
    this.content = content;
  }

  public HttpResponse() {

  }


  public int getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }
}
