package com.logistimo.api.servlets.mobile.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import com.logistimo.constants.Constants;

public class JsonOutput {

  public static final String VERSION_DEFAULT = "01";

  @Expose
  protected String v = VERSION_DEFAULT; // version
  @Expose
  protected String st = null; // status: 0 = success, 1 = failure
  @Expose
  protected String ms = null; // message
  @Expose
  protected String cs = null; // cursor

  public JsonOutput(String version, boolean status, String message) {
    this.st = (status ? "0" : "1");
    this.ms = message;
    this.v = version;
  }

  public boolean getStatus() {
    return (st != null && "0".equals(st));
  }

  public String getMessage() {
    return ms;
  }

  public String getVersion() {
    return v;
  }

  public String getCursor() {
    return cs;
  }

  public void setCursor(String cursor) {
    cs = cursor;
  }

  public String toJSONString() {
    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    return gson.toJson(this);
  }
}
