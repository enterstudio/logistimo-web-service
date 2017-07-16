/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

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
  private Integer type;
  @SerializedName("created_at")
  private Date createdAt;
  @SerializedName("num_items")
  private int numItems;

  public EntityMinModel getCustomer() {
    return customer;
  }

  public void setCustomer(EntityMinModel customer) {
    this.customer = customer;
  }

  public EntityMinModel getVendor() {
    return vendor;
  }

  public void setVendor(EntityMinModel vendor) {
    this.vendor = vendor;
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

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }
}
