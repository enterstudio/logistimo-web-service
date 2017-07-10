
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

package com.logistimo.entities.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class DemandItemModel {

  @SerializedName("material_id")
  @Expose
  private String materialId;
  @SerializedName("name")
  @Expose
  private String name;
  @SerializedName("ordered")
  @Expose
  private BigDecimal ordered;
  @SerializedName("recommended")
  @Expose
  private BigDecimal recommended;
  @SerializedName("originally_ordered")
  @Expose
  private BigDecimal originallyOrdered;
  @SerializedName("inventory")
  @Expose
  private CustomerVendor customerVendor;

  public String getMaterialId() {
    return materialId;
  }

  public void setMaterialId(String materialId) {
    this.materialId = materialId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BigDecimal getOrdered() {
    return ordered;
  }

  public void setOrdered(BigDecimal ordered) {
    this.ordered = ordered;
  }

  public BigDecimal getRecommended() {
    return recommended;
  }

  public void setRecommended(BigDecimal recommended) {
    this.recommended = recommended;
  }

  public BigDecimal getOriginallyOrdered() {
    return originallyOrdered;
  }

  public void setOriginallyOrdered(BigDecimal originallyOrdered) {
    this.originallyOrdered = originallyOrdered;
  }

  public CustomerVendor getCustomerVendor() {
    return customerVendor;
  }

  public void setCustomerVendor(CustomerVendor customerVendor) {
    this.customerVendor = customerVendor;
  }
}
