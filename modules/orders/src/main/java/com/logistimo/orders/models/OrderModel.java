package com.logistimo.orders.models;

import com.google.gson.annotations.SerializedName;

import com.logistimo.entities.models.EntityMinModel;

import java.util.Date;

/**
 * Created by charan on 26/06/17.
 */
public class OrderModel {
  private EntityMinModel customer;
  private EntityMinModel vendor;
  private Long orderId;
  @SerializedName("created_at")
  private Date createdAt;
  @SerializedName("num_items")
  private int numItems;

  public void setCustomer(EntityMinModel customer) {
    this.customer = customer;
  }

  public void setVendor(EntityMinModel vendor) {
    this.vendor = vendor;
  }

  public EntityMinModel getCustomer() {
    return customer;
  }

  public EntityMinModel getVendor() {
    return vendor;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public int getNumItems() {
    return numItems;
  }

  public void setNumItems(int numItems) {
    this.numItems = numItems;
  }
}
