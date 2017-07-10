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
  TRANSFERS(0),

  @SerializedName("3")
  SHIPMENT(3);

  private final int value;

  ApprovalType(int value) {
    this.value = value;
  }

  public static ApprovalType get(int approvalValue) {
    for (ApprovalType approvalType : ApprovalType.values()) {
      if (approvalValue == approvalType.value) {
        return approvalType;
      }
    }
    return null;
  }

  public int getValue() {
    return value;
  }
}
