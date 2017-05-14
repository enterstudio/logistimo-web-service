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

package com.logistimo.shipments.entity;

import com.logistimo.domains.IOverlappedDomain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Created by Mohan Raja on 28/09/16
 */
public interface IShipmentItemBatch extends IOverlappedDomain {
  String BATCH_ID = "bid";
  String QUANTITY = "quantity";
  String EXPIRY = "expiry";
  String MANUFACTURER = "manufacturer";
  String MANUFACTURED_ON = "manufacturedon";

  Long getShipmentItemId();

  void setShipmentItemId(Long siId);

  Long getDomainId();

  void setDomainId(Long domainId);

  Long getKioskId();

  void setKioskId(Long kId);

  Long getMaterialId();

  void setMaterialId(Long mId);

  BigDecimal getQuantity();

  void setQuantity(BigDecimal quantity);

  BigDecimal getDiscrepancyQuantity();

  void setDiscrepancyQuantity(BigDecimal quantity);

  BigDecimal getFulfilledQuantity();

  void setFulfilledQuantity(BigDecimal quantity);

  String getDiscrepancyReason();

  void setDiscrepancyReason(String rsn);

  String getBatchId();

  void setBatchId(String bid);

  Date getBatchExpiry();

  void setBatchExpiry(Date batchExpiry);

  String getBatchManufacturer();

  void setBatchManufacturer(String manufacturerName);

  Date getBatchManufacturedDate();

  void setBatchManufacturedDate(Date manufacturedDate);

  String getUpdatedBy();

  void setUpdatedBy(String uBy);

  Date getUpdatedOn();

  void setUpdatedOn(Date uOn);

  String getCreatedBy();

  void setCreatedBy(String cBy);

  Date getCreatedOn();

  void setCreatedOn(Date cOn);

  String getShippedMaterialStatus();

  void setShippedMaterialStatus(String smst);

  String getFulfilledMaterialStatus();

  void setFulfilledMaterialStatus(String fmst);

  String getFulfilledDiscrepancyReason();

  void setFulfilledDiscrepancyReason(String fdrsn);
}
