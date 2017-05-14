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

import com.logistimo.tags.entity.ITag;

import com.logistimo.domains.IOverlappedDomain;
import com.logistimo.events.IEvents;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by charan on 20/05/15.
 */
public interface IInvntry extends IOverlappedDomain {
  // Inventory model types
  String MODEL_NONE = "";
  String MODEL_SQ = "sq";
  String MODEL_MINMAX = "mm";
  String MODEL_KANBAN = "kn";

  // Types of inventory
  int ALL = 0;
  int BATCH_ENABLED = 1;
  int BATCH_DISABLED = 2;

  BigDecimal getPredictedDaysOfStock();

  void setPredictedDaysOfStock(BigDecimal predictedDaysOfStock);

  Long getKey();

  Long getDomainId();

  void setDomainId(Long domainId);

  Long getMaterialId();

  void setMaterialId(Long materialId);

  Long getKioskId();

  void setKioskId(Long kioskId);

  Long getShortId();

  void setShortId(Long sId);

  float getServiceLevel();

  void setServiceLevel(float serviceLevel);

  String getInventoryModel();

  void setInventoryModel(String inventoryModel);

  BigDecimal getLeadTime();

  void setLeadTime(BigDecimal leadTime);

  BigDecimal getStock();

  void setStock(BigDecimal stock);

  BigDecimal getAllocatedStock();

  void setAllocatedStock(BigDecimal stock);

  BigDecimal getInTransitStock();

  void setInTransitStock(BigDecimal stock);

  BigDecimal getAvailableStock();

  void setAvailableStock(BigDecimal stock);

  BigDecimal getLeadTimeDemand();

  void setLeadTimeDemand(BigDecimal leadTimeDemand);

  BigDecimal getRevPeriodDemand();

  void setRevPeriodDemand(BigDecimal revPeriodDemand);

  BigDecimal getStdevRevPeriodDemand();

  void setStdevRevPeriodDemand(BigDecimal stdevRevPeriodDemand);

  BigDecimal getSafetyStock();

  void setSafetyStock(BigDecimal safetyStock);

  boolean isSafetyStockDefined();

  Date getTimestamp(); // use getStockTimestamp() instead

  void setTimestamp(Date timeStamp); // use setStockTimestamp() instead

  // Timestamp of inventory creation (introduced Sept 12, 2015)
  // Note: this will return null for older records in database which do not have a creation time
  Date getCreatedOn();

  void setCreatedOn(Date createdOn);

  Date getReorderLevelUpdatedTime();

  Date getMaxUpdatedTime();

  Date getMnlConsumptionRateUpdatedTime();

  Date getRetailerPriceUpdatedTime();

  BigDecimal getOrderPeriodicity();

  void setOrderPeriodicity(BigDecimal orderPeriodicity);

  String getKioskName();

  void setKioskName(String kioskName);

  String getMaterialName();

  void setMaterialName(String materialName);

  String getBinaryValued();

  void setBinaryValued(String binaryValued);

  BigDecimal getReorderLevel();

  void setReorderLevel(BigDecimal reordLevel);

  BigDecimal getMaxStock();

  void setMaxStock(BigDecimal maxStock);

  BigDecimal getMinDuration();

  void setMinDuration(BigDecimal minDuration);

  BigDecimal getMaxDuration();

  void setMaxDuration(BigDecimal maxDuration);

  BigDecimal getNormalizedSafetyStock();

  boolean isStockUnsafe();

  boolean isStockExcess();

  // Get the remaining period for which stock exists

  BigDecimal getConsumptionRateManual(); // units are in OptimizerConfig

  void setConsumptionRateManual(BigDecimal consumptionRate);

  BigDecimal getConsumptionRateDaily();

  void setConsumptionRateDaily(BigDecimal crt);

  BigDecimal getConsumptionRateWeekly();

  BigDecimal getConsumptionRateMonthly();

  BigDecimal getEconomicOrderQuantity();

  void setEconomicOrderQuantity(BigDecimal eoq);

  Date getPSTimestamp();

  void setPSTimestamp(Date tPS);

  Date getDQTimestamp();

  void setDQTimestamp(Date tDQ);

  String getOptMessage();

  void setOptMessage(String msg);

  List<String> getTags(String tagType);

  void setTags(List<String> tags, String tagType);

  void setTgs(List<? extends ITag> tags, String tagType);

  BigDecimal getRetailerPrice();

  void setRetailerPrice(BigDecimal price);

  BigDecimal getTax();

  void setTax(BigDecimal tax);

  Long getLastStockEvent();

  void setLastStockEvent(Long key);

  String getIdString();


  Float getTmin();

  void setTmin(Float tmin);

  Float getTmax();

  void setTmax(Float tmax);

  // Reset stock and related parameters
  void reset();

  // Get the type of inventory stock event such as stock-out, under/over stock; otherwise, return -1
  int getStockEvent();

  String getKeyString();

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);

  String getUpdatedBy();

  void setUpdatedBy(String user);

  String getMCRUnits(Locale locale);
}
