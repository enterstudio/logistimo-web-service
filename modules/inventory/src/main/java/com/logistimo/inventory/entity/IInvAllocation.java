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

package com.logistimo.inventory.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by mohan on 29/09/16
 */
public interface IInvAllocation {

  Long getAllocationId();

  Long getMaterialId();

  void setMaterialId(Long materialId);

  Long getKioskId();

  void setKioskId(Long kioskId);

  String getBatchId();

  void setBatchId(String bId);

  BigDecimal getQuantity();

  void setQuantity(BigDecimal q);

  String getType();

  void setType(String type);

  String getTypeId();

  void setTypeId(String typeId);

  List<String> getTags();

  void setTags(List<String> tags);

  String getUpdatedBy();

  void setUpdatedBy(String uBy);

  Date getUpdatedOn();

  void setUpdatedOn(Date uOn);

  String getCreatedBy();

  void setCreatedBy(String cBy);

  Date getCreatedOn();

  void setCreatedOn(Date cOn);

  String getMaterialStatus();

  void setMaterialStatus(String mst);

  enum Type {
    ORDER("o"), SHIPMENT("s");
    private String value;

    Type(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }
}
