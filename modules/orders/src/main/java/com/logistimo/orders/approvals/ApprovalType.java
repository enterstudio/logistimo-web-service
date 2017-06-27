package com.logistimo.orders.approvals;

import com.google.gson.annotations.SerializedName;

/**
 * Created by charan on 22/06/17.
 */
public enum ApprovalType {
  @SerializedName("1")
  PURCHASE_ORDER(1),

  @SerializedName("2")
  SALES_ORDER(2),

  @SerializedName("0")
  TRANSFERS(0);

  private final int value;

  ApprovalType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
