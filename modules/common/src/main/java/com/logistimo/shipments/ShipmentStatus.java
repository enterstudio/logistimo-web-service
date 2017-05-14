package com.logistimo.shipments;

/**
 * Created by Mohan Raja on 29/09/16
 */
public enum ShipmentStatus {
  // PENDING is introduced only for display purpose. Not to be used as a status.
  // Just to maintain consistency with Order. Will be removed once Order status use OPEN.
  OPEN("op"), PENDING("op"), CONFIRMED("cf"), SHIPPED("sp"), FULFILLED("fl"), CANCELLED("cn");
  private String value;

  ShipmentStatus(String value) {
    this.value = value;
  }

  public static ShipmentStatus getStatus(String value) {
    for (ShipmentStatus shipmentStatus : ShipmentStatus.values()) {
      if (shipmentStatus.value.equals(value)) {
        return shipmentStatus;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return value;
  }
}
