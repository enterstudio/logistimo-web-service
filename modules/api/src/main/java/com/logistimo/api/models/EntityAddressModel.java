package com.logistimo.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by charan on 22/06/17.
 */
public class EntityAddressModel {

  @SerializedName("entity_id")
  private String entityId;

  private String name;

  private AddressModel address;

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AddressModel getAddress() {
    return address;
  }

  public void setAddress(AddressModel address) {
    this.address = address;
  }
}
