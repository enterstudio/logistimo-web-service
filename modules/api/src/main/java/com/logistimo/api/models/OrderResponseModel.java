package com.logistimo.api.models;


/**
 * Created by charan on 09/12/14.
 */
public class OrderResponseModel {
  public OrderModel order;
  public String msg;
  public boolean invErr;
  public String respData;

  public OrderResponseModel(OrderModel order, String message, boolean inventoryError,
                            String respData) {
    this.order = order;
    this.msg = message;
    this.invErr = inventoryError;
    this.respData = respData;
  }

  public OrderResponseModel() {

  }
}
