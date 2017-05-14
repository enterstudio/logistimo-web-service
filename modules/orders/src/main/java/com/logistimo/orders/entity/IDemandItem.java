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

package com.logistimo.orders.entity;

import com.logistimo.tags.entity.ITag;

import com.logistimo.domains.IOverlappedDomain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by charan on 20/05/15.
 */
public interface IDemandItem extends IOverlappedDomain {
  String ORDERING_DISCREPANCY = "ordering";
  String SHIPPING_DISCREPANCY = "shipping";
  String FULFILLMENT_DISCREPANCY = "fulfillment";

  Long getDomainId();

  void setDomainId(Long dId);

  Long getKioskId();

  void setKioskId(Long kId);

  Long getMaterialId();

  void setMaterialId(Long mId);

  BigDecimal getQuantity();

  void setQuantity(BigDecimal q);

  BigDecimal getOriginalQuantity();

  void setOriginalQuantity(BigDecimal quantity);

  String getStatus();

  void setStatus(String st);

  Date getTimestamp();

  void setTimestamp(Date t);

  BigDecimal getUnitPrice();

  void setUnitPrice(BigDecimal up);

  // Get formatted unit price, formatted to two decimal places
  String getFormattedPrice();

  // Computes item price without any tax added to it
  BigDecimal computeTotalPrice(boolean includeTax);

  String getCurrency();

  void setCurrency(String cr);

  String getMessage();

  void setMessage(String ms);

  String getUserId();

  void setUserId(String uId);

  Long getOrderId();

  BigDecimal getDiscount();

  void setDiscount(BigDecimal dct);

  BigDecimal getTax();

  void setTax(BigDecimal tax);

  List<String> getTags(String tagType);

  void setTags(List<String> tags, String tagType);

  void setTgs(List<? extends ITag> tags, String tagType);

  Set<? extends IDemandItemBatch> getItemBatches();

  void setItemBatches(Set<? extends IDemandItemBatch> itemBatches);

  void addBatch(IDemandItemBatch batch);

  BigDecimal getQuantityByBatches();

  List<Map<String, Object>> getBatchesAsMap();

  Map<String, Object> toMap(String currency, Locale locale, String timezone,
                            boolean forceIntegerQuantity);

  void setLinkedKioskId(Long linkedKioskId);

  String getIdAsString();

  void updateOId(IOrder o);

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);

  BigDecimal getRecommendedOrderQuantity();

  void setRecommendedOrderQuantity(BigDecimal roq);

  String getReason();

  void setReason(String rsn);

  BigDecimal getShippedQuantity();

  void setShippedQuantity(BigDecimal quantity);

  BigDecimal getInShipmentQuantity();

  void setInShipmentQuantity(BigDecimal quantity);

  BigDecimal getDiscrepancyQuantity();

  void setDiscrepancyQuantity(BigDecimal quantity);

  BigDecimal getFulfilledQuantity();

  void setFulfilledQuantity(BigDecimal quantity);

  /**
   * @return time to order, timediff from min event generated time to order creation time.
   */
  Long getTimeToOrder();

  void setTimeToOrder(Long timeToOrder);

  String getShippedDiscrepancyReason();

  void setShippedDiscrepancyReason(String sdrsn);

  /**
   * Transient
   */
  BigDecimal getAllocatedStock();

  /**
   * Transient non-persisted
   */
  void setAllocatedStock(BigDecimal quantity);
}
