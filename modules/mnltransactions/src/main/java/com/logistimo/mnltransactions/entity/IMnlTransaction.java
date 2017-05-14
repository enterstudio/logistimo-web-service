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

package com.logistimo.mnltransactions.entity;

import com.logistimo.tags.entity.ITag;

import com.logistimo.domains.IOverlappedDomain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by charan on 20/05/15.
 */
public interface IMnlTransaction extends IOverlappedDomain {

  String getKeyString();

  Long getDomainId();

  void setDomainId(Long domainId);

  String getUserId();

  void setUserId(String userId);

  Long getKioskId();

  void setKioskId(Long kioskId);

  Long getMaterialId();

  void setMaterialId(Long materialId);

  BigDecimal getOpeningStock();

  void setOpeningStock(BigDecimal openingStock);

  Date getReportingPeriod();

  void setReportingPeriod(Date reportingPeriod);

  BigDecimal getReceiptQuantity();

  void setReceiptQuantity(BigDecimal receiptQuantity);

  BigDecimal getIssueQuantity();

  void setIssueQuantity(BigDecimal issueQuantity);

  BigDecimal getDiscardQuantity();

  void setDiscardQuantity(BigDecimal discardQuantity);

  double getStockoutDuration();

  void setStockoutDuration(int stockoutDuration);

//    void setNumberOfStockouts(int noOfStockoutInstances);

//    double getNumberOfStockoutInstances();

  BigDecimal getManualConsumptionRate();

  void setManualConsumptionRate(BigDecimal manualConsumptionRate);

  BigDecimal getComputedConsumptionRate();

  void setComputedConsumptionRate(BigDecimal computedConsumptionRate);

  BigDecimal getOrderedQuantity();

  void setOrderedQuantity(BigDecimal orderedQuantity);

  BigDecimal getFulfilledQuantity();

  void setFulfilledQuantity(BigDecimal fulfilledQuantity);

  BigDecimal getClosingStock();

  void setTgs(List<? extends ITag> tags);

  List<String> getTags();

  void setTags(List<String> tags);

  boolean hasTag(String tag);

  Long getVendorId();

  void setVendorId(Long vendorId);

  Date getTimestamp();

  void setTimestamp(Date timeStamp);
}
