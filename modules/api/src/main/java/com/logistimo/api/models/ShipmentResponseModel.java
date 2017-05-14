package com.logistimo.api.models;

/**
 * Created by naveensnair on 20/04/17.
 */
public class ShipmentResponseModel {
  public String respMsg;
  public String orderUpdatedAt;

  public ShipmentResponseModel(String respMsg, String orderUpdatedAt){
    this.respMsg = respMsg;
    this.orderUpdatedAt = orderUpdatedAt;
  }
}
