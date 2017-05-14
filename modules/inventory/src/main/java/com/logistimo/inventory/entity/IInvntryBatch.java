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
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

/**
 * Created by charan on 20/05/15.
 */
public interface IInvntryBatch extends IOverlappedDomain {

  Long getDomainId();

  void setDomainId(Long domainId);

  Long getKioskId();

  void setKioskId(Long kioskId, String kioskName);

  Long getMaterialId();

  void setMaterialId(Long materialId);

  BigDecimal getQuantity();

  void setQuantity(BigDecimal q);

  BigDecimal getAllocatedStock();

  void setAllocatedStock(BigDecimal stock);

  BigDecimal getAvailableStock();

  void setAvailableStock(BigDecimal stock);

  String getBatchId();

  void setBatchId(String bid);

  Date getBatchExpiry();

  void setBatchExpiry(Date batchExpiry);

  boolean isExpired();

  String getBatchManufacturer();

  void setBatchManufacturer(String manufacturerName);

  Date getBatchManufacturedDate();

  void setBatchManufacturedDate(Date manufacturedDate);

  Date getTimestamp();

  void setTimestamp(Date t);

  List<String> getTags(String tagType);

  void setTags(List<String> tags, String tagType);

  void setTgs(List<? extends ITag> tags, String tagType);

  String getKeyString();

  Boolean getVld();

  void setVld(Boolean vld);

  Hashtable<String, String> toMapZ(Locale locale, String timezone,
                                   boolean isAutoPostingIssuesEnabled);

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);
}
