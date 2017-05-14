package com.logistimo.api.models;

import java.util.List;

/**
 * Created by charan on 09/12/14.
 */
public class OrderStatusModel {
  public String st;
  public String msg;
  public List<String> users;
  public String cdrsn;
  /**
   * transporter
   */
  public String t;
  /**
   * tracking id
   */
  public String tid;

  /**
   * Expected fulfilment date
   */
  public String efd;
  /**
   * Package size
   */
  public String ps;

  public String orderUpdatedAt;
}
