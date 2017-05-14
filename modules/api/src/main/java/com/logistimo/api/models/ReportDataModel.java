package com.logistimo.api.models;

/**
 * Created by mohan on 31/03/17.
 */
public class ReportDataModel {

  public ReportDataModel(String value) {
    this.value = value;
  }

  public ReportDataModel(String value, String num, String den) {
    this.value = value;
    this.num = num;
    this.den = den;
  }

  public String value;
  public String num;
  public String den;
}
