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

/**
 * Created by charan on 20/05/15.
 */
public interface ITransaction
    extends Cloneable, IOverlappedDomain {
  // Transaction types
  String TYPE_ISSUE = "i";
  String TYPE_RECEIPT = "r";
  String TYPE_PHYSICALCOUNT = "p";
  String TYPE_TRANSFER = "t";
  String TYPE_ORDER = "o";
  String TYPE_REORDER = "oo";
  String TYPE_WASTAGE = "w";
  String TYPE_RETURN = "rt";
  String TYPE_SHIPMENT = "s";

  String getKeyString();

  Long getMaterialId();

  void setMaterialId(Long materialId);

  Integer getSrc();

  void setSrc(Integer src);

  Long getKioskId();

  void setKioskId(Long kioskId);

  Date getTimestamp();

  void setTimestamp(Date timeStamp);

  String getSourceUserId();

  void setSourceUserId(String suId);

  String getDestinationUserId();

  void setDestinationUserId(String duId);

  BigDecimal getQuantity();

  void setQuantity(BigDecimal quantity);

  String getType();

  void setType(String inventoryTransactionType);

  Long getDomainId();

  void setDomainId(Long dId);

  String getMessage();

  void setMessage(String message);

  String getMsgCode();

  void setMsgCode(String msgCode);

  String getBatchId();

  void setBatchId(String bid);

  boolean hasBatch();

  Date getBatchExpiry();

  void setBatchExpiry(Date batchExpiry);

  String getBatchManufacturer();

  void setBatchManufacturer(String manufacturerName);

  Date getBatchManufacturedDate();

  void setBatchManufacturedDate(Date manufacturedDate);

  Long getLinkedKioskId();

  void setLinkedKioskId(Long lkId);

  String getTrackingId();

  void setTrackingId(String tid);

  BigDecimal getStockDifference();

  void setStockDifference(BigDecimal sdf);

  BigDecimal getClosingStock();

  void setClosingStock(BigDecimal closingStock);

  BigDecimal getClosingStockByBatch();

  void setClosingStockByBatch(BigDecimal closingStockByBatch);

  double getLatitude();

  void setLatitude(double latitude);

  double getLongitude();

  void setLongitude(double longitude);

  boolean hasGeo();

  double getGeoAccuracy();

  void setGeoAccuracy(double geoAccuracyMeters);

  String getGeoErrorCode();

  void setGeoErrorCode(String errorCode);

  String getReason();

  void setReason(String rs);

  List<String> getTags(String tagType);

  void setTags(List<String> tags, String tagType);

  void setTgs(List<? extends ITag> tags, String tagType);

  BigDecimal getOpeningStock();

  void setOpeningStock(BigDecimal openingStock);

  BigDecimal getOpeningStockByBatch();

  void setOpeningStockByBatch(BigDecimal openingStockByBatch);

  // Get the fingerprint of this transaction
  byte[] fingerprint();

  // Flag to indicate whether to use the custom timestamp set within the transaction (instead of generating a new one)
  boolean useCustomTimestamp();

  void setUseCustomTimestamp(boolean useCustomTimestamp);

  Long getTransactionId();

  void setTransactionId(Long trnId);

  String getTrackingObjectType();

  void setTrackingObjectType(String tot);

  ITransaction clone();

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);

  String getMaterialStatus();

  void setMaterialStatus(String materialStatus);

  double getAltitude();

  void setAltitude(double altitude);

  Date getAtd();

  void setAtd(Date atd);

  Boolean getEatd();

  void setEatd(Boolean eatd);

  String getEditOrderQtyReason();

  void setEditOrderQtyRsn(String eoqrsn);

}
