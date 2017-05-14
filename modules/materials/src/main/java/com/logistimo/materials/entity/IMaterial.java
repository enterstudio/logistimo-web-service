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

package com.logistimo.materials.entity;

import com.logistimo.tags.entity.ITag;

import com.logistimo.domains.ISubDomain;
import com.logistimo.domains.ISuperDomain;
import com.logistimo.events.IEvents;
import com.logistimo.proto.JsonTagsZ;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by charan on 20/05/15.
 */
public interface IMaterial extends ISubDomain, ISuperDomain {
  // Identifier types
  String IDTYPE_UPC = "upc";
  String IDTYPE_ISBN = "isb";
  String IDTYPE_NONSTANDARD = "nns";
  // Date types
  String TYPE_BINARY = JsonTagsZ.TYPE_BINARY;

  Long getMaterialId();

  void setMaterialId(Long materialId);

  Long getDomainId();

  void setDomainId(Long dId);

  String getVertical();

  void setVertical(String vertical);

  String getName();

  void setName(String name);

  String getTruncatedName(int numChars);

  String getDescription();

  void setDescription(String description);

  List<String> getTags();

  void setTags(List<String> tags);

  boolean hasTag(String tag);

  String getIdentifierType();

  void setIdentifierType(String identifierType);

  String getIdentifierValue();

  void setIdentifierValue(String identifierValue);

  boolean isSeasonal();

  void setSeasonal(boolean seasonal);

  String getImagePath();

  void setImagePath(String imagePath);

  Date getTimeStamp();

  void setTimeStamp(Date timeStamp);

  Date getLastUpdated();

  void setLastUpdated(Date lastUpdated);

  BigDecimal getMSRP();

  void setMSRP(BigDecimal retailPrice);

  BigDecimal getSalePrice();

  void setSalePrice(BigDecimal salePrice);

  BigDecimal getRetailerPrice();

  void setRetailerPrice(BigDecimal retailerPrice);

  String getCurrency();

  void setCurrency(String currency);

  String getUniqueName();

  String getShortCode();

  void setShortCode(String scode);

  String getShortName();

  void setShortName(String sname);

  String getInfo();

  void setInfo(String info);

  String getType();

  void setType(String ty);

  boolean isBinaryValued();

  boolean displayInfo();

  void setInfoDisplay(boolean display);

  boolean isBatchEnabled();

  void setBatchEnabled(boolean batchEnabled);

  boolean isBatchEnabledOnMobile();

  void setBatchEnabledOnMobile(boolean batchEnabled);

  String getCustomId();

  void setCustomId(String customId);

  boolean isTemperatureSensitive();

  void setTemperatureSensitive(boolean isSensitive);

  float getTemperatureMin();

  void setTemperatureMin(float min);

  float getTemperatureMax();

  void setTemperatureMax(float max);

  String toJsonStr();

  void setTgs(List<? extends ITag> tgs);

  String getCreatedBy();

  void setCreatedBy(String username);

  String getLastUpdatedBy();

  void setLastUpdatedBy(String username);

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);

}
